package com.github.trelawnm.weathersdk;

import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.trelawnm.weathersdk.model.WeatherResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Weather SDK - lightweight Java library for OpenWeatherMap API.
 * Provides a simple interface to retrieve weather data in JSON format.
 * 
 * <p><b>Operation Modes:</b>
 * <ul>
 *   <li><b>ON_DEMAND</b> - Lazy polling: data is fetched on request with TTL-based caching</li>
 *   <li><b>POLLING</b> - True polling: background agent periodically updates all cached cities</li>
 * </ul>
 * </p>
 * 
 * <p><b>Usage Example (ON_DEMAND mode):</b>
 * <pre>
 * {@code
 * WeatherSDK sdk = new WeatherSDK.Builder("your-api-key")
 *     .endpoint("https://api.openweathermap.org/data/2.5/")
 *     .mode(WeatherSDKMode.ON_DEMAND)
 *     .build();
 * String weatherJson = sdk.getWeather("London");
 * System.out.println(weatherJson);
 * 
 * // Don't forget to cleanup when done
 * sdk.shutdown();
 * }
 * </pre>
 * </p>
 * 
 * <p><b>Usage Example (POLLING mode):</b>
 * <pre>
 * {@code
 * // SDK automatically starts background polling agent
 * WeatherSDK sdk = new WeatherSDK.Builder("your-api-key")
 *     .mode(WeatherSDKMode.POLLING)
 *     .pollingInterval(Duration.ofMinutes(10))
 *     .maxSize(15)
 *     .build();
 * 
 * // Immediate response from cache (zero latency)
 * String weatherJson = sdk.getWeather("London");
 * System.out.println(weatherJson);
 * 
 * // Don't forget to cleanup when done
 * sdk.shutdown();
 * }
 * </pre>
 * </p>
 * 
 * <p><b>Resource Management:</b>
 * For POLLING mode, always call {@link #shutdown()} to stop background threads
 * and release resources when the SDK is no longer needed.
 * </p>
 * 
 * @author trelawnm
 * @version 1.0
 * @see <a href="https://openweathermap.org/api">OpenWeatherMap API</a>
 */
public class WeatherSDK {
    private final String key;
    private final String endpoint;
    private final WeatherSDKMode mode;
    private final Duration pollingInterval;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Cache manager for weather data with LRU eviction and TTL support.
     * Shared across both operation modes with different usage patterns.
     */
    private final WeatherCache cache;
    private final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    /**
     * Scheduled executor service for background polling in POLLING mode.
     * Null in ON_DEMAND mode to conserve resources.
     */
    private final ScheduledExecutorService scheduler;

    /**
     * Returns the API key used for authentication with OpenWeatherMap service
     * @return the API key as String
     */
    public String getKey() { return key; }

    /**
     * Returns the endpoint URL for OpenWeatherMap API requests
     * @return the endpoint URL as String
     */
    public String getEndpoint() { return endpoint; }

    /**
     * Returns the operation mode of the SDK (ON_DEMAND or POLLING)
     * @return the current operation mode
     */
    public WeatherSDKMode getMode() { return mode; }

    /**
     * Returns the polling interval used in POLLING mode
     * @return the polling interval as Duration
     */
    public Duration getPollingInterval() { return pollingInterval; }

    /**
     * Builder for configuring WeatherSDK instances with fluent interface.
     * Ensures required parameters are provided and applies sensible defaults.
     */
    public static class Builder {
        private String key;
        /**
         * Maximum number of cities to cache simultaneously.
         * When limit is reached, least recently used cities are evicted.
         */
        private Integer maxSize = 10;
        private String endpoint = "https://api.openweathermap.org/data/2.5/weather";
        private WeatherSDKMode mode = WeatherSDKMode.ON_DEMAND;
        private Duration pollingInterval = Duration.ofMinutes(10);
        private WeatherCache cache;
        private ScheduledExecutorService scheduler;

        /**
         * Constructs a new Builder with the required API key
         * @param key the OpenWeatherMap API key (required)
         * @throws IllegalArgumentException if the API key is null or blank
         */
        public Builder(String key) {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("API key cannot be null or empty");
            }
            this.key = key;
        }

        /**
         * Sets the custom endpoint URL for API requests
         * @param endpoint the endpoint URL (default: "https://api.openweathermap.org/data/2.5/weather")
         * @return the current Builder instance for method chaining
         */
        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        /**
         * Sets the operation mode for the SDK
         * @param mode the operation mode (default: ON_DEMAND)
         * @return the current Builder instance for method chaining
         */
        public Builder mode(WeatherSDKMode mode) {
            this.mode = mode;
            return this;
        }

        /**
         * Sets the polling interval for POLLING mode
         * @param pollingInterval the interval between polls (default: 10 minutes)
         * @return the current Builder instance for method chaining
         */
        public Builder pollingInterval(Duration pollingInterval) {
            this.pollingInterval = pollingInterval;
            return this;
        }

        /**
         * Sets the maximum size for stored data
         * @param maxSize the number of DTO instanse remember too (default: 10)
         * @return the current Builder instance for method chaining
         */
        public Builder maxSize(Integer size) {
            this.maxSize = size;
            return this;
        }

       /**
         * Builds and validates a new WeatherSDK instance with the configured parameters
         * @return a new configured WeatherSDK instance
         * @throws IllegalStateException if any validation checks fail
         */
        public WeatherSDK build() {
            this.cache = new WeatherCache(this.maxSize, this.pollingInterval);
            if (this.mode == WeatherSDKMode.POLLING) {
                this.scheduler = Executors.newScheduledThreadPool(1);
            }
            WeatherSDK res = new WeatherSDK(this);
            validate(res);
            return res;
        }

        /**
         * Validates the WeatherSDK configuration parameters
         * @param sdk the WeatherSDK instance to validate
         * @throws IllegalStateException if endpoint is invalid or polling interval is negative in POLLING mode
         */
        private void validate(WeatherSDK sdk) {
            if (sdk.endpoint == null || sdk.endpoint.isBlank()) {
                throw new IllegalStateException("Endpoint cannot be null or empty");
            }
            if (!sdk.endpoint.startsWith("http://") && !sdk.endpoint.startsWith("https://")) {
                throw new IllegalStateException("Endpoint must be a valid URL");
            }
            if (sdk.mode == WeatherSDKMode.POLLING && sdk.pollingInterval.isNegative()) {
                throw new IllegalStateException("Polling interval cannot be negative in POLLING mode");
            }
        }
    }

    /**
     * Gets current weather data for specified city
     * @param cityName the name of the city
     * @return weather data in JSON format
     * @throws WeatherSDKException if request fails or city not found
     */
    public WeatherResponse getWeatherObj(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            throw new IllegalArgumentException("City name cannot be null or empty");
        }
        
        try {
            String url = String.format("%s?appid=%s&q=%s", 
                endpoint,
                URLEncoder.encode(key, StandardCharsets.UTF_8),
                URLEncoder.encode(cityName, StandardCharsets.UTF_8)
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                    WeatherResponse weatherResponse = objectMapper.readValue(response.body(), WeatherResponse.class);
                    return weatherResponse;
            } else {
                throw new RuntimeException("Weather API request failed. Status: " + 
                    response.statusCode() + ", Response: " + response.body());
            }
            
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Request was interrupted", e);
            } else {
                throw new RuntimeException("Network error while fetching weather data: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Gets current weather data for specified city in JSON format.
     * Implements different caching strategies based on operation mode:
     * - ON_DEMAND: Lazy polling with TTL-based cache validation
     * - POLLING: Immediate response from pre-refreshed cache
     * 
     * @param cityName the name of the city to query
     * @return weather data as JSON string
     * @throws RuntimeException if city not found or API request fails
     */
    public String getWeather(String cityName) {
        WeatherResponse cached = cache.get(cityName);
        if (cached == null) {
            WeatherResponse fresh = getWeatherObj(cityName);
            cache.put(cityName, fresh);
            return toJson(fresh);
        }
        return toJson(cached);
    }

    /**
     * Serializes object to JSON string
     * @param object the object to serialize
     * @return JSON string representation
     * @throws RuntimeException if serialization fails
     */
    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
    
    /**
     * Starts background polling agent for POLLING mode
     */
    private void startPollingAgent() {
        if (mode == WeatherSDKMode.POLLING) {
            scheduler.scheduleAtFixedRate(() -> {
                System.out.println("Polling agent: updating all cached cities...");
        
                for (String city : cache.getAllCities()) {
                    try {
                        WeatherResponse fresh = getWeatherObj(city);
                        cache.put(city, fresh);
                        System.out.println("Updated: " + city);
                    } catch (Exception e) {
                        System.err.println("Failed to update " + city + ": " + e.getMessage());
                    }
                }
            }, 0, pollingInterval.toMinutes(), TimeUnit.MINUTES);
        }
    }
    
    /**
     * Gracefully shuts down background polling agent (if enabled) and releases resources.
     * Should be called when SDK is no longer needed, especially in POLLING mode.
     * No-op if scheduler is not initialized (ON_DEMAND mode).
     */
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    /**
     * Clears all cached weather data.
     * Useful for freeing memory or resetting SDK state.
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Constructs a new WeatherSDK instance with configuration from Builder.
     * Automatically starts polling agent if POLLING mode is enabled.
     * 
     * @param builder the Builder instance containing all configuration parameters
     */
    WeatherSDK(Builder builder) {
        this.key = builder.key;
        this.endpoint = builder.endpoint;
        this.mode = builder.mode;
        this.cache = builder.cache;
        this.pollingInterval = builder.pollingInterval;
        this.scheduler = builder.scheduler;

        if (this.mode == WeatherSDKMode.POLLING) {
            startPollingAgent();
        }
    }
}
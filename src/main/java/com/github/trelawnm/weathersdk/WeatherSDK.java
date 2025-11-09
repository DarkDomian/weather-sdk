package com.github.trelawnm.weathersdk;

import java.time.Duration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

/**
 * Weather SDK - lightweight Java library for OpenWeatherMap API.
 * Provides a simple interface to retrieve weather data in JSON format.
 * 
 * <p><b>Usage Example:</b>
 * <pre>
 * {@code
 * WeatherSDK sdk = new WeatherSDK.Builder("your-api-key")
 *     .endpoint("https://api.openweathermap.org/data/2.5/")
 *     .mode(WeatherSDKMode.ON_DEMAND)
 *     .build();
 * String weatherJson = sdk.getWeather("London");
 * System.out.println(weatherJson);
 * }
 * </pre>
 * @author trelawnm
 * @version 1.0
 * @see <a href="https://openweathermap.org/api">OpenWeatherMap API</a>
 */
public class WeatherSDK {
    private final String key;
    private final String endpoint;
    private final WeatherSDKMode mode;
    private final Duration pollingInterval;

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
     * Builder class for creating configured instances of WeatherSDK.
     * Implements the Builder pattern for flexible object creation.
     */
    public static class Builder {
        // required
        private String key;
        // unrequired
        private String endpoint = "https://api.openweathermap.org/data/2.5/weather";
        private WeatherSDKMode mode = WeatherSDKMode.ON_DEMAND;
        private Duration pollingInterval = Duration.ofMinutes(10);

        /**
         * Constructs a new Builder with the required API key
         *  @param key the OpenWeatherMap API key (required)
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
         * Builds and validates a new WeatherSDK instance with the configured parameters
         * @return a new configured WeatherSDK instance
         * @throws IllegalStateException if any validation checks fail
         */
        public WeatherSDK build() {
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
    public String getWeather(String cityName) {
        // Базовая валидация входных параметров
        if (cityName == null || cityName.isBlank()) {
            throw new IllegalArgumentException("City name cannot be null or empty");
        }
        
        try {
            // Формируем URL для запроса
            String url = String.format("%s?q=%s&appid=%s", 
                endpoint, 
                java.net.URLEncoder.encode(cityName, java.nio.charset.StandardCharsets.UTF_8),
                key);
            
            // Создаем HTTP клиент если еще не создан
            HttpClient client = HttpClient.newHttpClient();
            
            // Создаем запрос
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            // Выполняем запрос
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Проверяем статус ответа
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                // Бросаем стандартное исключение с информацией об ошибке
                throw new RuntimeException("Weather API request failed. Status: " + 
                    response.statusCode() + ", Response: " + response.body());
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Network error while fetching weather data: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Восстанавливаем флаг прерывания
            throw new RuntimeException("Request was interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Constructs a new WeatherSDK instance from the Builder
     * @param builder the Builder containing configuration parameters
     */
    WeatherSDK(Builder builder) {
        this.key = builder.key;
        this.endpoint = builder.endpoint;
        this.mode = builder.mode;
        this.pollingInterval = builder.pollingInterval;
    }

}
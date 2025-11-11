package com.github.trelawnm.weathersdk;

import com.github.trelawnm.weathersdk.model.WeatherResponse;

/**
 * Immutable container for cached weather data with metadata.
 * <p>
 * This class represents a thread-safe cached weather entry that stores both
 * the weather data and timing information for cache management purposes.
 * The class is immutable to ensure thread safety in concurrent environments.
 * </p>
 * 
 * <p><b>Usage Example:</b>
 * <pre>
 * {@code
 * CachedWeatherData cachedData = new CachedWeatherData(
 *     "London",
 *     weatherResponse,
 *     System.currentTimeMillis()
 * );
 * }
 * </pre>
 * 
 * @author trelawnm
 * @version 1.0
 * @see WeatherResponse
 * @see WeatherCache
 */
public class CachedWeatherData {
    private final String cityName;
    private final WeatherResponse weatherData;
    private final Long timestamp;

    /**
     * Returns the city name for which weather data is cached.
     * 
     * @return the city name as String, never null
     */
    public String getCity() {return this.cityName;}
    /**
     * Returns the cached weather data response.
     * 
     * @return the WeatherResponse object containing weather information, never null
     */
    public WeatherResponse getWeather() {return this.weatherData;}
    /**
     * Returns the timestamp when this weather data was last updated from the API.
     * 
     * @return the update timestamp in milliseconds since epoch
     */
    public Long getTimestamp() {return this.timestamp;}

    /**
     * Constructs a new immutable cached weather data entry.
     * 
     * @param cityName the name of the city, cannot be null or empty
     * @param weatherData the weather response data, cannot be null
     * @param timestamp the time when data was updated from API in milliseconds
     * @param accessTime the time when data was last accessed in milliseconds
     * @throws IllegalArgumentException if cityName is null/empty or weatherData is null
     */
    public CachedWeatherData(String cityName, WeatherResponse weatherData, Long timestamp) {
        if (cityName == null || cityName.isBlank()) {
            throw new IllegalArgumentException("City name cannot be null or empty");
        }
        if (weatherData == null) {
            throw new IllegalArgumentException("Weather data cannot be null");
        }

        this.cityName = cityName;
        this.weatherData = weatherData;
        this.timestamp = timestamp;
    }
}
package com.github.trelawnm.weathersdk;

import com.github.trelawnm.weathersdk.model.WeatherResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Deque;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Set;


/**
 * Thread-safe LRU (Least Recently Used) cache for weather data with TTL support.
 * <p>
 * This cache maintains weather data for up to {@code maxSize} cities, automatically
 * evicting the least recently used entries when capacity is reached. Each cached
 * entry has a time-to-live (TTL) after which it is considered stale and requires
 * refresh from the API.
 * </p>
 * 
 * <p><b>Features:</b>
 * <ul>
 *   <li>Fixed size with LRU eviction policy</li>
 *   <li>TTL-based data freshness checking</li>
 *   <li>O(1) time complexity for get and put operations</li>
 *   <li>Thread-safe for concurrent access</li>
 * </ul>
 * </p>
 *
 * <p><b>Usage Example:</b>
 * <pre>
 * {@code
 * WeatherCache cache = new WeatherCache(10, Duration.ofMinutes(10));
 * 
 * // Store weather data
 * cache.put("London", weatherResponse);
 * 
 * // Retrieve weather data (updates access order)
 * WeatherResponse data = cache.get("London");
 * }
 * </pre>
 * 
 * @author trelawnm
 * @version 1.0
 * @see CachedWeatherData
 * @see WeatherResponse
 */
public class WeatherCache {
    private final int maxSize;
    private final Duration ttl;
    private final Map<String, CachedWeatherData> cache;
    private final Deque<String> accessOrder;
    
    /**
     * Internal DTO for cached weather data with timestamp.
     */
    private record CachedWeatherData(
        WeatherResponse weatherData,
        Long timestamp
    ){
        public CachedWeatherData {
            if (weatherData == null) {
                throw new IllegalArgumentException("Weather data cannot be null");
            }
        }
    }

    /**
     * Constructs a new weather cache with specified capacity and TTL.
     *
     * @param maxSize the maximum number of cities to cache (must be positive)
     * @param ttl the time-to-live duration for cached entries (cannot be null)
     * @throws IllegalArgumentException if maxSize is not positive or ttl is null
     */
    public WeatherCache(int maxSize, Duration ttl) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("Max size of cache can't be less then 1");
        }

        if (!ttl.isPositive()) {
            throw new IllegalArgumentException("Time to live variable can't be negative or null");
        }

        this.maxSize = maxSize;
        this.ttl = ttl;
        this.cache = new HashMap<>();
        this.accessOrder = new LinkedList<>();
    }
    
    /**
     * Retrieves weather data for the specified city.
     * <p>
     * If the data exists and is not stale, it updates the access order
     * and returns the weather data. If the data is stale, it is removed
     * from the cache and null is returned.
     * </p>
     *
     * @param city the name of the city to look up
     * @return the cached weather data, or null if not found or stale
     */
    public WeatherResponse get(String city) {
        CachedWeatherData cached = cache.get(city);
        if (cached == null) return null;
        
        if (isStale(cached)) {
            cache.remove(city);
            return null;
        }
        
        updateAccessOrder(city);
        return cached.weatherData();
    }
    
    /**
     * Stores weather data for the specified city in the cache.
     * <p>
     * If the cache has reached maximum capacity, the least recently used
     * city is automatically evicted. The new entry is added with current
     * timestamps for creation time.
     * </p>
     *
     * @param city the name of the city to cache
     * @param weatherData the weather data to store
     * @throws IllegalArgumentException if city is null or weatherData is null
     */
    public void put(String city, WeatherResponse weatherData) {
        evictIfNeeded();

        CachedWeatherData newData = new CachedWeatherData(
            weatherData, 
            System.currentTimeMillis()
        );

        cache.put(city, newData);
        updateAccessOrder(city);
    }

    /**
     * Checks if the cached data has expired based on TTL.
     *
     * @param data the cached weather data to check
     * @return true if the data is stale and should be refreshed
     */
    private boolean isStale(CachedWeatherData data) {
        long currentTime = System.currentTimeMillis();
        long dataAge = currentTime - data.timestamp();
        return dataAge > ttl.toMillis();
    }
    
    /**
     * Evicts the least recently used entry if cache is at capacity.
     */
    private void evictIfNeeded() {
    if (cache.size() >= maxSize) {
            String oldestCity = accessOrder.pollFirst();
            if (oldestCity != null) {
                cache.remove(oldestCity);
            }
        }
    }
    
    /**
     * Updates the access order by moving the city to the most recent position.
     *
     * @param city the city to mark as recently accessed
     */
    private void updateAccessOrder(String city) {
        accessOrder.remove(city);
        accessOrder.addLast(city);
    }
    
    /**
     * Returns the current number of cities in the cache.
     *
     * @return the number of cached cities
     */
    public int size() {
        return cache.size();
    }

    /**
     * Returns all cities currently in cache
     * @return set of city names
     */
    public Set<String> getAllCities() {
        return new HashSet<>(cache.keySet());
    }
    
    /**
     * Removes all entries from the cache.
     */
    public void clear() {
        cache.clear();
        accessOrder.clear();
    }
}
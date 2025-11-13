package com.github.trelawnm.weathersdk;

import org.junit.jupiter.api.Test;
import io.github.cdimascio.dotenv.Dotenv;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

/**
 * Tests for WeatherSDK Builder and configuration
 */
class WeatherSDKBuilderTest {
    private static final String VALID_API_KEY = Dotenv.load().get("OPENWEATHER_API_KEY");
    
    @Test
    void builder_WithValidParameters_ShouldCreateInstance() {
        // Act
        WeatherSDK sdk = new WeatherSDK.Builder(VALID_API_KEY)
            .endpoint("https://api.openweathermap.org/another/endpoint")
            .mode(WeatherSDKMode.POLLING)
            .pollingInterval(Duration.ofMinutes(5))
            .build();
        
        // Assert
        assertNotNull(sdk);
        assertEquals(VALID_API_KEY, sdk.getKey());
        assertEquals("https://api.openweathermap.org/another/endpoint", sdk.getEndpoint());
        assertEquals(WeatherSDKMode.POLLING, sdk.getMode());
        assertEquals(Duration.ofMinutes(5), sdk.getPollingInterval());
    }
    
    @Test
    void builder_WithDefaultParameters_ShouldUseDefaults() {
        // Act
        WeatherSDK sdk = new WeatherSDK.Builder(VALID_API_KEY).build();
        
        // Assert
        assertNotNull(sdk);
        assertEquals(VALID_API_KEY, sdk.getKey());
        assertEquals("https://api.openweathermap.org/data/2.5/weather", sdk.getEndpoint());
        assertEquals(WeatherSDKMode.ON_DEMAND, sdk.getMode());
        assertEquals(Duration.ofMinutes(10), sdk.getPollingInterval());
    }
    
    @Test
    void builder_WithInvalidApiKey_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> new WeatherSDK.Builder(null));
        
        assertEquals("API key cannot be null or empty", exception.getMessage());
    }
}
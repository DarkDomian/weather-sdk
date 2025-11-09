package com.github.trelawnm.weathersdk;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Integration tests for WeatherSDK
 * These tests make actual API calls to OpenWeatherMap
 */
class WeatherSDKIntegrationTest {
    
    private static WeatherSDK sdk;
    private static String apiKey;

    @BeforeAll
    public static void setUp() {
        Dotenv dotenv = Dotenv.load();
        apiKey = dotenv.get("OPENWEATHER_API_KEY");
        
        if (apiKey == null) {
            throw new IllegalStateException("OPENWEATHER_API_KEY not found in .env file");
        }

        sdk = new WeatherSDK.Builder(apiKey)
        .endpoint("https://api.openweathermap.org/data/2.5/weather")
        .mode(WeatherSDKMode.ON_DEMAND)
        .build();
    }

    @Test
    @Timeout(30)
    void getWeather_MultipleMajorCities_ShouldReturnValidJson() {
        // Arrange
        String[] cities = {"London", "Paris", "Berlin", "Tokyo", "New York", "Buenos Aires", "Mexico City", "Kuala Lumpur"};
        
        for (String city : cities) {
            // Act
            String result = assertDoesNotThrow(() -> sdk.getWeather(city),
                "Should not throw exception for city: " + city);
            
            // Assert
            assertNotNull(result, "Response should not be null for city: " + city);
            assertFalse(result.isEmpty(), "Response should not be empty for city: " + city);
            assertTrue(result.contains("weather"), 
                      "Response should contain weather data for city: " + city);
            
            System.out.println("=== Weather for " + city + " ===");
            System.out.println(result.substring(0, Math.min(200, result.length())) + "...");
            System.out.println("=== End of response ===\n");
        }
    }
    
    @Test
    @Timeout(10)
    void getWeather_NonExistentCity_ShouldThrowException() {
        // Arrange
        String nonExistentCity = "NonexistentCity";
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, 
            () -> sdk.getWeather(nonExistentCity));
        
        String message = exception.getMessage().toLowerCase();
        assertTrue(message.contains("city") || message.contains("not found") || message.contains("404"),
              "Exception should indicate city not found. Actual: " + exception.getMessage());
        
        System.out.println("=== Expected error for non-existent city ===");
        System.out.println(exception.getMessage());
        System.out.println("=== End of error message ===\n");
    }
    
    @Test
    @Timeout(10)
    void getWeather_EmptyCity_ShouldThrowIllegalArgumentException() {
        // Arrange
        String emptyCity = "";
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> sdk.getWeather(emptyCity));
        
        assertEquals("City name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    @Timeout(10)
    void getWeather_NullCity_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> sdk.getWeather(null));
        
        assertEquals("City name cannot be null or empty", exception.getMessage());
    }
}
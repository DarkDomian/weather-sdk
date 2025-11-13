# Weather SDK

[![Release](https://img.shields.io/badge/release-v1.1.0-blue.svg)](https://github.com/trelawnm/weather-sdk/releases)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-8%2B-orange.svg)](https://java.com)

A lightweight Java SDK for seamless integration with the OpenWeatherMap API. Built for efficiency and ease of use.

![weather badge image](misc/badge.jpg)

## Quick Start

### Installation
```xml
<dependency>
    <groupId>com.github.trelawnm.weathersdk</groupId>
    <artifactId>weather-sdk</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Basic Usage
```java
// Initialize SDK
WeatherSDK sdk = new WeatherSDK.Builder("your-api-key")
     .mode(WeatherSDKMode.POLLING)
     .build();

// Get weather data
String weatherJson = sdk.getWeather("London");
WeatherResponse weather = sdk.getWeatherObj("Paris");

// Cleanup
sdk.shutdown();
sdk.clearCache();
```

## Key Features

- **Smart Caching** - Reduces API calls with configurable TTL
- **Dual Operation Modes** - On-demand or background polling
- **Free Tier Friendly** - Optimized for OpenWeatherMap's free plan
- **Thread-Safe** - Built for concurrent applications

## Documentation

- [Release Notes](https://github.com/trelawnm/weather-sdk/releases)
- [Full Documentation](https://github.com/trelawnm/weather-sdk)
- [OpenWeatherMap API](https://openweathermap.org/api)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Get your free API key at [OpenWeatherMap.org](https://openweathermap.org/api)** 🌤️
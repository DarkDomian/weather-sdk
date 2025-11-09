# Weather SDK

##  Setup for Development

### 1. Get Your API Key
- Register at [OpenWeatherMap](https://openweathermap.org/api)
- Get your free API key

### 2. Local Setup
```bash
# Copy the example file
cp .env.example .env

# Edit .env and add your API key
echo "OPENWEATHER_API_KEY=your_actual_key_here" > .env
```

### 3. Run Tests
```bash
# Run all tests
mvn test

# Run only integration tests
mvn test -Dtest=WeatherSDKIntegrationTest
```

## Environment Variables

### Required
- `OPENWEATHER_API_KEY` - Your OpenWeatherMap API key

### Optional
- `OPENWEATHER_ENDPOINT` - Custom API endpoint (default: OpenWeatherMap)
package com.github.trelawnm.weathersdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;

/**
 * Weather data response for SDK users
 * Contains simplified weather information
 * 
 * Example JSON structure:
 * {
 *   "weather": {"main": "Rain", "description": "light rain"},
 *   "main": {"temp": 15.5, "feels_like": 14.8},
 *   "visibility": 10000,
 *   "wind": {"speed": 3.5},
 *   "dt": 1643671200,
 *   "sys": {"sunrise": 1643671200, "sunset": 1643709600},
 *   "timezone": 10800,
 *   "name": "London"
 * }
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class WeatherResponse {
    /** Weather condition information */
    @JsonProperty("weather")
    @JsonDeserialize(using = WeatherArrayDeserializer.class)
    private WeatherInfo weather;

    /** Temperature measurements in Kelvin */
    @JsonProperty("main")
    private Temperature temperature;

    /** Visibility in meters */
    private Integer visibility;

    /** Wind information */
    private WindInfo wind;
    
    /** Data calculation time, unix timestamp UTC */
    @JsonProperty("dt")
    private Long datetime;

    /** System information (sunrise/sunset) */
    private SystemInfo sys;

    /** Shift in seconds from UTC */
    private Integer timezone;

    /** City name */
    private String name;
    
    /** Basic information about weather conditions */
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class WeatherInfo {
        /** Main weather condition (Rain, Snow, Clear, etc.) */
        private String main;
        /** Detailed weather description */
        private String description;

        /** @return main weather condition */
        public String getMain() {return main;}
        /** @param main main weather condition */
        public void setMain(String main) {this.main = main;}

        /** @return detailed weather description */
        public String getDescription() {return description;}
        /** @param description detailed weather description */
        public void setDescription(String description) {this.description = description;}
    }

    /** Temperature information in Kelvin */
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Temperature {
        /** Temperature in Kelvin */
        private Double temp;
        /** Human perception of temperature in Kelvin */
        @JsonProperty("feels_like")
        private Double feelsLike;

        /** @return temperature in Kelvin */
        public Double getTemp() {return temp;}
        /** @param temp temperature in Kelvin */
        public void setTemp(Double temp) {this.temp = temp;}

        /** @return perceived temperature in Kelvin */
        public Double getFeelsLike() {return feelsLike;}
        /** @param feelsLike perceived temperature in Kelvin */
        public void setFeelsLike(Double feelsLike) {this.feelsLike = feelsLike;}
    }

    /** Wind measurement data */
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class WindInfo {
        /** Wind speed in meters per second */
        private Double speed;

        /** @return wind speed in m/s */
        public Double getSpeed() {return speed;}
        /** @param speed wind speed in m/s */
        public void setSpeed(Double speed) {this.speed = speed;}
    }

    /** Sunrise and sunset timing information */
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class SystemInfo {
        /** Sunrise time as unix timestamp */
        private Long sunrise;
        /** Sunset time as unix timestamp */
        private Long sunset;

        /** @return sunrise unix timestamp */
        public Long getSunrise() {return sunrise;}
        /** @param sunrise sunrise unix timestamp */
        public void setSunrise(Long sunrise) {this.sunrise = sunrise;}

        /** @return sunset unix timestamp */
        public Long getSunset() {return sunset;}
        /** @param sunset sunset unix timestamp */
        public void setSunset(Long sunset) {this.sunset = sunset;}
    }

    /** @return weather condition information */
    public WeatherInfo getWeather() {return weather;}
    /** @param weather weather condition information */
    public void setWeather(WeatherInfo weather) {this.weather = weather;}

    /** @return temperature measurements in Kelvin */
    public Temperature getTemperature() {return temperature;}
    /** @param temperature temperature measurements in Kelvin */
    public void setTemperature(Temperature temperature) {this.temperature = temperature;}

    /** @return visibility in meters */
    public Integer getVisibility() {return visibility;}
    /** @param visibility visibility in meters */
    public void setVisibility(Integer visibility) {this.visibility = visibility;}

    /** @return wind information */
    public WindInfo getWind() {return wind;}
    /** @param wind wind information */
    public void setWind(WindInfo wind) {this.wind = wind;}

    /** @return data calculation time as unix timestamp */
    public Long getDatetime() {return datetime;}
    /** @param datetime data calculation time as unix timestamp */
    public void setDatetime(Long datetime) {this.datetime = datetime;}

    /** @return system information (sunrise/sunset) */
    public SystemInfo getSystem() {return sys;}
    /** @param sys system information (sunrise/sunset) */
    public void setSystem(SystemInfo sys) {this.sys = sys;}

    /** @return timezone shift in seconds from UTC */
    public Integer getTimezone() {return timezone;}
    /** @param timezone timezone shift in seconds from UTC */
    public void setTimezone(Integer timezone) {this.timezone = timezone;}

    /** @return city name */
    public String getName() {return name;}
    /** @param name city name */
    public void setName(String name) {this.name = name;}

    /** Custom deserializer for weather array to object conversion */
    public static class WeatherArrayDeserializer extends JsonDeserializer<WeatherInfo> {
        /** 
         * Deserializes weather array taking first element
         * @return first WeatherInfo from array or null
         */
        @Override
        public WeatherInfo deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException {
            JsonNode node = p.readValueAsTree();
            if (node.isArray() && node.size() > 0) {
                // Take first element from array
                return p.getCodec().treeToValue(node.get(0), WeatherInfo.class);
            }
            return null;
        }
    }
}
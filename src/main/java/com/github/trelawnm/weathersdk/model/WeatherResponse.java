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
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class WeatherResponse {
    /** Weather condition information */
    @JsonProperty("weather")
    @JsonDeserialize(using = WeatherArrayDeserializer.class)
    private WeatherInfo weather;

    /** Temperature measurements */
    @JsonProperty("main")
    private Temperature temperature;

    /** Visibility in meters */
    private Integer visibility;

     /** Wind information */
    private WindInfo wind;
    
    /** Data calculation time, unix, UTC */
    @JsonProperty("dt")
    private Long datetime;

    /** System information (sunrise/sunset) */
    private SystemInfo sys;

     /** Shift in seconds from UTC */
    private Integer timezone;

    /** City name */
    private String name;
    
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class WeatherInfo {
        private String main;
        private String description;

        public String getMain() {return main;}
        public void setMain(String main) {this.main = main;}

        public String getDescription() {return description;}
        public void setDescription(String description) {this.description = description;}
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Temperature {
        private Double temp;
        @JsonProperty("feels_like")
        private Double feelsLike;

        public Double getTemp() {return temp;}
        public void setTemp(Double temp) {this.temp = temp;}

        public Double getFeelsLike() {return feelsLike;}
        public void setFeelsLike(Double feelsLike) {this.feelsLike = feelsLike;}
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class WindInfo {
        private Double speed;

        public Double getSpeed() {return speed;}
        public void setSpeed(Double speed) {this.speed = speed;}
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class SystemInfo {
        private Long sunrise;
        private Long sunset;

        public Long getSunrise() {return sunrise;}
        public void setSunrise(Long sunrise) {this.sunrise = sunrise;}

        public Long getSunset() {return sunset;}
        public void setSunset(Long sunset) {this.sunset = sunset;}
    }

    public WeatherInfo getWeather() {return weather;}
    public void setWeather(WeatherInfo weather) {this.weather = weather;}

    public Temperature getTemperature() {return temperature;}
    public void setTemperature(Temperature temperature) {this.temperature = temperature;}

    public Integer getVisibility() {return visibility;}
    public void setVisibility(Integer visibility) {this.visibility = visibility;}

    public WindInfo getWind() {return wind;}
    public void setWind(WindInfo wind) {this.wind = wind;}

    public Long getDatetime() {return datetime;}
    public void setDatetime(Long datetime) {this.datetime = datetime;}

    public SystemInfo getSystem() {return sys;}
    public void setSystem(SystemInfo sys) {this.sys = sys;}

    public Integer getTimezone() {return timezone;}
    public void setTimezone(Integer timezone) {this.timezone = timezone;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public static class WeatherArrayDeserializer extends JsonDeserializer<WeatherInfo> {
        @Override
        public WeatherInfo deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException {
            JsonNode node = p.readValueAsTree();
            if (node.isArray() && node.size() > 0) {
                // Берем первый элемент массива
                return p.getCodec().treeToValue(node.get(0), WeatherInfo.class);
            }
            return null;
        }
    }
}
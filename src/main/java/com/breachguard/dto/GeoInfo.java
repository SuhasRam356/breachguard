package com.breachguard.dto;

public class GeoInfo {
    private String country = "Unknown";
    private String city = "Unknown";
    private Double lat;
    private Double lng;

    public GeoInfo() {}

    public GeoInfo(String country, String city, Double lat, Double lng) {
        this.country = country;
        this.city = city;
        this.lat = lat;
        this.lng = lng;
    }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
}

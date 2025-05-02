package com.example.localexplorer.data.remote.geoapify;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Classe pour déserialiser la réponse de l'API Geoapify
 */
public class PlacesResponse {
    
    @SerializedName("features")
    private List<Feature> features;
    
    public List<Feature> getFeatures() {
        return features;
    }
    
    public static class Feature {
        @SerializedName("properties")
        private Properties properties;
        
        @SerializedName("geometry")
        private Geometry geometry;
        
        public Properties getProperties() {
            return properties;
        }
        
        public Geometry getGeometry() {
            return geometry;
        }
    }
    
    public static class Properties {
        @SerializedName("place_id")
        private String placeId;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("formatted")
        private String address;
        
        @SerializedName("categories")
        private List<String> categories;
        
        @SerializedName("rating")
        private double rating;
        
        public String getPlaceId() {
            return placeId;
        }
        
        public String getName() {
            return name;
        }
        
        public String getAddress() {
            return address;
        }
        
        public List<String> getCategories() {
            return categories;
        }
        
        public double getRating() {
            return rating;
        }
    }
    
    public static class Geometry {
        @SerializedName("coordinates")
        private double[] coordinates;
        
        public double getLongitude() {
            return coordinates != null && coordinates.length > 0 ? coordinates[0] : 0;
        }
        
        public double getLatitude() {
            return coordinates != null && coordinates.length > 1 ? coordinates[1] : 0;
        }
    }
} 
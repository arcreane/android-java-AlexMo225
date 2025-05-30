package com.example.localexplorer.data.remote.geoapify;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Classe de réponse pour l'API Place Details de Geoapify
 * 
 * NOTE: Cette classe n'est actuellement pas utilisée dans l'application.
 * L'application utilise la classe PlaceDetailsService avec son propre parsing JSON
 * pour récupérer et traiter les détails des restaurants.
 * 
 * Cette classe est conservée comme référence pour une implémentation future
 * qui pourrait utiliser Retrofit pour les appels API de détails.
 */
public class PlaceDetailsResponse {
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("features")
    private List<Feature> features;
    
    public String getType() {
        return type;
    }
    
    public List<Feature> getFeatures() {
        return features;
    }
    
    public static class Feature {
        @SerializedName("type")
        private String type;
        
        @SerializedName("properties")
        private Properties properties;
        
        public String getType() {
            return type;
        }
        
        public Properties getProperties() {
            return properties;
        }
    }
    
    public static class Properties {
        @SerializedName("name")
        private String name;
        
        @SerializedName("formatted")
        private String formatted;
        
        @SerializedName("opening_hours")
        private Map<String, OpeningHour> openingHours;
        
        @SerializedName("price_level")
        private String priceLevel;
        
        @SerializedName("contact")
        private Contact contact;
        
        public String getName() {
            return name;
        }
        
        public String getFormatted() {
            return formatted;
        }
        
        public Map<String, OpeningHour> getOpeningHours() {
            return openingHours;
        }
        
        public String getPriceLevel() {
            return priceLevel;
        }
        
        public Contact getContact() {
            return contact;
        }
    }
    
    public static class OpeningHour {
        @SerializedName("open")
        private String open;
        
        @SerializedName("close")
        private String close;
        
        public String getOpen() {
            return open;
        }
        
        public String getClose() {
            return close;
        }
    }
    
    public static class Contact {
        @SerializedName("phone")
        private String phone;
        
        @SerializedName("website")
        private String website;
        
        public String getPhone() {
            return phone;
        }
        
        public String getWebsite() {
            return website;
        }
    }
} 
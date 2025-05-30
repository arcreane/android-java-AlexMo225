package com.example.localexplorer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe modèle pour représenter les critères de filtrage des restaurants
 */
public class Filter {
    
    private String priceLevel; // null, €, €€, €€€
    private List<String> cuisineTypes; // Types de cuisine sélectionnés
    private float minRating; // Note minimale (0.0 - 5.0)
    
    /**
     * Constructeur par défaut, initialise avec des valeurs par défaut
     */
    public Filter() {
        this.priceLevel = null; // Pas de filtre de prix
        this.cuisineTypes = new ArrayList<>(); // Pas de filtre de cuisine
        this.minRating = 0.0f; // Pas de filtre de note minimale
    }
    
    /**
     * Constructeur complet
     */
    public Filter(String priceLevel, List<String> cuisineTypes, float minRating) {
        this.priceLevel = priceLevel;
        this.cuisineTypes = cuisineTypes != null ? cuisineTypes : new ArrayList<>();
        this.minRating = minRating;
    }
    
    /**
     * Vérifie si un restaurant correspond aux critères de filtre
     */
    public boolean matches(Restaurant restaurant) {
        // Vérifier que le restaurant n'est pas null
        if (restaurant == null) {
            return false;
        }
        
        // Vérifier le prix
        if (priceLevel != null && !priceLevel.isEmpty()) {
            if (restaurant.getPriceLevel() == null || !restaurant.getPriceLevel().equals(priceLevel)) {
                return false;
            }
        }
        
        // Vérifier le type de cuisine
        if (!cuisineTypes.isEmpty()) {
            // Si le restaurant n'a pas de type de cuisine, il ne correspond pas
            if (restaurant.getPlaceType() == null) {
                return false;
            }
            
            // Vérification plus souple: recherche partielle et insensible à la casse
            boolean matchesCuisine = false;
            String restaurantType = restaurant.getPlaceType().toLowerCase();
            
            for (String cuisineType : cuisineTypes) {
                if (cuisineType == null) continue;
                
                String cuisine = cuisineType.toLowerCase();
                
                // Vérifier si le type du restaurant contient le type de cuisine
                // ou si le type de cuisine contient le type du restaurant
                if (restaurantType.contains(cuisine) || 
                    cuisine.contains(restaurantType) ||
                    normalizeTypeName(restaurantType).contains(normalizeTypeName(cuisine))) {
                    
                    matchesCuisine = true;
                    break;
                }
            }
            
            if (!matchesCuisine) {
                return false;
            }
        }
        
        // Vérifier la note minimale
        if (minRating > 0 && restaurant.getRating() < minRating) {
            return false;
        }
        
        // Si toutes les vérifications sont passées, le restaurant correspond aux critères
        return true;
    }
    
    /**
     * Normalise un nom de type de cuisine pour faciliter la comparaison
     * Exemple: "italian_restaurant" -> "italien"
     */
    private String normalizeTypeName(String typeName) {
        if (typeName == null) {
            return "";
        }
        
        // Supprimer les suffixes communs
        String normalized = typeName.replace("_restaurant", "")
                .replace("restaurant_", "")
                .replace("cuisine_", "")
                .replace("_cuisine", "");
        
        // Mapper certains noms en français
        if (normalized.contains("italian")) return "italien";
        if (normalized.contains("japan") || normalized.contains("sushi")) return "japonais";
        if (normalized.contains("india")) return "indien";
        if (normalized.contains("french")) return "français";
        if (normalized.contains("china") || normalized.contains("chinese")) return "chinois";
        if (normalized.contains("mexic")) return "mexicain";
        if (normalized.contains("thai")) return "thaïlandais";
        if (normalized.contains("america")) return "américain";
        if (normalized.contains("leban")) return "libanais";
        if (normalized.contains("veget") || normalized.contains("vegan")) return "végétarien";
        
        return normalized;
    }
    
    // Getters et Setters
    
    public String getPriceLevel() {
        return priceLevel;
    }
    
    public void setPriceLevel(String priceLevel) {
        this.priceLevel = priceLevel;
    }
    
    public List<String> getCuisineTypes() {
        return cuisineTypes;
    }
    
    public void setCuisineTypes(List<String> cuisineTypes) {
        this.cuisineTypes = cuisineTypes != null ? cuisineTypes : new ArrayList<>();
    }
    
    public float getMinRating() {
        return minRating;
    }
    
    public void setMinRating(float minRating) {
        this.minRating = minRating;
    }
    
    /**
     * Vérifie si le filtre est vide (aucun critère appliqué)
     */
    public boolean isEmpty() {
        return (priceLevel == null || priceLevel.isEmpty()) && 
               cuisineTypes.isEmpty() && 
               minRating == 0.0f;
    }
    
    /**
     * Réinitialise tous les filtres
     */
    public void reset() {
        priceLevel = null;
        cuisineTypes.clear();
        minRating = 0.0f;
    }
} 
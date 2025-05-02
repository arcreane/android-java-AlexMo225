package com.example.localexplorer.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.localexplorer.data.repository.RestaurantRepository;
import com.example.localexplorer.model.Restaurant;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel pour gérer les données des restaurants et leur présentation
 */
public class RestaurantViewModel extends AndroidViewModel {
    
    private final RestaurantRepository repository;
    
    // Valeurs par défaut pour Paris
    private final double DEFAULT_LATITUDE = 48.8575467;
    private final double DEFAULT_LONGITUDE = 2.351375;
    private final int DEFAULT_RADIUS = 1000; // 1km
    
    private final MutableLiveData<Double> currentLatitude = new MutableLiveData<>(DEFAULT_LATITUDE);
    private final MutableLiveData<Double> currentLongitude = new MutableLiveData<>(DEFAULT_LONGITUDE);
    private final MutableLiveData<Integer> searchRadius = new MutableLiveData<>(DEFAULT_RADIUS);
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Double> minRating = new MutableLiveData<>(0.0);
    
    private final MediatorLiveData<List<Restaurant>> filteredRestaurants = new MediatorLiveData<>();
    
    public RestaurantViewModel(@NonNull Application application) {
        super(application);
        repository = new RestaurantRepository(application);
        
        // Observer les changements dans les restaurants proches
        filteredRestaurants.addSource(repository.getNearbyRestaurants(), restaurants -> {
            applyFilters(restaurants, searchQuery.getValue(), minRating.getValue());
        });
        
        // Observer les changements dans la requête de recherche
        filteredRestaurants.addSource(searchQuery, query -> {
            applyFilters(repository.getNearbyRestaurants().getValue(), query, minRating.getValue());
        });
        
        // Observer les changements dans la note minimale
        filteredRestaurants.addSource(minRating, rating -> {
            applyFilters(repository.getNearbyRestaurants().getValue(), searchQuery.getValue(), rating);
        });
        
        // Recherche initiale si les coordonnées par défaut sont utilisées
        searchNearbyRestaurants();
    }
    
    /**
     * Applique les filtres aux restaurants
     */
    private void applyFilters(List<Restaurant> restaurants, String query, Double rating) {
        if (restaurants == null) {
            filteredRestaurants.setValue(new ArrayList<>());
            return;
        }
        
        List<Restaurant> result = new ArrayList<>();
        
        for (Restaurant restaurant : restaurants) {
            boolean matchesQuery = query == null || query.isEmpty() || 
                    (restaurant.getName() != null && restaurant.getName().toLowerCase().contains(query.toLowerCase())) ||
                    (restaurant.getPlaceType() != null && restaurant.getPlaceType().toLowerCase().contains(query.toLowerCase()));
            
            boolean matchesRating = rating == null || restaurant.getRating() >= rating;
            
            if (matchesQuery && matchesRating) {
                result.add(restaurant);
            }
        }
        
        filteredRestaurants.setValue(result);
    }
    
    /**
     * Recherche les restaurants à proximité
     */
    public void searchNearbyRestaurants() {
        Double latitude = currentLatitude.getValue();
        Double longitude = currentLongitude.getValue();
        Integer radius = searchRadius.getValue();
        
        if (latitude != null && longitude != null && radius != null) {
            repository.searchNearbyRestaurants(latitude, longitude, radius);
        }
    }
    
    /**
     * Met à jour la position actuelle
     */
    public void updateLocation(double latitude, double longitude) {
        currentLatitude.setValue(latitude);
        currentLongitude.setValue(longitude);
        searchNearbyRestaurants();
    }
    
    /**
     * Met à jour le rayon de recherche
     */
    public void updateSearchRadius(int radius) {
        searchRadius.setValue(radius);
        searchNearbyRestaurants();
    }
    
    /**
     * Met à jour la requête de recherche
     */
    public void updateSearchQuery(String query) {
        searchQuery.setValue(query);
    }
    
    /**
     * Met à jour la note minimale
     */
    public void updateMinRating(double rating) {
        minRating.setValue(rating);
    }
    
    /**
     * Bascule l'état favori d'un restaurant
     */
    public void toggleFavorite(Restaurant restaurant) {
        repository.toggleFavorite(restaurant);
    }
    
    /**
     * Obtient les restaurants filtrés
     */
    public LiveData<List<Restaurant>> getFilteredRestaurants() {
        return filteredRestaurants;
    }
    
    /**
     * Obtient les restaurants favoris
     */
    public LiveData<List<Restaurant>> getFavoriteRestaurants() {
        return repository.getFavoriteRestaurants();
    }
    
    /**
     * Obtient l'état de chargement
     */
    public LiveData<Boolean> getIsLoading() {
        return repository.getIsLoading();
    }
    
    /**
     * Obtient les erreurs éventuelles
     */
    public LiveData<String> getError() {
        return repository.getError();
    }
    
    /**
     * Obtient la latitude actuelle
     */
    public LiveData<Double> getCurrentLatitude() {
        return currentLatitude;
    }
    
    /**
     * Obtient la longitude actuelle
     */
    public LiveData<Double> getCurrentLongitude() {
        return currentLongitude;
    }
    
    /**
     * Nettoie les ressources lors de la destruction du ViewModel
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        // Libérer les ressources du repository
        repository.cleanup();
    }
} 
package com.example.localexplorer.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.localexplorer.data.repository.RestaurantRepository;
import com.example.localexplorer.model.Filter;
import com.example.localexplorer.model.Restaurant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    // Nouveau: filtre complet
    private final MutableLiveData<Filter> currentFilter = new MutableLiveData<>(new Filter());
    
    private final MediatorLiveData<List<Restaurant>> filteredRestaurants = new MediatorLiveData<>();
    
    public RestaurantViewModel(@NonNull Application application) {
        super(application);
        repository = new RestaurantRepository(application);
        
        // Observer les changements dans les restaurants proches
        filteredRestaurants.addSource(repository.getNearbyRestaurants(), restaurants -> {
            applyFilters(restaurants);
        });
        
        // Observer les changements dans la requête de recherche
        filteredRestaurants.addSource(searchQuery, query -> {
            applyFilters(repository.getNearbyRestaurants().getValue());
        });
        
        // Observer les changements dans le filtre
        filteredRestaurants.addSource(currentFilter, filter -> {
            applyFilters(repository.getNearbyRestaurants().getValue());
        });
        
        // Recherche initiale si les coordonnées par défaut sont utilisées
        searchNearbyRestaurants();
    }
    
    /**
     * Applique les filtres aux restaurants
     */
    private void applyFilters(List<Restaurant> restaurants) {
        if (restaurants == null) {
            filteredRestaurants.setValue(new ArrayList<>());
            Log.d("RestaurantViewModel", "Liste de restaurants nulle, pas de filtrage possible");
            return;
        }
        
        // Éliminer les doublons par ID avant d'appliquer les filtres
        restaurants = removeDuplicates(restaurants);
        
        List<Restaurant> result = new ArrayList<>();
        Filter filter = currentFilter.getValue();
        String query = searchQuery.getValue();
        
        // Loguer les critères de filtrage actuels
        logFilterCriteria(filter, query);
        
        int excludedByQuery = 0;
        int excludedByFilter = 0;
        
        for (Restaurant restaurant : restaurants) {
            // Vérifier si le restaurant est valide
            if (restaurant == null || restaurant.getId() == null || restaurant.getName() == null) {
                Log.d("RestaurantViewModel", "Restaurant ignoré car invalide");
                continue; // Ignorer les restaurants invalides
            }
            
            // Vérifier si le restaurant correspond à la requête de recherche
            boolean matchesQuery = query == null || query.isEmpty() || 
                    (restaurant.getName() != null && restaurant.getName().toLowerCase().contains(query.toLowerCase())) ||
                    (restaurant.getPlaceType() != null && restaurant.getPlaceType().toLowerCase().contains(query.toLowerCase()));
            
            if (!matchesQuery) {
                excludedByQuery++;
                continue;
            }
            
            // Vérifier si le restaurant correspond aux critères de filtre
            boolean matchesFilter = filter == null || filter.isEmpty() || filter.matches(restaurant);
            
            if (!matchesFilter) {
                excludedByFilter++;
                // Log détaillé pour les filtres par cuisine
                if (!filter.getCuisineTypes().isEmpty() && restaurant.getPlaceType() != null) {
                    Log.d("RestaurantViewModel", "Restaurant non retenu par filtre cuisine: " + 
                            restaurant.getName() + " (Type: " + restaurant.getPlaceType() + 
                            ") - Filtres actifs: " + filter.getCuisineTypes());
                }
                continue;
            }
            
            // Le restaurant passe tous les filtres
            result.add(restaurant);
        }
        
        // Logs pour le débogage
        Log.d("RestaurantViewModel", "Résultat du filtrage: " + result.size() + " restaurants sur " + restaurants.size() + " initiaux");
        Log.d("RestaurantViewModel", "Exclus par recherche: " + excludedByQuery + ", exclus par filtre: " + excludedByFilter);
        
        // S'assurer que les changements de liste sont bien notifiés en créant une nouvelle instance
        filteredRestaurants.setValue(new ArrayList<>(result));
    }
    
    /**
     * Loguer les critères de filtrage actuels pour le débogage
     */
    private void logFilterCriteria(Filter filter, String query) {
        StringBuilder logMessage = new StringBuilder("Critères de filtrage appliqués: ");
        
        if (query != null && !query.isEmpty()) {
            logMessage.append("Recherche='").append(query).append("', ");
        }
        
        if (filter != null) {
            if (filter.getPriceLevel() != null && !filter.getPriceLevel().isEmpty()) {
                logMessage.append("Prix='").append(filter.getPriceLevel()).append("', ");
            }
            
            if (!filter.getCuisineTypes().isEmpty()) {
                logMessage.append("Cuisines=").append(filter.getCuisineTypes()).append(", ");
            }
            
            if (filter.getMinRating() > 0) {
                logMessage.append("Note minimale=").append(filter.getMinRating());
            }
        }
        
        Log.d("RestaurantViewModel", logMessage.toString());
    }
    
    /**
     * Élimine les doublons dans la liste des restaurants en se basant sur l'ID
     */
    private List<Restaurant> removeDuplicates(List<Restaurant> restaurants) {
        if (restaurants == null || restaurants.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Utiliser un Map pour garder seulement un restaurant par ID
        Map<String, Restaurant> uniqueMap = new HashMap<>();
        int duplicatesCount = 0;
        
        for (Restaurant restaurant : restaurants) {
            if (restaurant != null && restaurant.getId() != null) {
                // Vérifier si le restaurant est déjà dans la map
                if (uniqueMap.containsKey(restaurant.getId())) {
                    duplicatesCount++;
                    Log.d("RestaurantViewModel", "Restaurant dupliqué éliminé: " + restaurant.getName() + " (ID: " + restaurant.getId() + ")");
                } else {
                    uniqueMap.put(restaurant.getId(), restaurant);
                }
            }
        }
        
        // Log pour le débogage
        Log.d("RestaurantViewModel", "Doublons éliminés: " + duplicatesCount + " sur " + restaurants.size() + " restaurants");
        
        // Retourner la liste sans doublons
        return new ArrayList<>(uniqueMap.values());
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
     * Met à jour le filtre
     */
    public void updateFilter(Filter filter) {
        currentFilter.setValue(filter);
    }
    
    /**
     * Réinitialise les filtres
     */
    public void resetFilters() {
        Filter filter = currentFilter.getValue();
        if (filter != null) {
            filter.reset();
            currentFilter.setValue(filter);
        } else {
            currentFilter.setValue(new Filter());
        }
    }
    
    /**
     * Obtient le filtre actuel
     */
    public Filter getCurrentFilter() {
        Filter filter = currentFilter.getValue();
        return filter != null ? filter : new Filter();
    }
    
    /**
     * Bascule l'état favori d'un restaurant
     */
    public void toggleFavorite(Restaurant restaurant) {
        // Le restaurant a déjà son état mis à jour dans le fragment
        // Nous devons juste persister ce changement
        repository.updateFavorite(restaurant);
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
     * Obtient le filtre actuel
     */
    public LiveData<Filter> getFilter() {
        return currentFilter;
    }
    
    /**
     * Vérifie si des filtres sont actifs
     */
    public boolean hasActiveFilters() {
        Filter filter = currentFilter.getValue();
        return filter != null && !filter.isEmpty();
    }
    
    /**
     * Compte le nombre de filtres actifs
     */
    public int countActiveFilters() {
        Filter filter = currentFilter.getValue();
        if (filter == null) return 0;
        
        int count = 0;
        if (filter.getPriceLevel() != null && !filter.getPriceLevel().isEmpty()) count++;
        if (!filter.getCuisineTypes().isEmpty()) count++;
        if (filter.getMinRating() > 0) count++;
        
        return count;
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
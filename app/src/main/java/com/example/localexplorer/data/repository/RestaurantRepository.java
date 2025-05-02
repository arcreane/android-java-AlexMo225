package com.example.localexplorer.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.localexplorer.data.local.AppDatabase;
import com.example.localexplorer.data.local.RestaurantDao;
import com.example.localexplorer.data.remote.ApiClient;
import com.example.localexplorer.data.remote.geoapify.GeoapifyApi;
import com.example.localexplorer.data.remote.geoapify.PlacesResponse;
import com.example.localexplorer.model.Restaurant;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository pour gérer les données des restaurants
 */
public class RestaurantRepository {
    
    private static final String TAG = "RestaurantRepository";
    private static final int THREAD_COUNT = 4;
    
    private final GeoapifyApi geoapifyApi;
    private final RestaurantDao restaurantDao;
    private final ExecutorService executorService;
    
    private final MutableLiveData<List<Restaurant>> nearbyRestaurants = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    
    public RestaurantRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        restaurantDao = database.restaurantDao();
        geoapifyApi = ApiClient.getGeoapifyApi();
        executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    }
    
    /**
     * Recherche les restaurants à proximité des coordonnées données
     */
    public void searchNearbyRestaurants(double latitude, double longitude, int radius) {
        isLoading.setValue(true);
        error.setValue(null);
        
        String categories = "catering.restaurant";
        String filter = "circle:" + longitude + "," + latitude + "," + radius;
        String bias = "proximity:" + longitude + "," + latitude;
        int limit = 20;
        
        geoapifyApi.searchNearbyPlaces(
                categories,
                filter,
                bias,
                limit,
                ApiClient.getGeoapifyApiKey()
        ).enqueue(new Callback<PlacesResponse>() {
            @Override
            public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Restaurant> restaurants = convertToRestaurants(response.body());
                    nearbyRestaurants.setValue(restaurants);
                } else {
                    error.setValue("Erreur lors de la récupération des restaurants");
                    Log.e(TAG, "API Error: " + response.code());
                }
                isLoading.setValue(false);
            }
            
            @Override
            public void onFailure(Call<PlacesResponse> call, Throwable t) {
                error.setValue("Erreur de connexion");
                Log.e(TAG, "API Failure: " + t.getMessage());
                isLoading.setValue(false);
            }
        });
    }
    
    /**
     * Convertit les résultats de l'API en objets Restaurant
     */
    private List<Restaurant> convertToRestaurants(PlacesResponse response) {
        List<Restaurant> restaurants = new ArrayList<>();
        
        if (response.getFeatures() != null) {
            for (PlacesResponse.Feature feature : response.getFeatures()) {
                PlacesResponse.Properties properties = feature.getProperties();
                PlacesResponse.Geometry geometry = feature.getGeometry();
                
                if (properties != null && geometry != null) {
                    String mainCategory = properties.getCategories() != null && !properties.getCategories().isEmpty() 
                            ? properties.getCategories().get(0) : "restaurant";
                    
                    Restaurant restaurant = new Restaurant(
                            properties.getPlaceId(),
                            properties.getName(),
                            properties.getAddress(),
                            geometry.getLatitude(),
                            geometry.getLongitude(),
                            properties.getRating(),
                            null,  // photoUrl sera mis à jour ultérieurement
                            mainCategory
                    );
                    
                    restaurants.add(restaurant);
                }
            }
        }
        
        return restaurants;
    }
    
    /**
     * Met à jour le statut favori d'un restaurant
     */
    public void toggleFavorite(Restaurant restaurant) {
        if (restaurant == null || restaurant.getId() == null) {
            Log.e(TAG, "Tentative de marquer comme favori un restaurant null ou avec ID null");
            return;
        }
        
        executorService.execute(() -> {
            try {
                restaurant.setFavorite(!restaurant.isFavorite());
                if (restaurant.isFavorite()) {
                    restaurantDao.insert(restaurant);
                } else {
                    restaurantDao.updateFavoriteStatus(restaurant.getId(), false);
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de la mise à jour du statut favori: " + e.getMessage());
                // Annuler le changement dans l'objet si l'opération échoue
                restaurant.setFavorite(!restaurant.isFavorite());
                error.postValue("Erreur lors de la mise à jour des favoris");
            }
        });
    }
    
    /**
     * Obtient tous les restaurants favoris
     */
    public LiveData<List<Restaurant>> getFavoriteRestaurants() {
        return restaurantDao.getAllFavorites();
    }
    
    /**
     * Obtient les restaurants à proximité
     */
    public LiveData<List<Restaurant>> getNearbyRestaurants() {
        return nearbyRestaurants;
    }
    
    /**
     * Obtient l'état de chargement
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    /**
     * Obtient les erreurs éventuelles
     */
    public LiveData<String> getError() {
        return error;
    }
    
    /**
     * Ferme les ressources utilisées par le repository
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
} 
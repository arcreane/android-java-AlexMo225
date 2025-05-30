package com.example.localexplorer.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.localexplorer.data.local.AppDatabase;
import com.example.localexplorer.data.local.RestaurantDao;
import com.example.localexplorer.data.remote.ApiClient;
import com.example.localexplorer.data.remote.geoapify.GeoapifyApi;
import com.example.localexplorer.data.remote.geoapify.PlacesResponse;
import com.example.localexplorer.model.Restaurant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    
    // URLs d'images génériques pour les restaurants
    private static final String[] RESTAURANT_IMAGES = {
            "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8cmVzdGF1cmFudHxlbnwwfHwwfHx8MA%3D%3D&w=1000&q=80",
            "https://images.unsplash.com/photo-1552566626-52f8b828add9?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8OHx8cmVzdGF1cmFudHxlbnwwfHwwfHx8MA%3D%3D&w=1000&q=80",
            "https://images.unsplash.com/photo-1514933651103-005eec06c04b?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8M3x8cmVzdGF1cmFudCUyMGludGVyaW9yfGVufDB8fDB8fHww&w=1000&q=80",
            "https://plus.unsplash.com/premium_photo-1675715924047-a9cf6c539d9b?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8ZnJlbmNoJTIwcmVzdGF1cmFudHxlbnwwfHwwfHx8MA%3D%3D&w=1000&q=80",
            "https://images.unsplash.com/photo-1592861956120-e524fc739696?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8M3x8cmVzdGF1cmFudHN8ZW58MHx8MHx8fDA%3D&w=1000&q=80"
    };
    private final Random random = new Random();
    
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
                    // Synchroniser les favoris avant de retourner les restaurants
                    synchronizeFavoritesStatus(restaurants);
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
        int invalidCount = 0;
        
        if (response.getFeatures() != null) {
            for (PlacesResponse.Feature feature : response.getFeatures()) {
                PlacesResponse.Properties properties = feature.getProperties();
                PlacesResponse.Geometry geometry = feature.getGeometry();
                
                // Vérifier que toutes les données essentielles sont présentes
                if (properties == null || geometry == null || properties.getPlaceId() == null || 
                        properties.getName() == null || properties.getName().trim().isEmpty()) {
                    invalidCount++;
                    Log.w(TAG, "Restaurant ignoré: données incomplètes");
                    continue;
                }
                
                try {
                    String mainCategory = properties.getCategories() != null && !properties.getCategories().isEmpty() 
                            ? properties.getCategories().get(0) : "restaurant";
                    
                    // Générer un ID stable et unique si nécessaire
                    String id = properties.getPlaceId();
                    if (id == null || id.trim().isEmpty()) {
                        // Utiliser une combinaison nom+coordonnées comme ID de secours
                        id = properties.getName() + "_" + geometry.getLatitude() + "_" + geometry.getLongitude();
                        Log.w(TAG, "ID généré pour " + properties.getName() + ": " + id);
                    }
                    
                    // S'assurer que nous avons une adresse
                    String address = properties.getAddress();
                    if (address == null || address.trim().isEmpty()) {
                        address = "Adresse non disponible";
                    }
                    
                    // Choisir une image aléatoire pour le restaurant - garantie valide
                    String photoUrl = RESTAURANT_IMAGES[random.nextInt(RESTAURANT_IMAGES.length)];
                    
                    // Assigner une note par défaut si elle n'existe pas
                    double rating = properties.getRating();
                    if (rating <= 0) {
                        rating = 3.0 + random.nextFloat() * 2.0; // Note aléatoire entre 3 et 5
                    }
                    
                    // Créer un objet Restaurant avec des données de base
                    Restaurant restaurant = new Restaurant(
                            id,
                            properties.getName(),
                            address,
                            geometry.getLatitude(),
                            geometry.getLongitude(),
                            rating,
                            photoUrl,
                            mainCategory
                    );
                    
                    // Ajouter des données supplémentaires pour améliorer l'expérience utilisateur
                    restaurant.setPriceLevel("€€"); // Prix moyen par défaut
                    
                    // Horaires d'ouverture fictifs pour améliorer l'expérience
                    String horaires = "Lundi: 11:30 - 22:00\n" +
                                     "Mardi: 11:30 - 22:00\n" +
                                     "Mercredi: 11:30 - 22:00\n" +
                                     "Jeudi: 11:30 - 22:00\n" +
                                     "Vendredi: 11:30 - 23:00\n" +
                                     "Samedi: 11:30 - 23:00\n" +
                                     "Dimanche: 12:00 - 21:00";
                    restaurant.setOpeningHours(horaires);
                    
                    // Numéro de téléphone fictif
                    restaurant.setPhoneNumber("+33 " + (1 + random.nextInt(9)) + " " + random.nextInt(10) + random.nextInt(10) + " " + random.nextInt(10) + random.nextInt(10) + " " + random.nextInt(10) + random.nextInt(10) + " " + random.nextInt(10) + random.nextInt(10));
                    
                    // Site web fictif
                    String siteName = properties.getName().toLowerCase().replaceAll("[^a-z0-9]", "");
                    restaurant.setWebsite("https://www." + siteName + ".fr");
                    
                    restaurants.add(restaurant);
                    Log.d(TAG, "Restaurant ajouté: " + restaurant.getName() + " (ID: " + restaurant.getId() + ")");
                    
                } catch (Exception e) {
                    invalidCount++;
                    Log.e(TAG, "Erreur lors de la conversion d'un restaurant: " + e.getMessage(), e);
                }
            }
        }
        
        Log.d(TAG, "Conversion terminée: " + restaurants.size() + " restaurants ajoutés, " + invalidCount + " ignorés");
        return restaurants;
    }
    
    /**
     *
     */
    public void updateFavorite(Restaurant restaurant) {
        if (restaurant == null || restaurant.getId() == null) {
            Log.e(TAG, "Tentative de mettre à jour un restaurant null ou avec ID null");
            return;
        }
        
        executorService.execute(() -> {
            try {
                if (restaurant.isFavorite()) {
                    // Enregistrer comme favori
                    restaurantDao.insert(restaurant);
                    Log.d(TAG, "Restaurant ajouté aux favoris: " + restaurant.getName());
                } else {
                    // Retirer des favoris
                    restaurantDao.updateFavoriteStatus(restaurant.getId(), false);
                    Log.d(TAG, "Restaurant retiré des favoris: " + restaurant.getName());
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de la mise à jour du statut favori: " + e.getMessage());
                error.postValue("Erreur lors de la mise à jour des favoris");
            }
        });
    }
    
    /**

     */
    public void toggleFavorite(Restaurant restaurant) {
        updateFavorite(restaurant);
    }
    
    /**
     * Obtient tous les restaurants favoris
     */
    public LiveData<List<Restaurant>> getFavoriteRestaurants() {
        // Utiliser un MediatorLiveData pour transformer les données avant de les renvoyer
        MediatorLiveData<List<Restaurant>> uniqueFavorites = new MediatorLiveData<>();
        
        uniqueFavorites.addSource(restaurantDao.getAllFavorites(), favorites -> {
            // Filtrer les doublons potentiels par ID avant de renvoyer la liste
            if (favorites != null) {
                Map<String, Restaurant> uniqueMap = new HashMap<>();
                for (Restaurant restaurant : favorites) {
                    if (restaurant != null && restaurant.getId() != null) {
                        uniqueMap.put(restaurant.getId(), restaurant);
                    }
                }
                uniqueFavorites.setValue(new ArrayList<>(uniqueMap.values()));
            } else {
                uniqueFavorites.setValue(new ArrayList<>());
            }
        });
        
        return uniqueFavorites;
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
    
    /**
     * Synchronise l'état des favoris entre la base de données locale et les restaurants
     * chargés depuis l'API pour assurer la cohérence entre les vues
     */
    private void synchronizeFavoritesStatus(List<Restaurant> restaurants) {
        if (restaurants == null || restaurants.isEmpty()) {
            nearbyRestaurants.setValue(new ArrayList<>());
            return;
        }
        
        // Exécuter la synchronisation en arrière-plan
        executorService.execute(() -> {
            try {
                // Charger les IDs des restaurants favoris depuis la base de données
                List<Restaurant> favorites = restaurantDao.getAllFavoritesSync();
                
                // Vider le cache des favoris pour éviter les états incorrects
                Log.d(TAG, "Synchronisation des favoris... " + favorites.size() + " favoris trouvés en base de données");
                
                // Créer un ensemble des IDs favoris pour une recherche rapide
                Map<String, Boolean> favoriteIds = new HashMap<>();
                for (Restaurant favorite : favorites) {
                    if (favorite != null && favorite.getId() != null) {
                        favoriteIds.put(favorite.getId(), true);
                    }
                }
                
                // Mettre à jour l'état favori de chaque restaurant
                for (Restaurant restaurant : restaurants) {
                    if (restaurant != null && restaurant.getId() != null) {
                        // S'assurer que tous les restaurants sont marqués comme non-favoris par défaut
                        restaurant.setFavorite(false);
                        
                        // Puis vérifier s'ils sont dans la liste des favoris
                        if (favoriteIds.containsKey(restaurant.getId())) {
                            restaurant.setFavorite(true);
                            Log.d(TAG, "Restaurant marqué comme favori: " + restaurant.getName() + " (ID: " + restaurant.getId() + ")");
                        }
                    }
                }
                
                // Mettre à jour la liste des restaurants avec les états synchronisés
                nearbyRestaurants.postValue(new ArrayList<>(restaurants));
                
                Log.d(TAG, "Synchronisation des favoris terminée: " + 
                      favorites.size() + " favoris, " + restaurants.size() + " restaurants totaux");
                
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de la synchronisation des favoris: " + e.getMessage(), e);
                // En cas d'erreur, retourner quand même les restaurants non synchronisés
                nearbyRestaurants.postValue(restaurants);
            }
        });
    }
} 
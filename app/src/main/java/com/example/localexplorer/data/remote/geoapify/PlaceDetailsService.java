package com.example.localexplorer.data.remote.geoapify;

import android.util.Log;

import com.example.localexplorer.data.remote.ApiClient;
import com.example.localexplorer.model.Restaurant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Service pour charger les détails des restaurants à partir de l'API Place Details de Geoapify
 */
public class PlaceDetailsService {
    
    private static final String TAG = "PlaceDetailsService";
    private static final String BASE_URL = "https://api.geoapify.com/v2/place-details";
    private final OkHttpClient client;
    private final Executor executor;

    public PlaceDetailsService() {
        this.client = new OkHttpClient();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Interface de callback pour les détails du restaurant
     */
    public interface PlaceDetailsCallback {
        void onSuccess(Restaurant restaurant);
        void onError(String message);
    }

    /**
     * Charge les détails pour un restaurant donné
     */
    public void loadPlaceDetails(Restaurant restaurant, PlaceDetailsCallback callback) {
        String url = String.format("%s?id=%s&apiKey=%s", 
                BASE_URL, 
                restaurant.getId(), 
                ApiClient.getGeoapifyApiKey());
        
        Request request = new Request.Builder().url(url).build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Erreur lors du chargement des détails: " + e.getMessage());
                callback.onError("Erreur de connexion");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    
                    executor.execute(() -> {
                        try {
                            parseResponseAndUpdateRestaurant(responseBody, restaurant);
                            callback.onSuccess(restaurant);
                        } catch (JSONException e) {
                            Log.e(TAG, "Erreur lors de l'analyse des détails: " + e.getMessage());
                            callback.onError("Erreur lors de l'analyse des données");
                        }
                    });
                } else {
                    Log.e(TAG, "Erreur API: " + response.code());
                    callback.onError("Erreur lors de la récupération des détails");
                }
            }
        });
    }

    /**
     * Analyse la réponse JSON et met à jour le restaurant avec les détails
     */
    private void parseResponseAndUpdateRestaurant(String responseBody, Restaurant restaurant) throws JSONException {
        if (responseBody == null || responseBody.isEmpty()) {
            throw new JSONException("Réponse vide de l'API");
        }
        
        JSONObject jsonResponse = new JSONObject(responseBody);
        
        // Vérifier que nous avons des résultats
        if (!jsonResponse.has("features") || jsonResponse.getJSONArray("features").length() == 0) {
            Log.w(TAG, "Aucun détail trouvé pour ce restaurant: " + restaurant.getId());
            return;
        }
        
        JSONObject feature = jsonResponse.getJSONArray("features").getJSONObject(0);
        JSONObject properties = feature.getJSONObject("properties");
        
        // Extraire les heures d'ouverture si disponibles
        if (properties.has("opening_hours")) {
            JSONObject openingHours = properties.getJSONObject("opening_hours");
            StringBuilder hoursText = new StringBuilder();
            
            // Format des heures d'ouverture : Jour: heure ouverture - heure fermeture
            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            for (String day : days) {
                if (openingHours.has(day.toLowerCase())) {
                    JSONObject dayHours = openingHours.getJSONObject(day.toLowerCase());
                    hoursText.append(day).append(": ");
                    
                    if (dayHours.has("open") && dayHours.has("close")) {
                        hoursText.append(dayHours.getString("open"))
                                .append(" - ")
                                .append(dayHours.getString("close"));
                    } else {
                        hoursText.append("Fermé");
                    }
                    
                    hoursText.append("\n");
                }
            }
            
            restaurant.setOpeningHours(hoursText.toString().trim());
        }
        
        // Extraire le niveau de prix si disponible
        if (properties.has("price_level")) {
            String priceLevel = properties.getString("price_level");
            // Convertir le niveau de prix en symboles €
            StringBuilder priceLevelText = new StringBuilder();
            int level = Integer.parseInt(priceLevel);
            for (int i = 0; i < level; i++) {
                priceLevelText.append("€");
            }
            restaurant.setPriceLevel(priceLevelText.toString());
        }
        
        // Extraire le numéro de téléphone si disponible
        if (properties.has("contact") && properties.getJSONObject("contact").has("phone")) {
            restaurant.setPhoneNumber(properties.getJSONObject("contact").getString("phone"));
        }
        
        // Extraire le site web si disponible
        if (properties.has("contact") && properties.getJSONObject("contact").has("website")) {
            restaurant.setWebsite(properties.getJSONObject("contact").getString("website"));
        }
    }
} 
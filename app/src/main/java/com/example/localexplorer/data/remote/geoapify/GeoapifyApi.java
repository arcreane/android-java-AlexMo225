package com.example.localexplorer.data.remote.geoapify;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface pour les appels à l'API Geoapify
 */
public interface GeoapifyApi {
    
    @GET("v2/places")
    Call<PlacesResponse> searchNearbyPlaces(
            @Query("categories") String categories,
            @Query("filter") String filter,
            @Query("bias") String bias,
            @Query("limit") int limit,
            @Query("apiKey") String apiKey
    );
} 
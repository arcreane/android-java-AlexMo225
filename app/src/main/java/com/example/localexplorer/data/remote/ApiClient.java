package com.example.localexplorer.data.remote;

import com.example.localexplorer.data.remote.geoapify.GeoapifyApi;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Client API pour configurer Retrofit et les appels réseau
 */
public class ApiClient {
    
    private static final String GEOAPIFY_BASE_URL = "https://api.geoapify.com/";
    private static final String GEOAPIFY_API_KEY = "72580b4bd7b84765a30a63a2d973312c";
    
    private static Retrofit retrofit = null;
    private static GeoapifyApi geoapifyApi = null;
    
    /**
     * Obtient une instance de l'API Geoapify
     */
    public static GeoapifyApi getGeoapifyApi() {
        if (geoapifyApi == null) {
            geoapifyApi = getRetrofitClient(GEOAPIFY_BASE_URL).create(GeoapifyApi.class);
        }
        return geoapifyApi;
    }
    
    /**
     * Obtient la clé API Geoapify
     */
    public static String getGeoapifyApiKey() {
        return GEOAPIFY_API_KEY;
    }
    
    /**
     * Crée et configure un client Retrofit
     */
    private static Retrofit getRetrofitClient(String baseUrl) {
        if (retrofit == null || !retrofit.baseUrl().toString().equals(baseUrl)) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build();
            
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
} 
package com.example.localexplorer.data.remote;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.example.localexplorer.data.remote.geoapify.GeoapifyApi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Client API pour configurer Retrofit et les appels réseau
 */
public class ApiClient {
    
    private static final String TAG = "ApiClient";
    private static final String GEOAPIFY_BASE_URL = "https://api.geoapify.com/";
    private static String GEOAPIFY_API_KEY = "";
    
    private static Retrofit retrofit = null;
    private static GeoapifyApi geoapifyApi = null;
    
    /**
     * Initialise les clés API depuis le fichier de propriétés
     * Cette méthode doit être appelée au démarrage de l'application
     */
    public static void init(Context context) {
        Properties properties = new Properties();
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("api_keys.properties");
            properties.load(inputStream);
            GEOAPIFY_API_KEY = properties.getProperty("GEOAPIFY_API_KEY", "");
            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors du chargement des clés API", e);
        }
    }
    
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
     * Retourne la clé API Geoapify
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
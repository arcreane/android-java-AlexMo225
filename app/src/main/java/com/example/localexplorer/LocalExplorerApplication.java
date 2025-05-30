package com.example.localexplorer;

import android.app.Application;

import com.example.localexplorer.data.remote.ApiClient;

/**
 * Classe Application principale pour initialiser les composants au démarrage
 */
public class LocalExplorerApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialiser les clés API
        ApiClient.init(this);
    }
} 
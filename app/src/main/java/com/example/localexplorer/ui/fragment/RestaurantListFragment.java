package com.example.localexplorer.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localexplorer.R;
import com.example.localexplorer.model.Restaurant;
import com.example.localexplorer.ui.adapter.RestaurantAdapter;
import com.example.localexplorer.ui.dialog.RestaurantDetailsDialog;
import com.example.localexplorer.viewmodel.RestaurantViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fragment pour afficher la liste des restaurants
 */
public class RestaurantListFragment extends Fragment implements RestaurantAdapter.OnRestaurantClickListener {

    private static final String TAG = "RestaurantListFragment";
    private static final String ARG_FAVORITE_MODE = "favorite_mode";
    
    // Handler pour les opérations différées
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // Vues
    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView errorTextView;
    private SearchView searchView;
    
    // État
    private boolean isFavoriteMode = false;
    private boolean isViewInitialized = false;
    private RestaurantViewModel viewModel;
    private boolean listInitialized = false;

    /**
     * Crée une nouvelle instance du fragment en mode favoris
     */
    public static RestaurantListFragment newFavoriteInstance() {
        RestaurantListFragment fragment = new RestaurantListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_FAVORITE_MODE, true);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Récupérer les arguments du fragment
        if (getArguments() != null) {
            isFavoriteMode = getArguments().getBoolean(ARG_FAVORITE_MODE, false);
        }
        
        // Initialiser l'adaptateur avant la création de la vue
        adapter = new RestaurantAdapter(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView;
        try {
            rootView = inflater.inflate(R.layout.fragment_restaurant_list, container, false);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'inflation du layout: " + e.getMessage(), e);
            errorTextView = new TextView(requireContext());
            errorTextView.setText("Erreur de chargement: " + e.getMessage());
            return errorTextView;
        }
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Éviter la double initialisation
        if (isViewInitialized) {
            return;
        }
        isViewInitialized = true;

        try {
            // Initialiser les vues
            initViews(view);
            
            // Initialiser le ViewModel avec un délai pour éviter les problèmes de concurrence
            mainHandler.postDelayed(() -> {
                // S'assurer que le fragment est encore attaché
                if (isAdded() && !isDetached() && getActivity() != null && !getActivity().isFinishing()) {
                    try {
                        initViewModel();
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors de l'initialisation du ViewModel: " + e.getMessage(), e);
                        showError("Erreur lors du chargement des données");
                    }
                }
            }, 500); // Délai de 500ms
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans onViewCreated: " + e.getMessage(), e);
            showError("Erreur de chargement: " + e.getMessage());
        }
    }
    
    /**
     * Initialise les vues
     */
    private void initViews(View view) {
        try {
            recyclerView = view.findViewById(R.id.restaurantRecyclerView);
            progressBar = view.findViewById(R.id.progressBar);
            emptyView = view.findViewById(R.id.emptyView);
            errorTextView = view.findViewById(R.id.errorTextView);
            searchView = view.findViewById(R.id.searchView);
            
            // Configurer le RecyclerView avec l'adaptateur déjà créé
            if (recyclerView != null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                recyclerView.setAdapter(adapter);
                recyclerView.setHasFixedSize(true); // Optimisation
            }
            
            // Masquer la barre de recherche en mode favoris
            if (searchView != null && isFavoriteMode) {
                searchView.setVisibility(View.GONE);
            }
            
            // Afficher le loading
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            
            // Masquer les messages d'erreur
            if (errorTextView != null) {
                errorTextView.setVisibility(View.GONE);
            }
            
            // Masquer le message vide
            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation des vues: " + e.getMessage(), e);
        }
    }
    
    /**
     * Initialise le ViewModel et observe les données
     */
    private void initViewModel() {
        try {
            // S'assurer que l'activité est disponible
            if (getActivity() == null) return;
            
            // Obtenir le ViewModel
            viewModel = new ViewModelProvider(requireActivity()).get(RestaurantViewModel.class);
            
            // Observer les données appropriées selon le mode
            if (isFavoriteMode) {
                observeFavorites();
            } else {
                observeRestaurants();
                configureSearch();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation du ViewModel: " + e.getMessage(), e);
            showError("Erreur lors de l'initialisation des données");
        }
    }
    
    /**
     * Observe les restaurants favoris
     */
    private void observeFavorites() {
        try {
            // Afficher l'indicateur de chargement au début
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            
            viewModel.getFavoriteRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
                try {
                    // Masquer l'indicateur de chargement une fois les données reçues
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    
                    updateRestaurants(restaurants);
                    
                    // Afficher un message spécifique pour les favoris vides
                    if (restaurants == null || restaurants.isEmpty()) {
                        if (emptyView != null) {
                            emptyView.setText(R.string.no_favorites);
                            emptyView.setVisibility(View.VISIBLE);
                        }
                        
                        if (recyclerView != null) {
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erreur dans l'observateur de favoris: " + e.getMessage(), e);
                    showError("Erreur de chargement des favoris");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'observation des favoris: " + e.getMessage(), e);
            showError("Erreur lors de la configuration du mode favoris");
        }
    }
    
    /**
     * Observe les restaurants filtrés
     */
    private void observeRestaurants() {
        try {
            // Observer les restaurants filtrés
            viewModel.getFilteredRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
                try {
                    updateRestaurants(restaurants);
                } catch (Exception e) {
                    Log.e(TAG, "Erreur dans l'observateur de restaurants: " + e.getMessage(), e);
                    showError("Erreur de chargement des restaurants");
                }
            });
            
            // Observer l'état de chargement
            viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
                try {
                    if (progressBar != null) {
                        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erreur lors de la mise à jour de l'état de chargement: " + e.getMessage(), e);
                }
            });
            
            // Observer les erreurs
            viewModel.getError().observe(getViewLifecycleOwner(), error -> {
                try {
                    if (errorTextView != null) {
                        if (error != null && !error.isEmpty()) {
                            errorTextView.setText(error);
                            errorTextView.setVisibility(View.VISIBLE);
                        } else {
                            errorTextView.setVisibility(View.GONE);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erreur lors de la mise à jour de l'erreur: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'observation des restaurants: " + e.getMessage(), e);
            showError("Erreur lors de la configuration de l'affichage");
        }
    }
    
    /**
     * Configure la recherche
     */
    private void configureSearch() {
        try {
            if (searchView != null) {
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        if (viewModel != null) {
                            viewModel.updateSearchQuery(query);
                        }
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (viewModel != null) {
                            viewModel.updateSearchQuery(newText);
                        }
                        return true;
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la configuration de la recherche: " + e.getMessage(), e);
        }
    }
    
    /**
     * Met à jour la liste des restaurants
     */
    private void updateRestaurants(List<Restaurant> restaurants) {
        try {
            // Créer une copie synchronisée et immuable de la liste pour éviter les problèmes de concurrence
            List<Restaurant> safeList = restaurants != null 
                ? Collections.synchronizedList(new ArrayList<>(restaurants)) 
                : Collections.synchronizedList(new ArrayList<>());
            
            // Mettre à jour l'adaptateur sur le thread principal
            mainHandler.post(() -> {
                if (isAdded() && !isDetached()) {
                    try {
                        adapter.submitList(safeList);
                        updateEmptyState(safeList.isEmpty());
                        
                        // Marquer comme initialisé
                        listInitialized = true;
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors de la mise à jour de l'adaptateur: " + e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la mise à jour des restaurants: " + e.getMessage(), e);
        }
    }
    
    /**
     * Met à jour l'état vide
     */
    private void updateEmptyState(boolean isEmpty) {
        try {
            if (isAdded() && !isDetached()) {
                if (emptyView != null) {
                    // Sélectionner le message approprié en fonction du mode
                    if (isEmpty) {
                        if (isFavoriteMode) {
                            emptyView.setText(R.string.no_favorites);
                        } else {
                            emptyView.setText(R.string.no_restaurants_found);
                        }
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                    }
                }
                
                if (recyclerView != null) {
                    recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                }
                
                // Toujours cacher le progressBar une fois que nous avons des données
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la mise à jour de l'état vide: " + e.getMessage(), e);
        }
    }
    
    /**
     * Affiche un message d'erreur
     */
    private void showError(String message) {
        try {
            if (isAdded() && !isDetached()) {
                mainHandler.post(() -> {
                    try {
                        if (errorTextView != null) {
                            errorTextView.setText(message);
                            errorTextView.setVisibility(View.VISIBLE);
                        }
                        
                        if (emptyView != null) {
                            emptyView.setVisibility(View.GONE);
                        }
                        
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors de l'affichage du message d'erreur: " + e.getMessage(), e);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'affichage du message d'erreur: " + e.getMessage(), e);
        }
    }

    @Override
    public void onRestaurantClick(Restaurant restaurant) {
        try {
            // Nous n'appelons plus directement le dialog ici puisque le clic
            // est maintenant utilisé pour le système d'accordéon dans l'adapter
            // Le comportement est géré dans l'adaptateur directement
            
            // Mise à jour de l'état d'expansion est maintenant gérée dans l'adaptateur
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'affichage des détails: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Impossible d'afficher les détails", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFavoriteClick(Restaurant restaurant) {
        try {
            if (restaurant != null && viewModel != null && isAdded() && !isDetached()) {
                // Sauvegarder l'état actuel pour le toast
                boolean willBeFavorite = !restaurant.isFavorite();
                
                // Mettre à jour l'interface immédiatement pour un retour visuel instantané
                restaurant.setFavorite(willBeFavorite);
                
                // Forcer une mise à jour immédiate de l'élément spécifique
                int position = -1;
                List<Restaurant> currentList = adapter.getCurrentList();
                for (int i = 0; i < currentList.size(); i++) {
                    if (currentList.get(i).getId().equals(restaurant.getId())) {
                        position = i;
                        break;
                    }
                }
                
                if (position >= 0) {
                    adapter.notifyItemChanged(position);
                }
                
                // Mettre à jour le favori dans le ViewModel/base de données
                viewModel.toggleFavorite(restaurant);
                
                // Afficher un toast sur le thread principal
                mainHandler.post(() -> {
                    try {
                        Toast.makeText(requireContext(), willBeFavorite 
                                ? "Ajouté aux favoris" 
                                : "Retiré des favoris", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors de l'affichage du toast: " + e.getMessage(), e);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du clic sur favori: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // Réinitialiser si nécessaire
        if (!listInitialized && viewModel != null) {
            // Recharger les données
            if (isFavoriteMode) {
                observeFavorites();
            } else {
                observeRestaurants();
            }
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Nettoyer les références
        isViewInitialized = false;
        listInitialized = false;
        
        // Supprimer tous les callbacks en attente
        mainHandler.removeCallbacksAndMessages(null);
    }
} 
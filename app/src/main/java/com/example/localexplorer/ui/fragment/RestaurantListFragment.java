package com.example.localexplorer.ui.fragment;

import android.os.Bundle;
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
import com.example.localexplorer.viewmodel.RestaurantViewModel;

/**
 * Fragment pour afficher la liste des restaurants
 */
public class RestaurantListFragment extends Fragment implements RestaurantAdapter.OnRestaurantClickListener {

    private RestaurantViewModel viewModel;
    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView errorTextView;
    private SearchView searchView;

    private boolean isFavoriteMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurant_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser les vues
        recyclerView = view.findViewById(R.id.restaurantRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        errorTextView = view.findViewById(R.id.errorTextView);
        searchView = view.findViewById(R.id.searchView);

        // Récupérer les arguments si en mode favoris
        Bundle args = getArguments();
        if (args != null) {
            isFavoriteMode = args.getBoolean("favorite_mode", false);
        }

        // Configurer le RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RestaurantAdapter(this);
        recyclerView.setAdapter(adapter);

        // Initialiser le ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(RestaurantViewModel.class);

        // Observer les restaurants appropriés (favoris ou tous)
        if (isFavoriteMode) {
            viewModel.getFavoriteRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
                adapter.submitList(restaurants);
                emptyView.setVisibility(restaurants.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(restaurants.isEmpty() ? View.GONE : View.VISIBLE);
            });
        } else {
            // Observer les changements dans les restaurants filtrés
            viewModel.getFilteredRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
                adapter.submitList(restaurants);
                emptyView.setVisibility(restaurants.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(restaurants.isEmpty() ? View.GONE : View.VISIBLE);
            });

            // Observer l'état de chargement
            viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            });

            // Observer les erreurs
            viewModel.getError().observe(getViewLifecycleOwner(), error -> {
                if (error != null && !error.isEmpty()) {
                    errorTextView.setText(error);
                    errorTextView.setVisibility(View.VISIBLE);
                } else {
                    errorTextView.setVisibility(View.GONE);
                }
            });

            // Configurer la recherche
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    viewModel.updateSearchQuery(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    viewModel.updateSearchQuery(newText);
                    return true;
                }
            });
        }
    }

    @Override
    public void onRestaurantClick(Restaurant restaurant) {
        // Ouvrir une activité de détail ou un bottom sheet avec plus d'informations
        Toast.makeText(requireContext(), "Restaurant sélectionné: " + restaurant.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFavoriteClick(Restaurant restaurant) {
        // Sauvegarder l'état actuel pour le toast
        boolean willBeFavorite = !restaurant.isFavorite();
        
        viewModel.toggleFavorite(restaurant);
        
        // Utiliser l'état qui sera appliqué pour le message
        Toast.makeText(requireContext(), willBeFavorite 
                ? "Ajouté aux favoris" 
                : "Retiré des favoris", Toast.LENGTH_SHORT).show();
    }

    /**
     * Crée une nouvelle instance du fragment en mode favoris
     */
    public static RestaurantListFragment newFavoriteInstance() {
        RestaurantListFragment fragment = new RestaurantListFragment();
        Bundle args = new Bundle();
        args.putBoolean("favorite_mode", true);
        fragment.setArguments(args);
        return fragment;
    }
} 
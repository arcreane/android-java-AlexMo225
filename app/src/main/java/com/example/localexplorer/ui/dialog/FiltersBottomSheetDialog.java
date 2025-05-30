package com.example.localexplorer.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.localexplorer.R;
import com.example.localexplorer.model.Filter;
import com.example.localexplorer.viewmodel.RestaurantViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * BottomSheetDialogFragment pour afficher et gérer les filtres de restaurants
 */
public class FiltersBottomSheetDialog extends BottomSheetDialogFragment {

    private RestaurantViewModel viewModel;
    private Filter currentFilter;
    
    // UI Components
    private Spinner priceSpinner;
    private ChipGroup cuisineChipGroup;
    private SeekBar ratingSeekBar;
    private TextView ratingValueText;
    private Button applyButton;
    private Button resetButton;
    
    // Cuisine types
    private final String[] cuisineTypes = {
            "Italien", "Japonais", "Indien", "Français", "Chinois", 
            "Mexicain", "Thaïlandais", "Américain", "Libanais", "Végétarien"
    };

    /**
     * Crée une nouvelle instance de FiltersBottomSheetDialog
     */
    public static FiltersBottomSheetDialog newInstance() {
        return new FiltersBottomSheetDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_filters, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialiser le ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(RestaurantViewModel.class);
        
        // Obtenir le filtre actuel
        currentFilter = viewModel.getCurrentFilter();
        
        // Initialiser les composants UI
        initViews(view);
        
        // Configurer les contrôles avec les valeurs actuelles
        setupControls();
        
        // Configurer les listeners
        setupListeners();
    }
    
    private void initViews(View view) {
        priceSpinner = view.findViewById(R.id.price_spinner);
        cuisineChipGroup = view.findViewById(R.id.cuisine_chip_group);
        ratingSeekBar = view.findViewById(R.id.rating_seekbar);
        ratingValueText = view.findViewById(R.id.rating_value_text);
        applyButton = view.findViewById(R.id.apply_filters_button);
        resetButton = view.findViewById(R.id.reset_filters_button);
    }
    
    private void setupControls() {
        // Configurer le spinner de prix
        ArrayAdapter<CharSequence> priceAdapter = ArrayAdapter.createFromResource(
                requireContext(), 
                R.array.price_options, 
                android.R.layout.simple_spinner_item);
        priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priceSpinner.setAdapter(priceAdapter);
        
        // Vérifier que le filtre n'est pas null avant de l'utiliser
        if (currentFilter == null) {
            currentFilter = new Filter();
        }
        
        // Sélectionner le prix actuel
        if (currentFilter.getPriceLevel() != null) {
            int pricePosition = Arrays.asList(getResources().getStringArray(R.array.price_values))
                    .indexOf(currentFilter.getPriceLevel());
            if (pricePosition >= 0) {
                priceSpinner.setSelection(pricePosition);
            }
        }
        
        // Configurer les chips de cuisine
        setupCuisineChips();
        
        // Configurer la SeekBar de notation
        ratingSeekBar.setMax(40); // 0.0 à 5.0 avec précision de 0.1 (50 pas)
        int progress = (int) (currentFilter.getMinRating() * 10);
        ratingSeekBar.setProgress(progress);
        ratingValueText.setText(getString(R.string.min_rating_value, currentFilter.getMinRating()));
    }
    
    private void setupCuisineChips() {
        cuisineChipGroup.removeAllViews();
        
        for (String cuisineType : cuisineTypes) {
            Chip chip = new Chip(requireContext());
            chip.setText(cuisineType);
            chip.setCheckable(true);
            chip.setChecked(currentFilter.getCuisineTypes().contains(cuisineType));
            
            cuisineChipGroup.addView(chip);
        }
    }
    
    private void setupListeners() {
        // Listener pour la SeekBar de notation
        ratingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float rating = progress / 10f;
                ratingValueText.setText(getString(R.string.min_rating_value, rating));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Pas nécessaire
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Pas nécessaire
            }
        });
        
        // Listener pour le bouton d'application des filtres
        applyButton.setOnClickListener(v -> {
            applyFilters();
            dismiss();
        });
        
        // Listener pour le bouton de réinitialisation des filtres
        resetButton.setOnClickListener(v -> {
            resetFilters();
            dismiss();
        });
    }
    
    private void applyFilters() {
        // Créer un nouveau filtre basé sur les sélections actuelles
        Filter newFilter = new Filter();
        
        // Récupérer le niveau de prix sélectionné
        String[] priceValues = getResources().getStringArray(R.array.price_values);
        int pricePosition = priceSpinner.getSelectedItemPosition();
        if (pricePosition > 0) { // Ignorer "Tous les prix"
            newFilter.setPriceLevel(priceValues[pricePosition]);
        }
        
        // Récupérer les types de cuisine sélectionnés
        List<String> selectedCuisines = new ArrayList<>();
        for (int i = 0; i < cuisineChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) cuisineChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedCuisines.add(chip.getText().toString());
            }
        }
        newFilter.setCuisineTypes(selectedCuisines);
        
        // Récupérer la note minimale
        float rating = ratingSeekBar.getProgress() / 10f;
        newFilter.setMinRating(rating);
        
        // Afficher le nombre de filtres actifs
        String message;
        int activeFilters = 0;
        
        if (pricePosition > 0) activeFilters++;
        if (!selectedCuisines.isEmpty()) activeFilters++;
        if (rating > 0) activeFilters++;
        
        if (activeFilters == 0) {
            message = getString(R.string.no_filters);
        } else {
            message = getString(R.string.filters_count, activeFilters);
        }
        
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        
        // Appliquer le filtre via le ViewModel
        viewModel.updateFilter(newFilter);
        
        // Observer le résultat du filtrage pour afficher un message à l'utilisateur
        viewModel.getFilteredRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            if (restaurants != null) {
                // Supprimer l'observer après la première mise à jour pour éviter les appels multiples
                viewModel.getFilteredRestaurants().removeObservers(getViewLifecycleOwner());
                
                // Afficher le nombre de restaurants trouvés
                String resultMessage = restaurants.size() + " restaurant(s) trouvé(s)";
                Toast.makeText(requireContext(), resultMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void resetFilters() {
        // Réinitialiser les filtres via le ViewModel
        viewModel.resetFilters();
    }
} 
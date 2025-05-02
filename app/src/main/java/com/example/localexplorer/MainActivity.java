package com.example.localexplorer;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.localexplorer.ui.adapter.ViewPagerAdapter;
import com.example.localexplorer.viewmodel.RestaurantViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton searchFab;
    private RestaurantViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser les vues
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        searchFab = findViewById(R.id.searchFab);

        // Configurer le ViewPager avec l'adaptateur
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Connecter TabLayout et ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.map);
                    break;
                case 1:
                    tab.setText(R.string.list);
                    break;
                case 2:
                    tab.setText(R.string.favorites);
                    break;
            }
        }).attach();

        // Configurer le bouton de recherche/filtre
        searchFab.setOnClickListener(this::showFilterDialog);

        // Obtenir le ViewModel
        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(RestaurantViewModel.class);
    }

    /**
     * Affiche le dialogue de filtrage
     */
    private void showFilterDialog(View view) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        
        // Récupérer les sliders
        Slider distanceSlider = dialogView.findViewById(R.id.distanceSlider);
        Slider ratingSlider = dialogView.findViewById(R.id.ratingSlider);
        
        // Configurer les valeurs initiales
        distanceSlider.setValue(1000);  // 1km par défaut
        ratingSlider.setValue(0);      // Pas de note minimale par défaut
        
        // Créer et afficher le dialogue
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.filter)
                .setView(dialogView)
                .setPositiveButton(R.string.apply_filters, (dialog, which) -> {
                    // Appliquer les filtres
                    viewModel.updateSearchRadius((int) distanceSlider.getValue());
                    viewModel.updateMinRating(ratingSlider.getValue());
                })
                .setNegativeButton(R.string.reset_filters, (dialog, which) -> {
                    // Réinitialiser les filtres
                    viewModel.updateSearchRadius(1000);
                    viewModel.updateMinRating(0);
                })
                .show();
    }
}
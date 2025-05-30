package com.example.localexplorer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.localexplorer.ui.adapter.ViewPagerAdapter;
import com.example.localexplorer.ui.dialog.FiltersBottomSheetDialog;
import com.example.localexplorer.viewmodel.RestaurantViewModel;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton filterFab;
    private RestaurantViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser les vues
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        filterFab = findViewById(R.id.searchFab);

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

        // Obtenir le ViewModel
        viewModel = new ViewModelProvider(this).get(RestaurantViewModel.class);

        // Configurer le bouton de filtre
        filterFab.setOnClickListener(v -> showFiltersBottomSheet());

        // Observer les changements de filtres
        viewModel.getFilter().observe(this, filter -> {
            updateFilterBadge();
        });
    }

    /**
     * Affiche le BottomSheet pour les filtres avancés
     */
    private void showFiltersBottomSheet() {
        FiltersBottomSheetDialog bottomSheet = FiltersBottomSheetDialog.newInstance();
        bottomSheet.show(getSupportFragmentManager(), "FiltersBottomSheet");
    }

    /**
     * Met à jour le badge sur le bouton de filtre
     */
    private void updateFilterBadge() {
        int activeFiltersCount = viewModel.countActiveFilters();
        
        if (activeFiltersCount > 0) {
            // Changer l'icône du FAB pour indiquer des filtres actifs
            filterFab.setImageResource(R.drawable.ic_filter_applied);
            
            // Ajouter une indication textuelle du nombre de filtres actifs
            filterFab.setContentDescription(getString(R.string.filters_count, activeFiltersCount));
            
            // Ajouter un badge visuel au FAB
            BadgeDrawable badge = BadgeDrawable.create(this);
            badge.setNumber(activeFiltersCount);
            // Utiliser une couleur existante dans le thème
            badge.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark, getTheme()));
            
            // Positionner le badge sur le FAB
            // Note: Pour une implémentation complète, utiliser une vue conteneur avec BadgeUtils
            // Cette approche simplifée utilise un workaround, mais une bibliothèque dédiée
            // comme MaterialComponents est recommandée pour une solution robuste
            
            // Mettre à jour le hint du FAB pour indiquer le nombre de filtres
            filterFab.setTooltipText(getString(R.string.filters_count, activeFiltersCount));
        } else {
            // Rétablir l'icône par défaut
            filterFab.setImageResource(R.drawable.ic_filter);
            filterFab.setContentDescription(getString(R.string.filters));
            filterFab.setTooltipText(getString(R.string.filters));
        }
    }
}
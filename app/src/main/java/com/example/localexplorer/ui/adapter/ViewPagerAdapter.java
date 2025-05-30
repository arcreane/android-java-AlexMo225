package com.example.localexplorer.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.localexplorer.ui.fragment.MapFragment;
import com.example.localexplorer.ui.fragment.RestaurantListFragment;

/**
 * Adaptateur pour gérer les fragments dans le ViewPager
 */
public class ViewPagerAdapter extends FragmentStateAdapter {

    private static final int TAB_COUNT = 3;
    private static final int MAP_TAB = 0;
    private static final int LIST_TAB = 1;
    private static final int FAVORITES_TAB = 2;
    
    // Fragments mis en cache pour éviter les recréations
    private Fragment mapFragment;
    private Fragment listFragment;
    private Fragment favoritesFragment;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        // Pré-initialiser les fragments
        mapFragment = new MapFragment();
        listFragment = new RestaurantListFragment();
        favoritesFragment = RestaurantListFragment.newFavoriteInstance();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case MAP_TAB:
                return mapFragment;
            case LIST_TAB:
                return listFragment;
            case FAVORITES_TAB:
                return favoritesFragment;
            default:
                return mapFragment;
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
} 
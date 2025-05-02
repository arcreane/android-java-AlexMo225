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

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case MAP_TAB:
                return new MapFragment();
            case LIST_TAB:
                return new RestaurantListFragment();
            case FAVORITES_TAB:
                return RestaurantListFragment.newFavoriteInstance();
            default:
                return new MapFragment();
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
} 
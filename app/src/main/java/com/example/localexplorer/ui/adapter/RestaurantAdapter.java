package com.example.localexplorer.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.localexplorer.R;
import com.example.localexplorer.model.Restaurant;

/**
 * Adaptateur pour afficher les restaurants dans un RecyclerView
 */
public class RestaurantAdapter extends ListAdapter<Restaurant, RestaurantAdapter.RestaurantViewHolder> {

    private final OnRestaurantClickListener listener;

    public RestaurantAdapter(OnRestaurantClickListener listener) {
        super(new RestaurantDiff());
        this.listener = listener;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = getItem(position);
        if (restaurant != null) {
            holder.nameTextView.setText(restaurant.getName());
            holder.addressTextView.setText(restaurant.getAddress());
            holder.categoryTextView.setText(restaurant.getPlaceType());
            holder.ratingBar.setRating((float) restaurant.getRating());
            
            // Définir l'icône de favori en fonction du statut
            holder.favoriteImageView.setImageResource(
                    restaurant.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_favorite_border
            );
            
            // Charger l'image du restaurant s'il y en a une
            if (restaurant.getPhotoUrl() != null && !restaurant.getPhotoUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(restaurant.getPhotoUrl())
                        .placeholder(R.drawable.placeholder_restaurant)
                        .error(R.drawable.placeholder_restaurant)
                        .centerCrop()
                        .into(holder.restaurantImageView);
            } else {
                holder.restaurantImageView.setImageResource(R.drawable.placeholder_restaurant);
            }
            
            // Configurer les clics
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRestaurantClick(restaurant);
                }
            });
            
            holder.favoriteImageView.setOnClickListener(v -> {
                if (listener != null) {
                    // Ne pas modifier l'état directement ici, laisser le ViewModel/Repository s'en charger
                    // et attendre la mise à jour via l'observateur LiveData
                    listener.onFavoriteClick(restaurant);
                }
            });
        }
    }

    /**
     * ViewHolder pour afficher un élément restaurant
     */
    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView addressTextView;
        private final TextView categoryTextView;
        private final RatingBar ratingBar;
        private final ImageView restaurantImageView;
        private final ImageView favoriteImageView;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.restaurant_name);
            addressTextView = itemView.findViewById(R.id.restaurant_address);
            categoryTextView = itemView.findViewById(R.id.restaurant_category);
            ratingBar = itemView.findViewById(R.id.restaurant_rating);
            restaurantImageView = itemView.findViewById(R.id.restaurant_image);
            favoriteImageView = itemView.findViewById(R.id.favorite_icon);
        }
    }

    /**
     * Interface pour gérer les clics sur les restaurants
     */
    public interface OnRestaurantClickListener {
        void onRestaurantClick(Restaurant restaurant);
        void onFavoriteClick(Restaurant restaurant);
    }

    /**
     * Classe DiffUtil pour optimiser les mises à jour de la liste
     */
    private static class RestaurantDiff extends DiffUtil.ItemCallback<Restaurant> {
        @Override
        public boolean areItemsTheSame(@NonNull Restaurant oldItem, @NonNull Restaurant newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Restaurant oldItem, @NonNull Restaurant newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getAddress().equals(newItem.getAddress()) &&
                    oldItem.getRating() == newItem.getRating() &&
                    oldItem.isFavorite() == newItem.isFavorite();
        }
    }
} 
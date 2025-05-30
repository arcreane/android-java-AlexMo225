package com.example.localexplorer.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.localexplorer.R;
import com.example.localexplorer.model.Restaurant;
import com.google.android.material.button.MaterialButton;

/**
 * Adaptateur pour afficher les restaurants dans un RecyclerView
 * Version simplifiée pour éviter les problèmes de recyclage
 */
public class RestaurantAdapter extends ListAdapter<Restaurant, RestaurantAdapter.RestaurantViewHolder> {

    private static final String TAG = "RestaurantAdapter";
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
        return new RestaurantViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = getItem(position);
        if (restaurant != null) {
            holder.bind(restaurant);
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
        private final ImageView expandIconView;
        
        // Vues pour l'expansion
        private final ConstraintLayout mainContent;
        private final View divider;
        private final ConstraintLayout expandedContent;
        private final TextView hoursText;
        private final TextView phoneText;
        private final MaterialButton shareButton;
        private final MaterialButton directionsButton;
        
        private final Context context;
        private Restaurant currentRestaurant;

        public RestaurantViewHolder(@NonNull View itemView, OnRestaurantClickListener listener) {
            super(itemView);
            
            this.context = itemView.getContext();
            
            // Initialiser les vues principales
            mainContent = itemView.findViewById(R.id.main_content);
            nameTextView = itemView.findViewById(R.id.restaurant_name);
            addressTextView = itemView.findViewById(R.id.restaurant_address);
            categoryTextView = itemView.findViewById(R.id.restaurant_category);
            ratingBar = itemView.findViewById(R.id.restaurant_rating);
            restaurantImageView = itemView.findViewById(R.id.restaurant_image);
            favoriteImageView = itemView.findViewById(R.id.favorite_icon);
            expandIconView = itemView.findViewById(R.id.expand_icon);
            
            // Initialiser les vues de la section dépliable
            divider = itemView.findViewById(R.id.divider);
            expandedContent = itemView.findViewById(R.id.expanded_content);
            hoursText = itemView.findViewById(R.id.hours_text);
            phoneText = itemView.findViewById(R.id.phone_text);
            shareButton = itemView.findViewById(R.id.share_button);
            directionsButton = itemView.findViewById(R.id.directions_button);
            
            // Configuration des clics
            mainContent.setOnClickListener(v -> {
                if (currentRestaurant != null) {
                    currentRestaurant.toggleExpanded();
                    updateExpansionState();
                }
            });
            
            favoriteImageView.setOnClickListener(v -> {
                if (listener != null && currentRestaurant != null) {
                    listener.onFavoriteClick(currentRestaurant);
                }
            });
            
            shareButton.setOnClickListener(v -> {
                if (currentRestaurant != null) {
                    shareByEmail();
                }
            });
            
            directionsButton.setOnClickListener(v -> {
                if (currentRestaurant != null) {
                    openMapsDirections();
                }
            });
        }

        /**
         * Méthode simplifiée pour lier les données du restaurant au ViewHolder
         */
        public void bind(Restaurant restaurant) {
            // Stocker une référence au restaurant actuel
            this.currentRestaurant = restaurant;
            
            // Définir les textes de base (toujours vérifier les valeurs null)
            nameTextView.setText(restaurant.getName() != null ? restaurant.getName() : "Restaurant");
            addressTextView.setText(restaurant.getAddress() != null ? restaurant.getAddress() : "Adresse non disponible");
            categoryTextView.setText(restaurant.getPlaceType() != null ? restaurant.getPlaceType() : "restaurant");
            
            // Définir la note
            ratingBar.setRating((float) restaurant.getRating());
            
            // Définir l'icône de favori avec un log pour déboguer
            boolean isFavorite = restaurant.isFavorite();
            Log.d("RestaurantAdapter", "Restaurant " + restaurant.getName() + " (ID: " + restaurant.getId() + ") - isFavorite: " + isFavorite);
            favoriteImageView.setImageResource(isFavorite ? 
                    R.drawable.ic_favorite : R.drawable.ic_favorite_border);
            
            // Charger l'image avec une approche simple
            loadRestaurantImage(restaurant);
            
            // Définir les informations supplémentaires
            if (restaurant.getOpeningHours() != null && !restaurant.getOpeningHours().isEmpty()) {
                hoursText.setText(restaurant.getOpeningHours());
            } else {
                hoursText.setText(R.string.no_opening_hours);
            }
            
            if (restaurant.getPhoneNumber() != null && !restaurant.getPhoneNumber().isEmpty()) {
                phoneText.setText(restaurant.getPhoneNumber());
            } else {
                phoneText.setText(R.string.no_phone_number);
            }
            
            // Mettre à jour l'état d'expansion
            updateExpansionState();
        }
        
        /**
         * Méthode simplifiée pour charger l'image du restaurant
         */
        private void loadRestaurantImage(Restaurant restaurant) {
            // Définir l'image par défaut immédiatement
            restaurantImageView.setImageResource(R.drawable.placeholder_restaurant);
            
            // Tenter de charger l'image réelle si elle est disponible
            if (restaurant.getPhotoUrl() != null && !restaurant.getPhotoUrl().isEmpty()) {
                try {
                    Glide.with(context)
                            .load(restaurant.getPhotoUrl())
                            .placeholder(R.drawable.placeholder_restaurant)
                            .error(R.drawable.placeholder_restaurant)
                            .centerCrop()
                            .into(restaurantImageView);
                } catch (Exception e) {
                    Log.e(TAG, "Erreur lors du chargement de l'image: " + e.getMessage());
                }
            }
        }
        
        /**
         * Met à jour l'état d'expansion de l'élément
         */
        private void updateExpansionState() {
            boolean isExpanded = currentRestaurant != null && currentRestaurant.isExpanded();
            
            // Mettre à jour l'icône
            expandIconView.setImageResource(isExpanded ? 
                    R.drawable.ic_expand_less : R.drawable.ic_expand_more);
            
            // Mettre à jour la visibilité
            divider.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            expandedContent.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        }
        
        /**
         * Partage les informations du restaurant par email
         */
        private void shareByEmail() {
            if (currentRestaurant == null || context == null) return;
            
            // Créer un lien Google Maps vers le restaurant
            String mapsLink = String.format("https://www.google.com/maps/search/?api=1&query=%s,%s", 
                    currentRestaurant.getLatitude(), currentRestaurant.getLongitude());
            
            // Construire le contenu de l'email
            String subject = context.getString(R.string.share_email_subject);
            String body = context.getString(R.string.share_email_body, 
                    currentRestaurant.getName(), 
                    currentRestaurant.getAddress(), 
                    currentRestaurant.getRating(),
                    mapsLink);
            
            // Créer l'intent pour le partage
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, body);
            
            try {
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_by_email)));
            } catch (Exception e) {
                Toast.makeText(context, "Aucune application pour partager n'a été trouvée", Toast.LENGTH_SHORT).show();
            }
        }
        
        /**
         * Ouvre Google Maps pour obtenir l'itinéraire vers le restaurant
         */
        private void openMapsDirections() {
            if (currentRestaurant == null || context == null) return;
            
            // Ouvrir Google Maps avec l'itinéraire vers le restaurant
            Uri gmmIntentUri = Uri.parse(String.format("google.navigation:q=%s,%s&mode=d",
                    currentRestaurant.getLatitude(), currentRestaurant.getLongitude()));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            
            try {
                context.startActivity(mapIntent);
            } catch (Exception e) {
                Toast.makeText(context, "Google Maps n'est pas installé", Toast.LENGTH_SHORT).show();
            }
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
                    oldItem.isFavorite() == newItem.isFavorite() &&
                    oldItem.isExpanded() == newItem.isExpanded();
        }
    }
} 
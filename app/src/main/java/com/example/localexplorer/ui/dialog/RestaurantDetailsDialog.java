package com.example.localexplorer.ui.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.localexplorer.R;
import com.example.localexplorer.data.remote.geoapify.PlaceDetailsService;
import com.example.localexplorer.model.Restaurant;
import com.example.localexplorer.viewmodel.RestaurantViewModel;
import com.google.android.material.button.MaterialButton;

/**
 * DialogFragment pour afficher les détails d'un restaurant
 */
public class RestaurantDetailsDialog extends DialogFragment {

    private static final String ARG_RESTAURANT = "restaurant";

    private Restaurant restaurant;
    private RestaurantViewModel viewModel;
    private PlaceDetailsService placeDetailsService;

    private ImageView photoImageView;
    private TextView nameTextView;
    private RatingBar ratingBar;
    private TextView ratingValueTextView;
    private TextView priceTextView;
    private TextView categoryTextView;
    private TextView addressTextView;
    private TextView phoneLabel;
    private TextView phoneTextView;
    private TextView websiteLabel;
    private TextView websiteTextView;
    private TextView openingHoursLabel;
    private TextView openingHoursTextView;
    private TextView coordinatesTextView;
    private Button directionsButton;
    private MaterialButton favoriteButton;
    private MaterialButton shareEmailButton;
    private ImageView closeButton;

    /**
     * Crée une nouvelle instance du dialog avec les informations du restaurant
     */
    public static RestaurantDetailsDialog newInstance(Restaurant restaurant) {
        RestaurantDetailsDialog fragment = new RestaurantDetailsDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RESTAURANT, restaurant);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogTheme);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_RESTAURANT)) {
            restaurant = (Restaurant) args.getSerializable(ARG_RESTAURANT);
        }
        
        placeDetailsService = new PlaceDetailsService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_restaurant_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser les vues
        initViews(view);
        
        // Initialiser le ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(RestaurantViewModel.class);

        // Fermer le dialog
        closeButton.setOnClickListener(v -> dismiss());

        // Si nous avons un restaurant, remplir les détails
        if (restaurant != null) {
            populateRestaurantDetails();
            loadAdditionalDetails();
        }

        // Configurer le bouton d'itinéraire
        directionsButton.setOnClickListener(v -> openMapsDirections());

        // Configurer le bouton de favori
        updateFavoriteButton();
        favoriteButton.setOnClickListener(v -> toggleFavorite());
        
        // Configurer le bouton de partage par email
        shareEmailButton.setOnClickListener(v -> shareByEmail());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private void initViews(View view) {
        photoImageView = view.findViewById(R.id.restaurant_photo);
        nameTextView = view.findViewById(R.id.restaurant_name);
        ratingBar = view.findViewById(R.id.restaurant_rating);
        ratingValueTextView = view.findViewById(R.id.restaurant_rating_value);
        priceTextView = view.findViewById(R.id.restaurant_price_level);
        categoryTextView = view.findViewById(R.id.restaurant_category);
        addressTextView = view.findViewById(R.id.restaurant_address);
        phoneLabel = view.findViewById(R.id.phone_label);
        phoneTextView = view.findViewById(R.id.restaurant_phone);
        websiteLabel = view.findViewById(R.id.website_label);
        websiteTextView = view.findViewById(R.id.restaurant_website);
        openingHoursLabel = view.findViewById(R.id.opening_hours_label);
        openingHoursTextView = view.findViewById(R.id.restaurant_opening_hours);
        coordinatesTextView = view.findViewById(R.id.restaurant_coordinates);
        directionsButton = view.findViewById(R.id.directions_button);
        favoriteButton = view.findViewById(R.id.favorite_button);
        shareEmailButton = view.findViewById(R.id.share_email_button);
        closeButton = view.findViewById(R.id.close_button);
    }

    private void populateRestaurantDetails() {
        // Définir le nom du restaurant
        nameTextView.setText(restaurant.getName());

        // Définir la note
        float rating = (float) restaurant.getRating();
        ratingBar.setRating(rating);
        ratingValueTextView.setText(String.valueOf(rating));

        // Définir le niveau de prix
        if (restaurant.getPriceLevel() != null && !restaurant.getPriceLevel().isEmpty()) {
            priceTextView.setText(restaurant.getPriceLevel());
            priceTextView.setVisibility(View.VISIBLE);
        } else {
            priceTextView.setVisibility(View.GONE);
        }

        // Définir la catégorie
        categoryTextView.setText(restaurant.getPlaceType());

        // Définir l'adresse
        addressTextView.setText(restaurant.getAddress());

        // Définir les coordonnées
        String coordinates = getString(R.string.lat_long_format, 
                restaurant.getLatitude(), restaurant.getLongitude());
        coordinatesTextView.setText(coordinates);

        // Charger l'image du restaurant
        if (restaurant.getPhotoUrl() != null && !restaurant.getPhotoUrl().isEmpty()) {
            Glide.with(requireContext())
                    .load(restaurant.getPhotoUrl())
                    .placeholder(R.drawable.placeholder_restaurant)
                    .error(R.drawable.placeholder_restaurant)
                    .centerCrop()
                    .into(photoImageView);
        } else {
            photoImageView.setImageResource(R.drawable.placeholder_restaurant);
        }

        // Définir le numéro de téléphone si disponible
        if (restaurant.getPhoneNumber() != null && !restaurant.getPhoneNumber().isEmpty()) {
            phoneLabel.setVisibility(View.VISIBLE);
            phoneTextView.setVisibility(View.VISIBLE);
            phoneTextView.setText(restaurant.getPhoneNumber());
            phoneTextView.setOnClickListener(v -> callRestaurant());
        } else {
            phoneLabel.setVisibility(View.GONE);
            phoneTextView.setVisibility(View.GONE);
        }

        // Définir le site web si disponible
        if (restaurant.getWebsite() != null && !restaurant.getWebsite().isEmpty()) {
            websiteLabel.setVisibility(View.VISIBLE);
            websiteTextView.setVisibility(View.VISIBLE);
            websiteTextView.setText(restaurant.getWebsite());
            websiteTextView.setOnClickListener(v -> openWebsite());
        } else {
            websiteLabel.setVisibility(View.GONE);
            websiteTextView.setVisibility(View.GONE);
        }

        // Définir les horaires d'ouverture si disponibles
        if (restaurant.getOpeningHours() != null && !restaurant.getOpeningHours().isEmpty()) {
            openingHoursLabel.setVisibility(View.VISIBLE);
            openingHoursTextView.setVisibility(View.VISIBLE);
            openingHoursTextView.setText(restaurant.getOpeningHours());
        } else {
            openingHoursLabel.setVisibility(View.GONE);
            openingHoursTextView.setVisibility(View.GONE);
        }
    }

    private void loadAdditionalDetails() {
        Toast.makeText(requireContext(), R.string.loading_details, Toast.LENGTH_SHORT).show();
        
        placeDetailsService.loadPlaceDetails(restaurant, new PlaceDetailsService.PlaceDetailsCallback() {
            @Override
            public void onSuccess(Restaurant updatedRestaurant) {
                if (getActivity() != null && !getActivity().isFinishing() && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        // Mettre à jour l'affichage avec les informations supplémentaires
                        populateRestaurantDetails();
                    });
                }
            }

            @Override
            public void onError(String message) {
                if (getActivity() != null && !getActivity().isFinishing() && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void openMapsDirections() {
        // Ouvrir Google Maps avec l'itinéraire vers le restaurant
        Uri gmmIntentUri = Uri.parse(String.format("google.navigation:q=%s,%s&mode=d",
                restaurant.getLatitude(), restaurant.getLongitude()));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    private void callRestaurant() {
        if (restaurant.getPhoneNumber() != null && !restaurant.getPhoneNumber().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + restaurant.getPhoneNumber()));
            startActivity(intent);
        }
    }

    private void openWebsite() {
        if (restaurant.getWebsite() != null && !restaurant.getWebsite().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(restaurant.getWebsite()));
            startActivity(intent);
        }
    }

    private void toggleFavorite() {
        // Basculer l'état du favori
        viewModel.toggleFavorite(restaurant);
        
        // Mettre à jour l'apparence du bouton (l'état sera actualisé via LiveData)
        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        if (restaurant != null) {
            // Mettre à jour l'icône et le texte du bouton
            favoriteButton.setIcon(restaurant.isFavorite() 
                    ? AppCompatResources.getDrawable(requireContext(), R.drawable.ic_favorite)
                    : AppCompatResources.getDrawable(requireContext(), R.drawable.ic_favorite_border));
            
            favoriteButton.setText(restaurant.isFavorite() 
                    ? R.string.remove_favorite 
                    : R.string.favorite);
                    
            // Afficher le bouton de partage par email uniquement si le restaurant est un favori
            shareEmailButton.setVisibility(restaurant.isFavorite() ? View.VISIBLE : View.GONE);
        }
    }
    
    /**
     * Partage les détails du restaurant par email
     */
    private void shareByEmail() {
        if (restaurant == null) return;
        
        // Créer un lien Google Maps vers le restaurant
        String mapsLink = String.format("https://www.google.com/maps/search/?api=1&query=%s,%s", 
                restaurant.getLatitude(), restaurant.getLongitude());
        
        // Construire le contenu de l'email
        String subject = getString(R.string.share_email_subject);
        String body = getString(R.string.share_email_body, 
                restaurant.getName(), 
                restaurant.getAddress(), 
                restaurant.getRating(),
                mapsLink);
        
        // Créer l'intent pour le partage général (plus compatible avec différentes applications)
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        
        // Créer un intent chooser pour permettre à l'utilisateur de choisir l'application
        Intent chooser = Intent.createChooser(intent, getString(R.string.share_by_email));
        
        try {
            startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Aucune application pour partager n'a été trouvée", Toast.LENGTH_SHORT).show();
        }
    }
} 
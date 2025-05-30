package com.example.localexplorer.ui.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.localexplorer.R;
import com.example.localexplorer.model.Restaurant;
import com.example.localexplorer.ui.dialog.RestaurantDetailsDialog;
import com.example.localexplorer.viewmodel.RestaurantViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment pour afficher les restaurants sur une carte
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float DEFAULT_ZOOM = 15f;

    private GoogleMap map;
    private RestaurantViewModel viewModel;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private FloatingActionButton myLocationFab;
    private FusedLocationProviderClient fusedLocationClient;
    private Map<String, Restaurant> restaurantMarkers = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        errorTextView = view.findViewById(R.id.errorTextView);
        myLocationFab = view.findViewById(R.id.myLocationFab);

        // Initialiser le ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(RestaurantViewModel.class);

        // Initialiser la carte
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialiser le fournisseur de localisation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Configurer le bouton de localisation
        myLocationFab.setOnClickListener(v -> requestUserLocation());

        // Observer les changements dans les restaurants filtrés
        viewModel.getFilteredRestaurants().observe(getViewLifecycleOwner(), this::updateMapMarkers);

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
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        // Configurer la carte
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setOnInfoWindowClickListener(this::onMarkerInfoWindowClick);
        map.setOnMarkerClickListener(this);

        // Demander la localisation si la permission est accordée
        requestUserLocation();
    }

    /**
     * Demande la position de l'utilisateur
     */
    private void requestUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Activer le bouton de localisation sur la carte
        if (map != null) {
            map.setMyLocationEnabled(true);
        }

        // Obtenir la dernière position connue
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                // Mettre à jour le ViewModel avec la position actuelle
                viewModel.updateLocation(location.getLatitude(), location.getLongitude());

                // Déplacer la caméra vers la position actuelle
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));
            } else {
                // Utiliser les coordonnées par défaut (Marseille)
                Toast.makeText(requireContext(), R.string.marseille_default, Toast.LENGTH_SHORT).show();
                LatLng defaultLocation = new LatLng(viewModel.getCurrentLatitude().getValue(), viewModel.getCurrentLongitude().getValue());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
            }
        });
    }

    /**
     * Met à jour les marqueurs sur la carte
     */
    private void updateMapMarkers(List<Restaurant> restaurants) {
        if (map == null || restaurants == null) {
            return;
        }

        // Effacer tous les marqueurs
        map.clear();
        restaurantMarkers.clear();

        // Ajouter les nouveaux marqueurs
        for (Restaurant restaurant : restaurants) {
            LatLng position = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(position)
                    .title(restaurant.getName())
                    .snippet(restaurant.getAddress()));
            
            if (marker != null) {
                restaurantMarkers.put(marker.getId(), restaurant);
            }
        }
    }

    /**
     * Gère le clic sur la fenêtre d'info d'un marqueur
     */
    private void onMarkerInfoWindowClick(Marker marker) {
        Restaurant restaurant = restaurantMarkers.get(marker.getId());
        if (restaurant != null) {
            showRestaurantDetails(restaurant);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Restaurant restaurant = restaurantMarkers.get(marker.getId());
        if (restaurant != null) {
            showRestaurantDetails(restaurant);
            return true;
        }
        return false;
    }

    /**
     * Affiche les détails d'un restaurant dans une popup modale
     */
    private void showRestaurantDetails(Restaurant restaurant) {
        RestaurantDetailsDialog dialog = RestaurantDetailsDialog.newInstance(restaurant);
        dialog.show(getParentFragmentManager(), "RestaurantDetailsDialog");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestUserLocation();
            } else {
                Toast.makeText(requireContext(), R.string.marseille_default, Toast.LENGTH_SHORT).show();
            }
        }
    }
} 
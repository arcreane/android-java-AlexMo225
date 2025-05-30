package com.example.localexplorer.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Classe modèle pour représenter un restaurant
 */
@Entity(tableName = "restaurants")
public class Restaurant implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private double rating;
    private String photoUrl;
    private String placeType;
    private boolean isFavorite;
    private String openingHours;
    private String priceLevel;
    private String phoneNumber;
    private String website;
    private boolean isExpanded;

    public Restaurant(@NonNull String id, String name, String address, double latitude, double longitude, double rating, String photoUrl, String placeType) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.photoUrl = photoUrl;
        this.placeType = placeType;
        this.isFavorite = false;
        this.openingHours = null;
        this.priceLevel = null;
        this.phoneNumber = null;
        this.website = null;
        this.isExpanded = false;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPlaceType() {
        return placeType;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
    
    public String getOpeningHours() {
        return openingHours;
    }
    
    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }
    
    public String getPriceLevel() {
        return priceLevel;
    }
    
    public void setPriceLevel(String priceLevel) {
        this.priceLevel = priceLevel;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
    }
    
    /**
     * Vérifie si l'élément est déplié dans la liste
     */
    public boolean isExpanded() {
        return isExpanded;
    }
    
    /**
     * Définit l'état d'expansion de l'élément dans la liste
     */
    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
    
    /**
     * Bascule l'état d'expansion
     */
    public void toggleExpanded() {
        isExpanded = !isExpanded;
    }
} 
package com.example.localexplorer.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.localexplorer.model.Restaurant;

import java.util.List;

/**
 * Interface DAO pour les opérations CRUD sur les restaurants
 */
@Dao
public interface RestaurantDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Restaurant restaurant);
    
    @Update
    void update(Restaurant restaurant);
    
    @Delete
    void delete(Restaurant restaurant);
    
    @Query("SELECT * FROM restaurants WHERE isFavorite = 1")
    LiveData<List<Restaurant>> getAllFavorites();
    
    @Query("SELECT * FROM restaurants WHERE id = :id")
    LiveData<Restaurant> getRestaurantById(String id);
    
    @Query("UPDATE restaurants SET isFavorite = :isFavorite WHERE id = :id")
    void updateFavoriteStatus(String id, boolean isFavorite);
} 
package com.example.fitsnacks.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.lifecycle.LiveData;
import java.util.List;

@Dao
public interface SnackDao {
    @Insert
    void insert(SnackEntry snack);

    @Query("SELECT * FROM snacks ORDER BY date DESC, id DESC")
    LiveData<List<SnackEntry>> getAllSnacks();

    @Query("SELECT SUM(calories) FROM snacks")
    LiveData<Integer> getTotalCalories();

    @Query("DELETE FROM snacks WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM snacks WHERE date = :date ORDER BY id DESC")
    LiveData<List<SnackEntry>> getSnacksByDate(String date);
}
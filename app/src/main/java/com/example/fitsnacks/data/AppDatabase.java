package com.example.fitsnacks.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {SnackEntry.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SnackDao snackDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "fitsnacks_database"
                            ).fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
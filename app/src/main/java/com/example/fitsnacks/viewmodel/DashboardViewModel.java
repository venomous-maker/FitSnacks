package com.example.fitsnacks.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.fitsnacks.data.SnackEntry;
import com.example.fitsnacks.data.SnackRepository;
import java.util.List;
import com.example.fitsnacks.util.Callback;

public class DashboardViewModel extends AndroidViewModel {
    private final SnackRepository repo;
    public final LiveData<List<SnackEntry>> allSnacks;
    public final LiveData<Integer> totalCalories;
    private final MutableLiveData<Integer> caloriesBurned = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> netCalories = new MutableLiveData<>(0);

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repo = new SnackRepository(application);
        allSnacks = repo.getAllSnacks();
        totalCalories = repo.getTotalCalories();

        // Observe total calories to calculate net calories
        totalCalories.observeForever(total -> {
            Integer burned = caloriesBurned.getValue();
            if (burned == null) burned = 0;
            netCalories.setValue(total - burned);
        });
    }

    public void insertSnack(SnackEntry entry) {
        repo.insert(entry);
    }

    public void insertSnack(SnackEntry entry, @Nullable Callback<Long> onComplete) {
        repo.insert(entry, onComplete);
    }

    public LiveData<Integer> getCaloriesBurned() {
        return caloriesBurned;
    }

    public LiveData<Integer> getNetCalories() {
        return netCalories;
    }

    public void setCaloriesBurned(int calories) {
        caloriesBurned.setValue(calories);
    }

    public void deleteSnack(long id) {
        // Delegate deletion to repository
        repo.delete(id);
    }

    /**
     * Switch repository to a different user (email) or guest. Pass null to use guest.
     */
    public void switchUser(@Nullable String email) {
        repo.switchUser(email);
        // update any local derived LiveData if needed (repo already posts new values)
    }
}
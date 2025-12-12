package com.example.fitsnacks.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SnackRepository {
    private static final String PREFS = "fitsnacks_prefs";
    private static final String KEY_SNACKS = "snacks_json";

    // Auth prefs used to determine current user (email) or guest
    private static final String AUTH_PREFS = "auth_prefs";
    private static final String AUTH_EMAIL = "auth_email";
    private static final String AUTH_LOGGED_IN = "auth_logged_in";

    // Current user id (sanitized email) or "guest"
    private String currentUserId = "guest";

    private final SharedPreferences prefs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String TAG = "SnackRepository";

    private final MutableLiveData<List<SnackEntry>> snacksLive = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalCaloriesLive = new MutableLiveData<>();

    public SnackRepository(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        // determine current user from auth prefs (fallback to guest)
        SharedPreferences auth = context.getApplicationContext().getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE);
        boolean isLogged = auth.getBoolean(AUTH_LOGGED_IN, false);
        String email = auth.getString(AUTH_EMAIL, null);
        if (isLogged && email != null && !email.isEmpty()) {
            currentUserId = sanitizeUserId(email);
        } else {
            currentUserId = "guest";
        }

        List<SnackEntry> list = loadFromPrefsForUser(currentUserId);
        snacksLive.setValue(list);
        totalCaloriesLive.setValue(calculateTotal(list));
    }

    /**
     * Switch repository to a different user (email) or guest. Provides simple per-user scoping for stored snacks.
     * Pass null or empty email to switch to guest.
     */
    public void switchUser(@Nullable String email) {
        if (email == null || email.isEmpty()) {
            currentUserId = "guest";
        } else {
            currentUserId = sanitizeUserId(email);
        }
        List<SnackEntry> list = loadFromPrefsForUser(currentUserId);
        snacksLive.postValue(list);
        totalCaloriesLive.postValue(calculateTotal(list));
    }

    public LiveData<List<SnackEntry>> getAllSnacks() {
        return snacksLive;
    }

    public LiveData<Integer> getTotalCalories() {
        return totalCaloriesLive;
    }

    public void insert(final SnackEntry entry) {
        insert(entry, null);
    }

    /**
     * Insert snack and optionally receive the assigned id on the main thread via callback.
     */
    public void insert(final SnackEntry entry, @Nullable java.util.function.Consumer<Long> callback) {
        executor.execute(() -> {
            List<SnackEntry> current = snacksLive.getValue();
            if (current == null) current = new ArrayList<>();
            // assign id
            long maxId = 0;
            for (SnackEntry s : current) if (s.id > maxId) maxId = s.id;
            entry.id = maxId + 1;
            current.add(0, entry); // add to front (newest first)
            saveToPrefs(current);
            snacksLive.postValue(Collections.unmodifiableList(new ArrayList<>(current)));
            totalCaloriesLive.postValue(calculateTotal(current));
            if (callback != null) {
                // post callback on main thread
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.accept(entry.id));
            }
        });
    }

    public void delete(final long id) {
        executor.execute(() -> {
            List<SnackEntry> current = snacksLive.getValue();
            if (current == null) return;
            List<SnackEntry> updated = new ArrayList<>();
            for (SnackEntry s : current) {
                if (s.id != id) updated.add(s);
            }
            saveToPrefs(updated);
            snacksLive.postValue(Collections.unmodifiableList(updated));
            totalCaloriesLive.postValue(calculateTotal(updated));
        });
    }

    private int calculateTotal(List<SnackEntry> list) {
        if (list == null) return 0;
        int sum = 0;
        for (SnackEntry s : list) sum += s.calories;
        return sum;
    }

    private List<SnackEntry> loadFromPrefsForUser(String userId) {
        String key = prefsKeyFor(userId);
        String json = prefs.getString(key, null);
        if (json == null) return new ArrayList<>();
        List<SnackEntry> out = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                SnackEntry s = new SnackEntry();
                s.id = o.optLong("id", i+1);
                s.name = o.optString("name", "");
                s.calories = o.optInt("calories", 0);
                s.portion = o.optString("portion", "");
                s.date = o.optString("date", "");
                out.add(s);
            }
        } catch (JSONException e) {
            // log and continue with empty list
            android.util.Log.w(TAG, "Failed to parse snacks JSON", e);
        }
        return out;
    }

    private void saveToPrefs(List<SnackEntry> list) {
        JSONArray arr = new JSONArray();
        try {
            for (SnackEntry s : list) {
                JSONObject o = new JSONObject();
                o.put("id", s.id);
                o.put("name", s.name);
                o.put("calories", s.calories);
                o.put("portion", s.portion);
                o.put("date", s.date);
                arr.put(o);
            }
            String key = prefsKeyFor(currentUserId);
            prefs.edit().putString(key, arr.toString()).apply();
        } catch (JSONException e) {
            android.util.Log.w(TAG, "Failed to serialize snacks JSON", e);
        }
    }

    private String prefsKeyFor(String userId) {
        return KEY_SNACKS + ":" + userId;
    }

    private String sanitizeUserId(String email) {
        // very simple sanitation for preference keys
        return email.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}

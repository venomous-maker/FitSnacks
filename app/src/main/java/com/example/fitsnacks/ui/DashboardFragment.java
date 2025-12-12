package com.example.fitsnacks.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.MenuItem;
import android.content.SharedPreferences;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitsnacks.R;
import com.example.fitsnacks.data.SnackEntry;
import com.example.fitsnacks.viewmodel.DashboardViewModel;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;

public class DashboardFragment extends Fragment {
    private TextView caloriesEatenText, caloriesBurnedText, netCaloriesText;
    private DashboardViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        // Setup toolbar (local variable)
        final MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            // open drawer in host activity
            DrawerLayout drawer = requireActivity().findViewById(R.id.drawer_layout);
            if (drawer != null) drawer.openDrawer(GravityCompat.START);
        });

        // Setup navigation view listener (if present)
        NavigationView nav = requireActivity().findViewById(R.id.nav_view);
        if (nav != null) {
            nav.setNavigationItemSelectedListener(menuItem -> {
                int id = menuItem.getItemId();
                if (id == R.id.nav_all_snacks) {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new AllSnacksFragment())
                            .addToBackStack(null)
                            .commit();
                } else if (id == R.id.nav_settings) {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new SettingsFragment())
                            .addToBackStack(null)
                            .commit();
                } else if (id == R.id.nav_sign_out) {
                    SharedPreferences auth = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
                    auth.edit().remove("auth_email").putBoolean("auth_logged_in", false).apply();
                    vm.switchUser(null);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new com.example.fitsnacks.ui.LoginFragment())
                            .commit();
                }
                DrawerLayout d = requireActivity().findViewById(R.id.drawer_layout);
                if (d != null) d.closeDrawer(GravityCompat.START);
                return true;
            });
        }

        caloriesEatenText = view.findViewById(R.id.calories_eaten);
        caloriesBurnedText = view.findViewById(R.id.calories_burned);
        netCaloriesText = view.findViewById(R.id.net_calories);

        final RecyclerView historyList = view.findViewById(R.id.history_list);
        historyList.setLayoutManager(new LinearLayoutManager(getContext()));
        final SnackListAdapter adapter = new SnackListAdapter(this::showDeleteDialog);
        adapter.setMaxItems(5); // show only 5 recent snacks on dashboard
        historyList.setAdapter(adapter);

        // Set click listener for calories burned
        view.findViewById(R.id.calories_burned).setOnClickListener(v -> showCaloriesBurnedDialog());

        // Set click listener for quick action cards
        View cardAddSnack = view.findViewById(R.id.card_add_snack);
        if (cardAddSnack != null) cardAddSnack.setOnClickListener(v -> navigateToAddSnack());
        View cardAddWorkout = view.findViewById(R.id.card_add_workout);
        if (cardAddWorkout != null) cardAddWorkout.setOnClickListener(v -> navigateToAddWorkout());

        // View All - navigate to full list
        View viewAll = view.findViewById(R.id.btn_view_all);
        if (viewAll != null) viewAll.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new AllSnacksFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Observe ViewModel data (updates are posted by repository)
        vm.totalCalories.observe(getViewLifecycleOwner(), total -> {
            int value = total == null ? 0 : total;
            caloriesEatenText.setText(String.valueOf(value));
            updateNetCalories();
        });

        vm.getCaloriesBurned().observe(getViewLifecycleOwner(), burned -> {
            int value = burned == null ? 0 : burned;
            caloriesBurnedText.setText(String.valueOf(value));
            updateNetCalories();
        });

        // Update list when snacks change
        vm.allSnacks.observe(getViewLifecycleOwner(), list -> {
            adapter.setItems(list);
        });

        // FAB
        View fab = view.findViewById(R.id.fab_add_snack);
        if (fab != null) fab.setOnClickListener(v -> navigateToAddSnack());
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sign_out) {
            // clear auth prefs and switch to guest
            SharedPreferences auth = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
            auth.edit().remove("auth_email").remove("auth_password").putBoolean("auth_logged_in", false).apply();
            vm.switchUser(null);
            // navigate back to login
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new com.example.fitsnacks.ui.LoginFragment())
                    .commit();
            return true;
        } else if (id == R.id.action_settings) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new SettingsFragment())
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return false;
    }

    private void updateNetCalories() {
        Integer eaten = vm.totalCalories.getValue();
        Integer burned = vm.getCaloriesBurned().getValue();

        int eatenVal = eaten == null ? 0 : eaten;
        int burnedVal = burned == null ? 0 : burned;
        int net = eatenVal - burnedVal;

        netCaloriesText.setText(String.valueOf(net));

        if (net > 0) {
            netCaloriesText.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.error));
        } else if (net < 0) {
            netCaloriesText.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.success));
        } else {
            netCaloriesText.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_primary));
        }
    }

    private void showDeleteDialog(SnackEntry snack) {
        if (snack == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_delete_title)
                .setMessage("Delete " + snack.name + "?")
                .setPositiveButton(R.string.dialog_delete_confirm, (dialog, which) -> vm.deleteSnack(snack.id))
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showCaloriesBurnedDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_calories_burned, null);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_calories_burned_title)
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    TextView input = dialogView.findViewById(R.id.input_calories_burned);
                    try {
                        int calories = Integer.parseInt(input.getText().toString());
                        vm.setCaloriesBurned(calories);
                    } catch (NumberFormatException e) {
                        // Keep previous value
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToAddSnack() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new AddSnackFragment())
                .addToBackStack(null)
                .commit();
    }

    private void navigateToAddWorkout() {
        // small placeholder fragment - reuse AddSnack for now or implement AddWorkoutFragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new AddSnackFragment())
                .addToBackStack(null)
                .commit();
    }

    private void navigateToAllSnacks() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new AllSnacksFragment())
                .addToBackStack(null)
                .commit();
    }
}
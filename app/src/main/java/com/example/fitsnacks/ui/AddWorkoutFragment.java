// Name : pooja bandari
// Course: open - source intelligent device (ITMD- 555)
// Project : FitSnacks
package com.example.fitsnacks.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.fitsnacks.R;
import com.example.fitsnacks.viewmodel.DashboardViewModel;
import com.google.android.material.snackbar.Snackbar;

public class AddWorkoutFragment extends Fragment {
    private DashboardViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        EditText name = view.findViewById(R.id.input_workout_name);
        EditText calories = view.findViewById(R.id.input_workout_calories);

        view.findViewById(R.id.btn_save_workout).setOnClickListener(v -> {
            String n = name.getText() == null ? "" : name.getText().toString().trim();
            String c = calories.getText() == null ? "" : calories.getText().toString().trim();
            if (TextUtils.isEmpty(n)) {
                name.setError("Enter workout name");
                return;
            }
            int cal = 0;
            try {
                cal = Integer.parseInt(c);
            } catch (NumberFormatException e) {
                calories.setError("Enter numeric calories");
                return;
            }
            // set calories burned in ViewModel (this app tracks total burned as simple integer)
            Integer prev = vm.getCaloriesBurned().getValue();
            int prevVal = prev == null ? 0 : prev;
            vm.setCaloriesBurned(prevVal + cal);

            View root = requireActivity().findViewById(android.R.id.content);
            if (root == null) root = view;
            final int added = cal;
            Snackbar.make(root, "Workout added", Snackbar.LENGTH_LONG)
                    .setAction("Undo", undoV -> vm.setCaloriesBurned(prevVal))
                    .show();

            Toast.makeText(requireContext(), "Workout added", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        view.findViewById(R.id.btn_cancel_workout).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());
    }
}


package com.example.fitsnacks.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.fitsnacks.R;
import com.example.fitsnacks.data.SnackEntry;
import com.example.fitsnacks.viewmodel.DashboardViewModel;
import com.google.android.material.snackbar.Snackbar;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AddSnackFragment extends Fragment {
    private DashboardViewModel vm;
    private EditText nameInput, caloriesInput, portionInput, dateInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_snack, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        nameInput = view.findViewById(R.id.input_snack_name);
        caloriesInput = view.findViewById(R.id.input_snack_calories);
        portionInput = view.findViewById(R.id.input_snack_portion);
        dateInput = view.findViewById(R.id.input_snack_date);

        // default date to today
        dateInput.setText(LocalDate.now().toString());

        Button btnSave = view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String caloriesStr = caloriesInput.getText().toString().trim();
            String portion = portionInput.getText().toString().trim();
            String date = dateInput.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                nameInput.setError("Please enter snack name");
                return;
            }

            if (TextUtils.isEmpty(caloriesStr)) {
                caloriesInput.setError("Please enter calories");
                return;
            }

            int calories;
            try {
                calories = Integer.parseInt(caloriesStr);
                if (calories <= 0) {
                    caloriesInput.setError("Calories must be positive");
                    return;
                }
                if (calories > 10000) {
                    caloriesInput.setError("Calories too high");
                    return;
                }
            } catch (NumberFormatException e) {
                caloriesInput.setError("Calories must be a number");
                return;
            }

            // Validate date format
            try {
                LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                dateInput.setError("Invalid date format (YYYY-MM-DD)");
                return;
            }

            SnackEntry entry = new SnackEntry(name, calories, portion, date);
            // insert with callback to receive assigned id
            vm.insertSnack(entry, id -> {
                 // Show Snackbar with Undo
                 View root = requireActivity().findViewById(android.R.id.content);
                 if (root == null) root = view;
                 Snackbar.make(root, "Snack saved", Snackbar.LENGTH_LONG)
                         .setAction("Undo", undoV -> vm.deleteSnack(id))
                         .show();
             });

            Toast.makeText(getContext(), "Snack saved", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());
    }
}
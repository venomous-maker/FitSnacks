package com.example.fitsnacks.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitsnacks.R;
import com.example.fitsnacks.viewmodel.DashboardViewModel;

public class SettingsFragment extends Fragment {

    private DashboardViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        EditText emailInput = view.findViewById(R.id.input_switch_email);
        Button switchBtn = view.findViewById(R.id.btn_switch_user);
        Button guestBtn = view.findViewById(R.id.btn_switch_guest);
        TextView accountsTv = view.findViewById(R.id.tv_accounts_list);
        Button clearBtn = view.findViewById(R.id.btn_clear_accounts);

        SharedPreferences auth = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        // show accounts (simple single account stored in auth_prefs)
        String email = auth.getString("auth_email", null);
        if (email == null) accountsTv.setText(getString(R.string.no_accounts));
        else accountsTv.setText(email);

        switchBtn.setOnClickListener(v -> {
            String e = emailInput.getText() == null ? null : emailInput.getText().toString().trim();
            if (e == null || e.isEmpty()) {
                Toast.makeText(requireContext(), "Enter email to switch", Toast.LENGTH_SHORT).show();
                return;
            }
            // set auth prefs to this email and mark logged_in true (no password verification here)
            auth.edit().putString("auth_email", e).putBoolean("auth_logged_in", true).apply();
            vm.switchUser(e);
            Toast.makeText(requireContext(), "Switched user", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        guestBtn.setOnClickListener(v -> {
            auth.edit().remove("auth_email").putBoolean("auth_logged_in", false).apply();
            vm.switchUser(null);
            Toast.makeText(requireContext(), "Switched to guest", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        clearBtn.setOnClickListener(v -> {
            auth.edit().clear().apply();
            Toast.makeText(requireContext(), "Cleared accounts", Toast.LENGTH_SHORT).show();
            accountsTv.setText(getString(R.string.no_accounts));
        });
    }
}

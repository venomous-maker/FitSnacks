// Name : pooja bandari
// Course: open - source intelligent device (ITMD- 555)
// Project : FitSnacks
package com.example.fitsnacks.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.fitsnacks.viewmodel.DashboardViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.text.TextUtils;

public class LoginFragment extends Fragment {

    private static final String PREFS_AUTH = "auth_prefs";
    private static final String KEY_EMAIL = "auth_email"; // last used email
    private static final String KEY_ACCOUNTS = "accounts_json"; // JSON object {email: hashedPassword}
    private static final String KEY_LOGGED_IN = "auth_logged_in";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText emailInput = view.findViewById(R.id.input_email);
        EditText passwordInput = view.findViewById(R.id.input_password);
        Button signIn = view.findViewById(R.id.btn_sign_in);
        Button guest = view.findViewById(R.id.btn_continue_guest);

        SharedPreferences prefs = requireContext().getApplicationContext().getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE);
        // obtain DashboardViewModel to notify repository about current user
        DashboardViewModel vm = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        // Continue as guest: don't persist credentials, mark not logged in and navigate
        guest.setOnClickListener(v -> {
            prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply();
            // switch repository to guest
            vm.switchUser(null);
            navigateToDashboard();
        });

        // Sign in logic (using accounts_json storing hashed passwords)
        signIn.setOnClickListener(v -> {
            String email = emailInput.getText() == null ? "" : emailInput.getText().toString().trim();
            String password = passwordInput.getText() == null ? "" : passwordInput.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Load accounts JSON into an effectively-final JSONObject for use in lambdas
            String accountsJson = prefs.getString(KEY_ACCOUNTS, null);
            JSONObject accountsTemp;
            try {
                accountsTemp = accountsJson != null ? new JSONObject(accountsJson) : new JSONObject();
            } catch (JSONException e) {
                accountsTemp = new JSONObject();
            }
            final JSONObject accounts = accountsTemp;

            String hashed = hashPassword(password);

            if (accounts.length() == 0 || !accounts.has(email)) {
                // No account exists - offer to create
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Create account")
                        .setMessage("No account found. Create a new account with this email?")
                        .setPositiveButton("Create", (dialog, which) -> {
                            try {
                                accounts.put(email, hashed);
                                prefs.edit().putString(KEY_ACCOUNTS, accounts.toString()).putString(KEY_EMAIL, email)
                                        .putBoolean(KEY_LOGGED_IN, true).apply();
                            } catch (JSONException ex) {
                                // ignore
                            }
                            // update repository user scope
                            vm.switchUser(email);
                            Toast.makeText(requireContext(), "Account created — logged in", Toast.LENGTH_SHORT).show();
                            navigateToDashboard();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return;
            }

            // Check credentials by hash
            String storedHash = accounts.optString(email, null);
            if (!TextUtils.isEmpty(storedHash) && storedHash.equals(hashed)) {
                prefs.edit().putBoolean(KEY_LOGGED_IN, true).putString(KEY_EMAIL, email).apply();
                // update repository user scope
                vm.switchUser(email);
                Toast.makeText(requireContext(), "Signed in", Toast.LENGTH_SHORT).show();
                navigateToDashboard();
            } else {
                // Offer to reset/create new account if credentials don't match
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Sign in failed")
                        .setMessage("Credentials do not match existing account. Create new account with these credentials?")
                        .setPositiveButton("Create", (dialog, which) -> {
                            try {
                                accounts.put(email, hashed);
                                prefs.edit().putString(KEY_ACCOUNTS, accounts.toString()).putString(KEY_EMAIL, email)
                                        .putBoolean(KEY_LOGGED_IN, true).apply();
                            } catch (JSONException ex) {
                                // ignore
                            }
                            // update repository user scope
                            vm.switchUser(email);
                            Toast.makeText(requireContext(), "Account created — logged in", Toast.LENGTH_SHORT).show();
                            navigateToDashboard();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

        });
    }

    private void navigateToDashboard() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new DashboardFragment())
                .commit();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(password.hashCode());
        }
    }
}

package com.example.fitsnacks;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import com.example.fitsnacks.ui.LoginFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Wire toolbar/drawer when fragments change
        getSupportFragmentManager().addOnBackStackChangedListener(this::setupToolbarAndDrawer);
        // also attempt initial wiring
        setupToolbarAndDrawer();

        if (savedInstanceState == null) {
            // If user already logged in (or opted to continue as guest), skip login
            boolean loggedIn = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getBoolean("auth_logged_in", false);
            if (loggedIn) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new com.example.fitsnacks.ui.DashboardFragment())
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new LoginFragment())
                        .commit();
            }
        }
    }

    private void setupToolbarAndDrawer() {
        // Attempt to find the toolbar from the current fragment view hierarchy
        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView nav = findViewById(R.id.nav_view);
        if (toolbar == null) return;

        // set navigation click to open drawer
        toolbar.setNavigationOnClickListener(v -> {
            if (drawer != null) drawer.openDrawer(androidx.core.view.GravityCompat.START);
        });

        if (nav != null) {
            // set header email if present
            View header = nav.getHeaderView(0);
            if (header != null) {
                android.widget.TextView emailTv = header.findViewById(R.id.nav_header_email);
                String email = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).getString("auth_email", null);
                emailTv.setText(email == null ? "guest" : email);
            }
            nav.setNavigationItemSelectedListener(menuItem -> {
                int id = menuItem.getItemId();
                if (id == R.id.nav_all_snacks) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new com.example.fitsnacks.ui.AllSnacksFragment())
                            .addToBackStack(null)
                            .commit();
                } else if (id == R.id.nav_settings) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new com.example.fitsnacks.ui.SettingsFragment())
                            .addToBackStack(null)
                            .commit();
                } else if (id == R.id.nav_sign_out) {
                    getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).edit().remove("auth_email").putBoolean("auth_logged_in", false).apply();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new com.example.fitsnacks.ui.LoginFragment())
                            .commit();
                }
                if (drawer != null) drawer.closeDrawer(androidx.core.view.GravityCompat.START);
                return true;
            });
        }
    }
}
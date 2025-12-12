package com.example.fitsnacks;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
}
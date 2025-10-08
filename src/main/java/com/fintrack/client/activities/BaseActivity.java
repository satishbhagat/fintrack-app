package com.fintrack.client.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.fintrack.client.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public abstract class BaseActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Note: The actual layout is set in child activities
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_logout) {
            logout();
            return true;
        }

        // Avoid reloading the same activity
        if (itemId == bottomNavigationView.getSelectedItemId()) {
            return false;
        }

        if (itemId == R.id.nav_home) {
            startActivity(new Intent(this, DashboardActivity.class));
        } else if (itemId == R.id.nav_spend) {
            startActivity(new Intent(this, SpendActivity.class)); // Changed from ProfileActivity
        }

        // Add a slide transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        return true;
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("FinTrackPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("AUTH_TOKEN");
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public abstract void setToolbarTitle(String title);
}

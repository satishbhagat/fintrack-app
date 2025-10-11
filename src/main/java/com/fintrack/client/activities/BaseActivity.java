package com.fintrack.client.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.fintrack.client.R;
import com.fintrack.client.utils.UserSession;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public abstract class BaseActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ContentView is set in child activities
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
            startActivity(new Intent(this, SpendActivity.class));
        } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        }

        // Add a slide transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        return true;
    }

    private void logout() {
        // Clear saved token or session data
        SharedPreferences prefs = getSharedPreferences("FinTrackPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("AUTH_TOKEN");
        // Also clear user session data
        UserSession.getInstance().setEmailId(null);
        UserSession.getInstance().setUserId(null);
        UserSession.getInstance().setSalary(null);
        editor.apply();

        // Navigate to LoginActivity and clear the back stack
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Abstract method to be implemented by child activities to set the title
    public abstract void setToolbarTitle(String title);
}


package com.fintrack.client.activities;


import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.fintrack.client.R;
import com.fintrack.client.dto.ChangePasswordRequest;
import com.fintrack.client.dto.GenericResponse;
import com.fintrack.client.network.ApiService;
import com.fintrack.client.network.RetrofitClient;
import com.fintrack.client.utils.UserSession;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity {

    private ApiService apiService;
    private TextInputEditText etProfileEmail, etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnUpdatePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);

        setToolbarTitle("My Profile");

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // Initialize views
        etProfileEmail = findViewById(R.id.etProfileEmail);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);

        // Populate user email
        etProfileEmail.setText(UserSession.getInstance().getEmailId());

        btnUpdatePassword.setOnClickListener(v -> handleChangePassword());
    }

    private void handleChangePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All password fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        ChangePasswordRequest request = new ChangePasswordRequest(
                UserSession.getInstance().getEmailId(),
                currentPassword,
                newPassword
        );

        apiService.changePassword(request).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                    etCurrentPassword.setText("");
                    etNewPassword.setText("");
                    etConfirmPassword.setText("");
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update password. Check your current password.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.getMenu().findItem(R.id.nav_profile).setChecked(true);
    }

    @Override
    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}


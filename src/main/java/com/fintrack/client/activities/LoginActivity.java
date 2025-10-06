package com.fintrack.client.activities;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fintrack.client.R;
import com.fintrack.client.dto.AuthRequest;
import com.fintrack.client.dto.AuthResponse;
import com.fintrack.client.dto.GenericResponse;
import com.fintrack.client.network.RetrofitClient;
import com.fintrack.client.network.ApiService;

import com.fintrack.client.utils.UserSession;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN_ACTIVITY";
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This should match the name of your main layout file
        setContentView(R.layout.activity_main);

        // Initialize ApiService
        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // Bind UI components
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        // Set OnClickListener for the login button
        btnLogin.setOnClickListener(v -> loginUser());

        // Set OnClickListener for the register link
        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthRequest authRequest = new AuthRequest(email, password);
        Call<AuthResponse> call = apiService.loginUser(authRequest);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(LoginActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    populateUserSession(response);
                    Log.d(TAG," Logged in user: " + email);
                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    intent.putExtra("USER_EMAIL", email);
                    startActivity(intent);
                    finish(); // Close the login activity
                } else {
                    // Handle API errors (e.g., wrong credentials)
                    Toast.makeText(LoginActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_LONG).show();
                }
            }

            private void populateUserSession(Response<AuthResponse> response) {
                UserSession.getInstance().setEmailId(email);
                UserSession.getInstance().setUserId(response.body().getUserId());
                UserSession.getInstance().setSalary(response.body().getSalary());
                Log.d(TAG, "User session populated: " + UserSession.getInstance());
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                // Handle network errors
                Toast.makeText(LoginActivity.this, "An error occurred: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}



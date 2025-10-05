package com.fintrack.client.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fintrack.client.network.ApiService;
import com.fintrack.client.network.RetrofitClient;
import com.fintrack.client.models.RegisterRequest;
import com.fintrack.client.models.AuthResponse;

import com.fintrack.client.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText etName,etEmail, etPassword, etSalary;
    private Button btnRegister;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etSalary = findViewById(R.id.etSalary);
        btnRegister = findViewById(R.id.btnRegister);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        btnRegister.setOnClickListener(v -> {
            Log.d(TAG, "Register button clicked");
            registerUser();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String salaryStr = etSalary.getText().toString().trim();

        Log.d(TAG, "User input - Email: " + email + ", Password: [HIDDEN], Salary: " + salaryStr);

        if (validateFields(name, email, password, salaryStr)) return;
        double salary = Double.parseDouble(salaryStr);

        RegisterRequest request = new RegisterRequest();
        request.name = name;
        request.email = email;
        request.password = password;
        request.monthlySalary = salary;

        Log.d(TAG, "Sending registration request: " + request);

        apiService.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Registration successful: " + response.body());
                    Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                    Log.d(TAG, " Navigating to DashboardActivity with email: " + email);
                    intent.putExtra("USER_EMAIL", email);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e(TAG, "Registration failed: " + response.errorBody());
                    Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage(), t);
                Toast.makeText(RegisterActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateFields(String name, String email, String password, String salaryStr) {
        if (name.isEmpty()){
            etName.setError("Enter your name");
            Log.e(TAG, "Name field is empty");
            return true;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email");
            Log.e(TAG, "Invalid email: " + email);
            return true;
        }
        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            Log.e(TAG, "Invalid password length");
            return true;
        }
        if (salaryStr.isEmpty()) {
            etSalary.setError("Enter your salary");
            Log.e(TAG, "Salary field is empty");
            return true;
        }
        return false;
    }
}
package com.tvxargtec.online.mine.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tvxargtec.online.R;
import com.tvxargtec.online.activity.MainAty;
import com.tvxargtec.online.base.BaseActivity;

/**
 * Activity para el registro de nuevos usuarios
 */
public class AccountAty extends BaseActivity {
    
    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister, btnBackToLogin;
    private TextView tvTerms;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "user_data";

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_accountaty;
    }

    @Override
    protected void initView() {
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        tvTerms = findViewById(R.id.tvTerms);

        btnRegister.setOnClickListener(v -> attemptRegister());
        btnBackToLogin.setOnClickListener(v -> finish());
        tvTerms.setOnClickListener(v -> showTermsAndConditions());
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validación de campos
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showToast("Por favor completa todos los campos");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Por favor ingresa un email válido");
            return;
        }

        if (password.length() < 6) {
            showToast("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showToast("Las contraseñas no coinciden");
            return;
        }

        // Verificar si el usuario ya existe (búsqueda local)
        if (userExists(email)) {
            showToast("Este email ya está registrado");
            return;
        }

        showLoading();
        
        // TODO: Conectar con API para registro real
        // ApiClient.getInstance().createService(AuthService.class)
        //     .register(new LoginRequest(email, password))
        //     .enqueue(new Callback<LoginResponse>() {
        //         @Override
        //         public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
        //             hideLoading();
        //             if (response.isSuccessful() && response.body() != null) {
        //                 saveUserSession(response.body());
        //                 navigateToMain();
        //             } else {
        //                 showToast("Error en el registro");
        //             }
        //         }
        //         
        //         @Override
        //         public void onFailure(Call<LoginResponse> call, Throwable t) {
        //             hideLoading();
        //             showToast("Error de conexión: " + t.getMessage());
        //         }
        //     });
        
        // Por ahora simula registro exitoso
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.postDelayed(() -> {
            hideLoading();
            saveUserSession(name, email);
            showToast("Registro exitoso");
            navigateToMain();
        }, 1500);
    }

    private void saveUserSession(String name, String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", name);
        editor.putString("user_email", email);
        editor.putString("user_id", "user_" + System.currentTimeMillis());
        editor.putString("plan_status", "Free");
        editor.putString("plan_expiry", "31/12/2026");
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }

    private void navigateToMain() {
        Intent intent = new Intent(AccountAty.this, MainAty.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean userExists(String email) {
        // TODO: Verificar en la base de datos local o backend
        return false;
    }

    private void showTermsAndConditions() {
        // TODO: Mostrar diálogo con términos y condiciones
        showToast("Términos y Condiciones (próximamente)");
    }
}

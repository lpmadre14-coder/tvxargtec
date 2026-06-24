package com.tvxargtec.online.mine.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tvxargtec.online.R;
import com.tvxargtec.online.activity.MainAty;
import com.tvxargtec.online.base.BaseActivity;
import com.tvxargtec.online.utils.AuthManager;

public class AccountAty extends BaseActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister, btnBackToLogin;
    private TextView tvTerms;
    private AuthManager authManager;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_accountaty;
    }

    @Override
    protected void initView() {
        authManager = AuthManager.getInstance(this);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        tvTerms = findViewById(R.id.tvTerms);

        btnRegister.setOnClickListener(v -> attemptRegister());
        btnBackToLogin.setOnClickListener(v -> finish());
        tvTerms.setOnClickListener(v -> showToast("Términos y condiciones aceptados al registrarte"));
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

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

        showLoading();

        authManager.register(email, password, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(com.tvxargtec.online.models.UserProfile user) {
                hideLoading();
                showToast("Registro exitoso. Ahora inicia sesión.");
                finish();
            }

            @Override
            public void onError(String error) {
                hideLoading();
                showToast("Error: " + error);
            }
        });
    }

    private void showTermsAndConditions() {
        showToast("Términos y Condiciones (próximamente)");
    }
}

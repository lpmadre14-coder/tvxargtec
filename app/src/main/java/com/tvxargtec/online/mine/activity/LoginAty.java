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

public class LoginAty extends BaseActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgot;
    private AuthManager authManager;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgot = findViewById(R.id.tvForgot);

        authManager = AuthManager.getInstance(this);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, AccountAty.class)));
        tvForgot.setOnClickListener(v -> startActivity(new Intent(this, ForgetPasswordAty.class)));
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Ingresa email y contraseña");
            return;
        }

        showLoading();

        authManager.login(email, password, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(com.tvxargtec.online.models.UserProfile user) {
                hideLoading();
                showToast("Login exitoso");
                navigateToMain();
            }

            @Override
            public void onError(String error) {
                hideLoading();
                showToast("Error: " + error);
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginAty.this, MainAty.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

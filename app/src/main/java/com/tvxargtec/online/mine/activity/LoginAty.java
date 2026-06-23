package com.tvxargtec.online.mine.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tvxargtec.online.R;
import com.tvxargtec.online.base.BaseActivity;
import com.tvxargtec.online.activity.MainAty;

public class LoginAty extends BaseActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgot;

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
        
        // TODO: Conectar con API de login
        // ApiClient.getInstance().login(email, password, new Callback<LoginResponse>() {
        //     @Override
        //     public void onSuccess(LoginResponse response) {
        //         saveUserSession(response);
        //         hideLoading();
        //         navigateToMain();
        //     }
        //     
        //     @Override
        //     public void onError(String error) {
        //         hideLoading();
        //         showToast("Error: " + error);
        //     }
        // });
        
        // Por ahora simula login exitoso y guarda datos localmente
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.postDelayed(() -> {
            hideLoading();
            saveUserSession(email, "Usuario Demo");
            showToast("Login exitoso");
            navigateToMain();
        }, 1000);
    }
    
    private void saveUserSession(String email, String name) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_email", email);
        editor.putString("user_name", name);
        editor.putString("user_id", "demo_user_" + System.currentTimeMillis());
        editor.putString("plan_status", "Free");
        editor.putString("plan_expiry", "31/12/2026");
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }
    
    private void navigateToMain() {
        Intent intent = new Intent(LoginAty.this, MainAty.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

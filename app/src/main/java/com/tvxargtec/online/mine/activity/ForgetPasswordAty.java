package com.tvxargtec.online.mine.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tvxargtec.online.R;
import com.tvxargtec.online.base.BaseActivity;

/**
 * Activity para recuperar la contraseña olvidada
 */
public class ForgetPasswordAty extends BaseActivity {
    
    private EditText etEmail;
    private Button btnSendReset, btnBackToLogin;
    private TextView tvMessage;
    private int currentStep = 1; // Paso 1: Email, Paso 2: Código, Paso 3: Nueva contraseña

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_forget_password;
    }

    @Override
    protected void initView() {
        etEmail = findViewById(R.id.etEmail);
        btnSendReset = findViewById(R.id.btnSendReset);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        tvMessage = findViewById(R.id.tvMessage);

        btnSendReset.setOnClickListener(v -> handleResetFlow());
        btnBackToLogin.setOnClickListener(v -> finish());
    }

    private void handleResetFlow() {
        if (currentStep == 1) {
            sendPasswordResetEmail();
        } else if (currentStep == 2) {
            verifyResetCode();
        } else if (currentStep == 3) {
            updateNewPassword();
        }
    }

    private void sendPasswordResetEmail() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            showToast("Por favor ingresa tu email");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Por favor ingresa un email válido");
            return;
        }

        showLoading();

        // TODO: Conectar con API para enviar email de recuperación
        // ApiClient.getInstance().createService(AuthService.class)
        //     .sendPasswordReset(email)
        //     .enqueue(new Callback<Void>() {
        //         @Override
        //         public void onResponse(Call<Void> call, Response<Void> response) {
        //             hideLoading();
        //             if (response.isSuccessful()) {
        //                 showToast("Email de recuperación enviado");
        //                 moveToStep2();
        //             } else {
        //                 showToast("No se pudo enviar el email");
        //             }
        //         }
        //         
        //         @Override
        //         public void onFailure(Call<Void> call, Throwable t) {
        //             hideLoading();
        //             showToast("Error de conexión");
        //         }
        //     });

        // Simular envío de email
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.postDelayed(() -> {
            hideLoading();
            showToast("Email de recuperación enviado a: " + email);
            moveToStep2();
        }, 1500);
    }

    private void verifyResetCode() {
        String code = etEmail.getText().toString().trim();

        if (code.isEmpty()) {
            showToast("Por favor ingresa el código de verificación");
            return;
        }

        showLoading();

        // TODO: Conectar con API para verificar código
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.postDelayed(() -> {
            hideLoading();
            if (code.length() >= 4) {
                showToast("Código verificado");
                moveToStep3();
            } else {
                showToast("Código inválido");
            }
        }, 1000);
    }

    private void updateNewPassword() {
        String newPassword = etEmail.getText().toString().trim();

        if (newPassword.isEmpty()) {
            showToast("Por favor ingresa la nueva contraseña");
            return;
        }

        if (newPassword.length() < 6) {
            showToast("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        showLoading();

        // TODO: Conectar con API para actualizar contraseña
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.postDelayed(() -> {
            hideLoading();
            showToast("Contraseña actualizada exitosamente");
            finish();
        }, 1500);
    }

    private void moveToStep2() {
        currentStep = 2;
        etEmail.setText("");
        etEmail.setHint("Ingresa el código de verificación");
        btnSendReset.setText("Verificar Código");
        tvMessage.setText("Se envió un código a tu email. Ingrésalo aquí.");
    }

    private void moveToStep3() {
        currentStep = 3;
        etEmail.setText("");
        etEmail.setHint("Ingresa tu nueva contraseña");
        etEmail.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        btnSendReset.setText("Actualizar Contraseña");
        tvMessage.setText("Ingresa tu nueva contraseña");
    }
}

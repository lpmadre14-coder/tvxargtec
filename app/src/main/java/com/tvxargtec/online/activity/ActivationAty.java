package com.tvxargtec.online.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.tvxargtec.online.R;
import com.tvxargtec.online.base.BaseActivity;
import com.tvxargtec.online.utils.AuthManager;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ActivationAty extends BaseActivity {

    private EditText et1, et2, et3, et4, et5, et6;
    private TextView tvStatus;
    private AuthManager authManager;
    private OkHttpClient httpClient;
    private boolean isValidating = false;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_activation;
    }

    @Override
    protected void initView() {
        et1 = findViewById(R.id.etCode1);
        et2 = findViewById(R.id.etCode2);
        et3 = findViewById(R.id.etCode3);
        et4 = findViewById(R.id.etCode4);
        et5 = findViewById(R.id.etCode5);
        et6 = findViewById(R.id.etCode6);
        tvStatus = findViewById(R.id.tvStatus);

        tvStatus.setText("Ingresá el código de 6 dígitos de la web");
    }

    @Override
    protected void initData() {
        authManager = AuthManager.getInstance(this);
        httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build();
        setupInputs();
    }

    private void setupInputs() {
        final EditText[] inputs = {et1, et2, et3, et4, et5, et6};
        for (int i = 0; i < inputs.length; i++) {
            final int index = i;
            inputs[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && index < inputs.length - 1) {
                        inputs[index + 1].requestFocus();
                    }
                    if (s.length() == 0 && index > 0) {
                        inputs[index - 1].requestFocus();
                    }
                    if (getEnteredCode().length() == 6) {
                        validateCode();
                    }
                }
            });
        }
    }

    private String getEnteredCode() {
        return et1.getText().toString() + et2.getText().toString() +
                et3.getText().toString() + et4.getText().toString() +
                et5.getText().toString() + et6.getText().toString();
    }

    private void validateCode() {
        if (isValidating) return;
        isValidating = true;
        tvStatus.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
        tvStatus.setText("⏳ Verificando código...");

        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("code", getEnteredCode());

                Request request = new Request.Builder()
                    .url("https://apitvxargtec.duckdns.org/api/activation/validate")
                    .post(RequestBody.create(MediaType.parse("application/json"), body.toString()))
                    .build();

                Response response = httpClient.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : "{}";
                JSONObject json = new JSONObject(responseBody);

                if (json.optInt("code") == 200 && json.optJSONObject("data") != null) {
                    runOnUiThread(() -> {
                        tvStatus.setTextColor(getResources().getColor(R.color.brand_green, getTheme()));
                        tvStatus.setText("✅ Código válido, activando plan...");
                    });
                    activateFreePlan();
                } else {
                    runOnUiThread(() -> {
                        tvStatus.setTextColor(getResources().getColor(R.color.semantic_error, getTheme()));
                        tvStatus.setText("❌ " + json.optString("message", "Código inválido o expirado"));
                        clearInputs();
                        isValidating = false;
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvStatus.setTextColor(getResources().getColor(R.color.semantic_error, getTheme()));
                    tvStatus.setText("❌ Error de conexión");
                    clearInputs();
                    isValidating = false;
                });
            }
        }).start();
    }

    private void activateFreePlan() {
        String token = authManager.getToken();
        if (token == null) {
            runOnUiThread(() -> {
                tvStatus.setTextColor(getResources().getColor(R.color.semantic_warning, getTheme()));
                tvStatus.setText("⚠️ Inicia sesión antes de activar");
                clearInputs();
                isValidating = false;
            });
            return;
        }

        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                    .url("https://apitvxargtec.duckdns.org/api/user/activate-free")
                    .post(RequestBody.create(MediaType.parse("application/json"), "{}"))
                    .header("Authorization", "Bearer " + token)
                    .build();

                Response response = httpClient.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : "{}";
                JSONObject json = new JSONObject(responseBody);

                if (json.optInt("code") == 200 && json.optJSONObject("data") != null) {
                    JSONObject data = json.getJSONObject("data");
                    String planType = data.optString("planType", "free");
                    String planExpiry = data.optString("planExpiry", "");
                    authManager.updatePlanLocally(planType, planExpiry);

                    runOnUiThread(() -> {
                        tvStatus.setTextColor(getResources().getColor(R.color.brand_green, getTheme()));
                        tvStatus.setText("✅ ¡Plan Free activado con éxito!");
                    });
                } else {
                    runOnUiThread(() -> {
                        tvStatus.setTextColor(getResources().getColor(R.color.semantic_error, getTheme()));
                        tvStatus.setText("❌ " + json.optString("message", "Error al activar"));
                        clearInputs();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvStatus.setTextColor(getResources().getColor(R.color.semantic_error, getTheme()));
                    tvStatus.setText("❌ Error al activar plan");
                    clearInputs();
                });
            }
            isValidating = false;
        }).start();
    }

    private void clearInputs() {
        final EditText[] inputs = {et1, et2, et3, et4, et5, et6};
        for (EditText et : inputs) et.getText().clear();
        et1.requestFocus();
    }
}

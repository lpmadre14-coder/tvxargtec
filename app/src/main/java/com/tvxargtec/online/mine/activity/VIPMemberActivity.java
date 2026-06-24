package com.tvxargtec.online.mine.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.tvxargtec.online.R;
import com.tvxargtec.online.activity.RedemptionAty;
import com.tvxargtec.online.api.VipService;
import com.tvxargtec.online.base.BaseActivity;
import com.tvxargtec.online.utils.ApiClient;
import com.tvxargtec.online.utils.ApiResponse;
import com.tvxargtec.online.utils.AuthManager;
import com.tvxargtec.online.utils.PaymentManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class VIPMemberActivity extends BaseActivity {

    private PaymentManager paymentManager;
    private LinearLayout loadingContainer;
    private View contentContainer;
    private ProgressBar progressBar;
    private TextView tvError;

    private List<Map<String, Object>> plans = new ArrayList<>();
    private AuthManager authManager;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_vip_member;
    }

    @Override
    protected void initView() {
        paymentManager = new PaymentManager(this);
        authManager = AuthManager.getInstance(this);

        loadingContainer = findViewById(R.id.loadingContainer);
        contentContainer = findViewById(R.id.contentContainer);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);

        if (loadingContainer != null) loadingContainer.setVisibility(View.VISIBLE);
        if (contentContainer != null) contentContainer.setVisibility(View.GONE);

        TextView tvRedeem = findViewById(R.id.tvRedeem);
        if (tvRedeem != null) {
            tvRedeem.setOnClickListener(v -> {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("¿Qué deseas hacer?");
                builder.setItems(new CharSequence[]{"🎫 Activar código gratuito", "🔑 Ingresar código de canje"}, (dialog, which) -> {
                    if (which == 0) {
                        startActivity(new Intent(this, com.tvxargtec.online.activity.ActivationAty.class));
                    } else {
                        startActivity(new Intent(this, RedemptionAty.class));
                    }
                });
                builder.show();
            });
        }
    }

    @Override
    protected void initData() {
        fetchPlans();
    }

    private void fetchPlans() {
        String token = authManager.getToken();
        if (token == null) {
            showError("Inicia sesión para ver los planes");
            return;
        }

        VipService service = ApiClient.getInstance().createService(VipService.class);
        service.getPlans("Bearer " + token).enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Map<String, Object>>>> call, retrofit2.Response<ApiResponse<List<Map<String, Object>>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    plans = response.body().getData();
                    runOnUiThread(() -> {
                        hidePlanLoading();
                        populatePlans();
                    });
                } else {
                    showError("No se pudieron cargar los planes");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Map<String, Object>>>> call, Throwable t) {
                showError("Error de conexión: " + t.getLocalizedMessage());
            }
        });
    }

    private void populatePlans() {
        if (plans.isEmpty()) {
            showError("No hay planes disponibles");
            return;
        }

        Button[] buttons = {
            findViewById(R.id.btnMonthly),
            findViewById(R.id.btnYearly),
            findViewById(R.id.btnLifetime)
        };

        TextView[] tvPrices = {
            findViewById(R.id.tvPriceMonthly),
            findViewById(R.id.tvPriceYearly),
            findViewById(R.id.tvPriceLifetime)
        };

        TextView[] tvDurations = {
            findViewById(R.id.tvDurationMonthly),
            findViewById(R.id.tvDurationYearly),
            findViewById(R.id.tvDurationLifetime)
        };

        MaterialCardView[] cards = {
            findViewById(R.id.cardMonthly),
            findViewById(R.id.cardYearly),
            findViewById(R.id.cardLifetime)
        };

        LinearLayout[] featuresContainers = {
            findViewById(R.id.featuresMonthly),
            findViewById(R.id.featuresYearly),
            findViewById(R.id.featuresLifetime)
        };

        String[] planKeys = {"plan_monthly", "plan_yearly", "plan_lifetime"};

        for (int i = 0; i < planKeys.length && i < buttons.length; i++) {
            final int index = i;
            Map<String, Object> plan = findPlanById(planKeys[i]);
            if (plan == null) continue;

            String name = (String) plan.get("name");
            Object priceObj = plan.get("price");
            String duration = (String) plan.get("duration");
            Object featuresObj = plan.get("features");

            // Formatear precio
            String priceText = "$0";
            if (priceObj instanceof Number) {
                priceText = "$" + String.format("%.2f", ((Number) priceObj).doubleValue());
            } else if (priceObj instanceof String) {
                priceText = "$" + priceObj;
            }

            if (tvPrices[i] != null) tvPrices[i].setText(priceText);
            if (tvDurations[i] != null) tvDurations[i].setText("/ " + (duration != null ? duration : ""));

            // Features
            if (featuresContainers[i] != null && featuresObj instanceof List) {
                featuresContainers[i].removeAllViews();
                List<String> features = (List<String>) featuresObj;
                for (String feature : features) {
                    TextView tv = new TextView(this);
                    tv.setText("✓ " + feature);
                    tv.setTextColor(getResources().getColor(R.color.text_secondary));
                    tv.setTextSize(13);
                    tv.setPadding(0, 4, 0, 4);
                    featuresContainers[i].addView(tv);
                }
            }

            // Click listener
            if (buttons[i] != null) {
                buttons[i].setOnClickListener(v -> checkout(planKeys[index], name, priceObj));
            }

            if (cards[i] != null) {
                cards[i].setOnClickListener(v -> checkout(planKeys[index], name, priceObj));
            }
        }
    }

    private Map<String, Object> findPlanById(String id) {
        for (Map<String, Object> plan : plans) {
            if (id.equals(plan.get("id"))) return plan;
        }
        return null;
    }

    private void checkout(String planId, String planName, Object priceObj) {
        long priceCents = 0;
        if (priceObj instanceof Number) {
            priceCents = (long) (((Number) priceObj).doubleValue() * 100);
        }
        paymentManager.startCheckout(planId, planName, priceCents, new PaymentManager.PaymentCallback() {
            @Override
            public void onSuccess(String id) {
                upgradePlan(planId, planName);
            }

            @Override
            public void onError(String message) {
                showToast("Error: " + message);
            }

            @Override
            public void onCancel() {
                showToast("Pago cancelado");
            }
        });
    }

    private void upgradePlan(String planId, String planName) {
        String token = authManager.getToken();
        if (token == null) {
            showToast("Debes iniciar sesión");
            return;
        }

        VipService service = ApiClient.getInstance().createService(VipService.class);
        Map<String, String> body = new HashMap<>();
        body.put("planId", planId);

        showPlanLoading();
        service.upgrade("Bearer " + token, body).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, retrofit2.Response<ApiResponse<Map<String, Object>>> response) {
                hidePlanLoading();
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    Map<String, Object> data = response.body().getData();
                    if (data != null) {
                        String expiry = data.get("planExpiry") != null ? data.get("planExpiry").toString() : "";
                        authManager.updatePlanLocally(planId, expiry);
                    }
                    showToast("¡Suscripción " + planName + " activada!");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showToast("Error al activar el plan");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                hidePlanLoading();
                showToast("Error de conexión");
            }
        });
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            if (loadingContainer != null) loadingContainer.setVisibility(View.GONE);
            if (tvError != null) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText(message);
            }
        });
    }

    private void showPlanLoading() {
        if (loadingContainer != null) loadingContainer.setVisibility(View.VISIBLE);
        if (contentContainer != null) contentContainer.setVisibility(View.GONE);
        if (tvError != null) tvError.setVisibility(View.GONE);
    }

    private void hidePlanLoading() {
        if (loadingContainer != null) loadingContainer.setVisibility(View.GONE);
        if (contentContainer != null) contentContainer.setVisibility(View.VISIBLE);
        if (tvError != null) tvError.setVisibility(View.GONE);
    }
}

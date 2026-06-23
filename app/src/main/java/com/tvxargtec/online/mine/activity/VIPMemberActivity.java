package com.tvxargtec.online.mine.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tvxargtec.online.R;
import com.tvxargtec.online.activity.RedemptionAty;
import com.tvxargtec.online.base.BaseActivity;
import com.tvxargtec.online.utils.PaymentManager;

public class VIPMemberActivity extends BaseActivity {

    private PaymentManager paymentManager;

    // Precios en centavos (USD)
    private static final long PRICE_MONTHLY = 499;   // $4.99
    private static final long PRICE_YEARLY = 2999;   // $29.99
    private static final long PRICE_LIFETIME = 4999; // $49.99

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_vip_member;
    }

    @Override
    protected void initView() {
        paymentManager = new PaymentManager(this);

        Button btnMonthly = findViewById(R.id.btnMonthly);
        Button btnYearly = findViewById(R.id.btnYearly);
        Button btnLifetime = findViewById(R.id.btnLifetime);
        TextView tvRedeem = findViewById(R.id.tvRedeem);

        btnMonthly.setOnClickListener(v -> checkout("plan_monthly", "Mensual", PRICE_MONTHLY));
        btnYearly.setOnClickListener(v -> checkout("plan_yearly", "Anual", PRICE_YEARLY));
        btnLifetime.setOnClickListener(v -> checkout("plan_lifetime", "Vitalicio", PRICE_LIFETIME));
        tvRedeem.setOnClickListener(v ->
                startActivity(new Intent(this, RedemptionAty.class)));
    }

    private void checkout(String planId, String planName, long priceCents) {
        paymentManager.startCheckout(planId, planName, priceCents, new PaymentManager.PaymentCallback() {
            @Override
            public void onSuccess(String id) {
                showToast("¡Suscripción " + planName + " activada!");
                // TODO: Actualizar estado VIP en backend + local
                finish();
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

    @Override
    protected void initData() {
        // Nothing to load
    }
}

package com.tvxargtec.online.mine.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;

import com.tvxargtec.online.R;
import com.tvxargtec.online.base.BaseActivity;

public class MyBenefitsAty extends BaseActivity {

    private TextView tvTotalPoints, tvVipStatus, tvVipExpiry, tvEmptyHistory;
    private Button btnRedeem;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_my_benefits;
    }

    @Override
    protected void initView() {
        try {
            tvTotalPoints = findViewById(R.id.tvTotalPoints);
            tvVipStatus = findViewById(R.id.tvVipStatus);
            tvVipExpiry = findViewById(R.id.tvVipExpiry);
            tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
            btnRedeem = findViewById(R.id.btnRedeem);

            if (btnRedeem != null) {
                btnRedeem.setOnClickListener(v -> {
                    showToast("Función de canje disponible pronto");
                });
            }
        } catch (Exception e) {
            showToast("Error al cargar beneficios");
            e.printStackTrace();
        }
    }

    @Override
    protected void initData() {
        loadUserBenefits();
    }

    private void loadUserBenefits() {
        try {
            // Cargar datos del usuario desde SharedPreferences
            int totalPoints = getSharedPreferences("user_data", MODE_PRIVATE)
                .getInt("total_points", 0);
            String vipStatus = getSharedPreferences("user_data", MODE_PRIVATE)
                .getString("vip_status", "Miembro Gratis");
            String vipExpiry = getSharedPreferences("user_data", MODE_PRIVATE)
                .getString("vip_expiry", "Actualiza a VIP para obtener más beneficios");

            // Actualizar vistas
            if (tvTotalPoints != null) {
                tvTotalPoints.setText(String.valueOf(totalPoints));
            }

            if (tvVipStatus != null) {
                tvVipStatus.setText(vipStatus);
            }

            if (tvVipExpiry != null) {
                tvVipExpiry.setText(vipExpiry);
            }

            // Mostrar/ocultar historial vacío
            if (tvEmptyHistory != null) {
                tvEmptyHistory.setText(totalPoints == 0 ? 
                    "No hay historial de puntos disponible" : 
                    "Historial de puntos cargado");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

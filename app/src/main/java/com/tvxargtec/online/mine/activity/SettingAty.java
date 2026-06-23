package com.tvxargtec.online.mine.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.tvxargtec.online.R;
import com.tvxargtec.online.mine.activity.AboutAty;
import com.tvxargtec.online.activity.SettingLanguageAty;
import com.tvxargtec.online.base.BaseActivity;

public class SettingAty extends BaseActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initView() {
        try {
            TextView tvLanguage = findViewById(R.id.tvLanguage);
            TextView tvNotifications = findViewById(R.id.tvNotifications);
            CardView llAbout = findViewById(R.id.llAbout);
            CardView llLogout = findViewById(R.id.llLogout);

            if (tvLanguage != null) {
                tvLanguage.setOnClickListener(v ->
                        startActivity(new Intent(this, SettingLanguageAty.class)));
            }

            if (tvNotifications != null) {
                tvNotifications.setOnClickListener(v ->
                        startActivity(new Intent(this, NotificationSettingAty.class)));
            }

            if (llAbout != null) {
                llAbout.setOnClickListener(v ->
                        startActivity(new Intent(this, AboutAty.class)));
            }

            if (llLogout != null) {
                llLogout.setOnClickListener(v -> {
                    showToast("Sesión cerrada");
                    finish();
                });
            }
        } catch (Exception e) {
            showToast("Error al cargar configuración: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void initData() {
        // Aquí puedes cargar datos de configuración del usuario si es necesario
    }
}

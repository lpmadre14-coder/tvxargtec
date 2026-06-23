package com.tvxargtec.online.mine.activity;

import android.os.Bundle;
import android.widget.Switch;
import android.widget.LinearLayout;
import com.tvxargtec.online.R;
import com.tvxargtec.online.base.BaseActivity;

public class NotificationSettingAty extends BaseActivity {
    
    private Switch switchAlerts, switchContent, switchPromos, switchSystem;
    
    @Override
    protected int getLayoutResId() {
        return R.layout.activity_notification_setting;
    }

    @Override
    protected void initView() {
        try {
            // Intentar encontrar switches si existen en el layout
            switchAlerts = findViewById(R.id.switchAlerts);
            switchContent = findViewById(R.id.switchContent);
            switchPromos = findViewById(R.id.switchPromos);
            switchSystem = findViewById(R.id.switchSystem);
            
            // Configurar listeners si existen
            if (switchAlerts != null) {
                switchAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> 
                    saveNotificationPreference("alerts", isChecked));
            }
            if (switchContent != null) {
                switchContent.setOnCheckedChangeListener((buttonView, isChecked) -> 
                    saveNotificationPreference("content", isChecked));
            }
            if (switchPromos != null) {
                switchPromos.setOnCheckedChangeListener((buttonView, isChecked) -> 
                    saveNotificationPreference("promos", isChecked));
            }
            if (switchSystem != null) {
                switchSystem.setOnCheckedChangeListener((buttonView, isChecked) -> 
                    saveNotificationPreference("system", isChecked));
            }
        } catch (Exception e) {
            showToast("Error al cargar configuración de notificaciones");
            e.printStackTrace();
        }
    }

    @Override
    protected void initData() {
        // Cargar preferencias guardadas
        loadNotificationPreferences();
    }
    
    private void saveNotificationPreference(String key, boolean value) {
        getSharedPreferences("notifications", MODE_PRIVATE)
            .edit()
            .putBoolean(key, value)
            .apply();
    }
    
    private void loadNotificationPreferences() {
        // Cargar y restaurar las preferencias guardadas
        boolean alertsEnabled = getSharedPreferences("notifications", MODE_PRIVATE)
            .getBoolean("alerts", true);
        boolean contentEnabled = getSharedPreferences("notifications", MODE_PRIVATE)
            .getBoolean("content", true);
        boolean promosEnabled = getSharedPreferences("notifications", MODE_PRIVATE)
            .getBoolean("promos", true);
        boolean systemEnabled = getSharedPreferences("notifications", MODE_PRIVATE)
            .getBoolean("system", true);
            
        if (switchAlerts != null) switchAlerts.setChecked(alertsEnabled);
        if (switchContent != null) switchContent.setChecked(contentEnabled);
        if (switchPromos != null) switchPromos.setChecked(promosEnabled);
        if (switchSystem != null) switchSystem.setChecked(systemEnabled);
    }
}

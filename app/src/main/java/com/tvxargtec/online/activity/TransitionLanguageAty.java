package com.tvxargtec.online.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.tvxargtec.online.R;
import com.tvxargtec.online.base.BaseActivity;
import com.tvxargtec.online.utils.LocaleHelper;

public class TransitionLanguageAty extends BaseActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_transition_language;
    }

    @Override
    protected void initView() {
        TextView tvMessage = findViewById(R.id.tvMessage);
        tvMessage.setText("Cambiando idioma...");
    }

    @Override
    protected void initData() {
        String lang = getIntent().getStringExtra("lang");
        if (lang != null) {
            LocaleHelper.INSTANCE.setLocale(this, lang);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(this, MainAty.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 500);
    }
}

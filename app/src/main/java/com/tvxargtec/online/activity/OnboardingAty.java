package com.tvxargtec.online.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.tvxargtec.online.R;
import com.tvxargtec.online.adapter.OnboardingAdapter;
import com.tvxargtec.online.base.BaseActivity;

public class OnboardingAty extends BaseActivity {

    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;
    private TextView tvSkip;
    private MaterialButton btnNext;
    private OnboardingAdapter adapter;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_onboarding;
    }

    @Override
    protected void initView() {
        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        tvSkip = findViewById(R.id.tvSkip);
        btnNext = findViewById(R.id.btnNext);

        adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);

        setupDots();
        setupListeners();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
                updateButton(position);
            }
        });
    }

    @Override
    protected void initData() {
        SharedPreferences prefs = getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE);
        if (prefs.getBoolean("onboarding_completed", false)) {
            startActivity(new Intent(this, PinAty.class));
            finish();
        }
    }

    private void goToNext() {
        SharedPreferences prefs = getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("onboarding_completed", true).apply();
        startActivity(new Intent(this, PinAty.class));
        finish();
    }

    private void setupDots() {
        for (int i = 0; i < 3; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(12, 12);
            params.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.bg_dot_inactive);
            dotsLayout.addView(dot);
        }
        updateDots(0);
    }

    private void updateDots(int position) {
        for (int i = 0; i < dotsLayout.getChildCount(); i++) {
            dotsLayout.getChildAt(i).setBackgroundResource(
                    i == position ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive
            );
        }
    }

    private void setupListeners() {
        tvSkip.setOnClickListener(v -> goToNext());

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < 2) {
                viewPager.setCurrentItem(current + 1);
            } else {
                goToNext();
            }
        });
    }

    private void updateButton(int position) {
        if (position == 2) {
            btnNext.setText(R.string.onboarding_start);
        } else {
            btnNext.setText(R.string.onboarding_next);
        }
    }
}

package com.tvxargtec.online.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.tvxargtec.online.R;
import com.tvxargtec.online.base.BaseActivity;

import java.util.Random;

public class ActivationAty extends BaseActivity {

    private EditText et1, et2, et3, et4, et5, et6;
    private TextView tvCode, tvTimer, tvStatus;
    private CountDownTimer countDownTimer;
    private String currentCode = "";
    private long timeLeft = 20000;

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
        tvCode = findViewById(R.id.tvDisplayCode);
        tvTimer = findViewById(R.id.tvTimer);
        tvStatus = findViewById(R.id.tvStatus);
    }

    @Override
    protected void initData() {
        setupInputs();
        generateCode();
        startTimer();
    }

    private void generateCode() {
        currentCode = String.format("%06d", new Random().nextInt(1000000));
        tvCode.setText(currentCode);
    }

    private void startTimer() {
        timeLeft = 20000;
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                int seconds = (int) (millisUntilFinished / 1000);
                tvTimer.setText("⏱ " + seconds + "s");
                if (seconds <= 5) {
                    tvTimer.setTextColor(getResources().getColor(R.color.semantic_error, getTheme()));
                } else {
                    tvTimer.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
                }
            }

            @Override
            public void onFinish() {
                generateCode();
                startTimer();
                clearInputs();
                tvStatus.setText("🔄 Nuevo código generado");
            }
        }.start();
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
                    checkCode();
                }
            });
        }
    }

    private void checkCode() {
        String entered = et1.getText().toString() + et2.getText().toString() +
                et3.getText().toString() + et4.getText().toString() +
                et5.getText().toString() + et6.getText().toString();

        if (entered.length() < 6) return;

        if (entered.equals(currentCode)) {
            tvStatus.setTextColor(getResources().getColor(R.color.brand_green, getTheme()));
            tvStatus.setText("✅ ¡Código válido! Plan Free activado");
            if (countDownTimer != null) countDownTimer.cancel();
            // TODO: Llamar a backend para activar plan free
        } else {
            tvStatus.setTextColor(getResources().getColor(R.color.semantic_error, getTheme()));
            tvStatus.setText("❌ Código incorrecto");
            clearInputs();
        }
    }

    private void clearInputs() {
        final EditText[] inputs = {et1, et2, et3, et4, et5, et6};
        for (EditText et : inputs) et.getText().clear();
        et1.requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}

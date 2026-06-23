package com.tvxargtec.online.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

public class PaymentManager {

    private static final String TAG = "PaymentManager";
    private static final String PUBLISHABLE_KEY = "pk_test_XXXXXXXXXXXXXXXXXXXXXXXX"; // ← REEMPLAZAR

    private final PaymentSheet paymentSheet;
    private final AppCompatActivity activity;
    private PaymentCallback callback;

    public interface PaymentCallback {
        void onSuccess(String planId);
        void onError(String message);
        void onCancel();
    }

    public PaymentManager(AppCompatActivity activity) {
        this.activity = activity;
        PaymentConfiguration.init(activity, PUBLISHABLE_KEY);
        this.paymentSheet = new PaymentSheet(activity, this::onPaymentResult);
    }

    public void startCheckout(String planId, String planName, long priceCents, PaymentCallback callback) {
        this.callback = callback;

        String currency = "usd";

        // En producción: llamar a tu backend para crear PaymentIntent
        // Por ahora simulamos un client_secret
        String clientSecret = mockCreatePaymentIntent(priceCents, currency);

        if (clientSecret == null) {
            // Modo simulación sin backend
            simulatePayment(planId);
            return;
        }

        PaymentSheet.Configuration config = new PaymentSheet.Configuration.Builder("Tvxargtec").build();

        paymentSheet.presentWithPaymentIntent(clientSecret, config);
    }

    private String mockCreatePaymentIntent(long amountCents, String currency) {
        // TODO: Llamar a ApiService.createPaymentIntent(amount, currency)
        // Ejemplo:
        // String json = ApiService.post("/create-payment-intent", body);
        // return new JSONObject(json).getString("client_secret");
        return null; // null = usar modo simulación
    }

    private void simulatePayment(String planId) {
        Log.d(TAG, "Simulando pago para plan: " + planId);
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (callback != null) {
                callback.onSuccess(planId);
            }
        }, 1500);
    }

    private void onPaymentResult(PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            Log.d(TAG, "Pago exitoso");
            if (callback != null) callback.onSuccess(null);
        } else if (result instanceof PaymentSheetResult.Canceled) {
            Log.d(TAG, "Pago cancelado");
            if (callback != null) callback.onCancel();
        } else if (result instanceof PaymentSheetResult.Failed) {
            String error = ((PaymentSheetResult.Failed) result).getError().getLocalizedMessage();
            Log.e(TAG, "Error de pago: " + error);
            if (callback != null) callback.onError(error);
        }
    }

    // Para usar cuando el backend esté listo:
    // public void setPublishableKey(String key) {
    //     PaymentConfiguration.init(activity, key);
    // }
}

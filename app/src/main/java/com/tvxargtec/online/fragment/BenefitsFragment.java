package com.tvxargtec.online.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tvxargtec.online.R;
import com.tvxargtec.online.utils.AuthManager;

public class BenefitsFragment extends Fragment {

    private TextView tvTotalPoints, tvVipStatus, tvVipExpiry, tvEmptyHistory;
    private Button btnRedeem;
    private AuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_my_benefits, container, false);
        authManager = AuthManager.getInstance(requireActivity());
        initView(view);
        loadUserBenefits();
        return view;
    }

    private void initView(View view) {
        try {
            tvTotalPoints = view.findViewById(R.id.tvTotalPoints);
            tvVipStatus = view.findViewById(R.id.tvVipStatus);
            tvVipExpiry = view.findViewById(R.id.tvVipExpiry);
            tvEmptyHistory = view.findViewById(R.id.tvEmptyHistory);
            btnRedeem = view.findViewById(R.id.btnRedeem);

            if (btnRedeem != null) {
                btnRedeem.setOnClickListener(v -> {
                    if (getActivity() != null) {
                        android.widget.Toast.makeText(getActivity(), "Función de canje disponible pronto", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUserBenefits() {
        if (getActivity() == null) return;
        try {
            String vipStatus = authManager.getPlanType();
            String vipExpiry = authManager.getPlanExpiry();
            String username = authManager.getUserName();
            String email = authManager.getEmail();

            boolean isLoggedIn = authManager.isLoggedIn();

            if (tvTotalPoints != null) {
                if (isLoggedIn) {
                    tvTotalPoints.setText(username != null ? username : "Usuario");
                } else {
                    tvTotalPoints.setText("Invitado");
                }
            }

            if (tvVipStatus != null) {
                if (isLoggedIn && vipStatus != null && !vipStatus.equals("free")) {
                    String displayName;
                    switch (vipStatus) {
                        case "plan_monthly": displayName = "VIP Mensual"; break;
                        case "plan_yearly": displayName = "VIP Anual"; break;
                        case "plan_lifetime": displayName = "VIP Vitalicio"; break;
                        default: displayName = "Miembro VIP";
                    }
                    tvVipStatus.setText(displayName);
                } else {
                    tvVipStatus.setText(isLoggedIn ? "Miembro Gratis" : "Invitado");
                }
            }

            if (tvVipExpiry != null) {
                if (isLoggedIn && vipExpiry != null && !vipExpiry.isEmpty()) {
                    tvVipExpiry.setText("Vence: " + vipExpiry);
                } else {
                    tvVipExpiry.setText(isLoggedIn ? "Actualiza a VIP para obtener más beneficios" : "Inicia sesión para acceder");
                }
            }

            if (tvEmptyHistory != null) {
                if (isLoggedIn) {
                    tvEmptyHistory.setText(email != null ? email : "Historial de puntos cargado");
                } else {
                    tvEmptyHistory.setText("No hay historial de puntos disponible");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

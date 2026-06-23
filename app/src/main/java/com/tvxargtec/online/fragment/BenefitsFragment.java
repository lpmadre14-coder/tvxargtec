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

import static android.content.Context.MODE_PRIVATE;

public class BenefitsFragment extends Fragment {

    private TextView tvTotalPoints, tvVipStatus, tvVipExpiry, tvEmptyHistory;
    private Button btnRedeem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_my_benefits, container, false);
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
            int totalPoints = getActivity().getSharedPreferences("user_data", MODE_PRIVATE)
                .getInt("total_points", 0);
            String vipStatus = getActivity().getSharedPreferences("user_data", MODE_PRIVATE)
                .getString("vip_status", "Miembro Gratis");
            String vipExpiry = getActivity().getSharedPreferences("user_data", MODE_PRIVATE)
                .getString("vip_expiry", "Actualiza a VIP para obtener más beneficios");
            String planStatus = getActivity().getSharedPreferences("user_data", MODE_PRIVATE)
                .getString("plan_status", "Free");

            if (tvTotalPoints != null) tvTotalPoints.setText(String.valueOf(totalPoints));
            if (tvVipStatus != null) tvVipStatus.setText(vipStatus);
            if (tvVipExpiry != null) tvVipExpiry.setText(vipExpiry);
            if (tvEmptyHistory != null) {
                tvEmptyHistory.setText(totalPoints == 0 ? 
                    "No hay historial de puntos disponible" : 
                    "Historial de puntos cargado");
            }
            
            // Cargar beneficios del plan desde backend
            // TODO: Implementar llamada a API para obtener beneficios
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

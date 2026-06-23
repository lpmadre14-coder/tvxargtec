package com.tvxargtec.online.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.tvxargtec.online.R;
import com.tvxargtec.online.activity.MainAty;
import com.tvxargtec.online.activity.MyFavListActivity;
import com.tvxargtec.online.activity.RecordsAty;
import com.tvxargtec.online.fragment.DownloadsFragment;
import com.tvxargtec.online.fragment.SettingsFragment;
import com.tvxargtec.online.fragment.BenefitsFragment;
import com.tvxargtec.online.mine.activity.LoginAty;
import com.tvxargtec.online.mine.activity.VIPMemberActivity;
import com.tvxargtec.online.utils.ApiClient;

public class ProfileFragment extends Fragment {

    private MaterialCardView btnMyAccount, btnFavorites, btnHistory, btnDownloads, btnSettings, btnLogout;
    private MaterialCardView cardPlanStatus;
    private TextView tvUserName, tvUserEmail, tvPlanStatus, tvPlanExpiry;
    private ImageView ivAvatar;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "user_data";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        sharedPreferences = getActivity().getSharedPreferences(PREF_NAME, android.content.Context.MODE_PRIVATE);
        
        initViews(view);
        loadUserData();
        setupListeners();
        
        return view;
    }

    private void initViews(View view) {
        btnMyAccount = view.findViewById(R.id.btnMyAccount);
        btnFavorites = view.findViewById(R.id.btnFavorites);
        btnHistory = view.findViewById(R.id.btnHistory);
        btnDownloads = view.findViewById(R.id.btnDownloads);
        btnSettings = view.findViewById(R.id.btnSettings);
        btnLogout = view.findViewById(R.id.btnLogout);
        cardPlanStatus = view.findViewById(R.id.cardPlanStatus);
        
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        ivAvatar = view.findViewById(R.id.ivAvatar);

        TextView tvVersion = view.findViewById(R.id.tvProfileVersion);
        if (tvVersion != null) {
            try {
                String vn = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
                tvVersion.setText("Tvxargtec v" + vn);
            } catch (PackageManager.NameNotFoundException ignored) {}
        }
    }

    private void loadUserData() {
        if (getActivity() == null) return;
        
        try {
            // Cargar datos del SharedPreferences
            String userName = sharedPreferences.getString("user_name", "Usuario");
            String userEmail = sharedPreferences.getString("user_email", "usuario@email.com");
            String avatarUrl = sharedPreferences.getString("user_avatar", "");
            String planStatus = sharedPreferences.getString("plan_status", "Free");
            String planExpiry = sharedPreferences.getString("plan_expiry", "N/A");
            
            // Actualizar UI con datos locales
            if (tvUserName != null) tvUserName.setText(userName);
            if (tvUserEmail != null) tvUserEmail.setText(userEmail);
            
            // Cargar avatar con Glide
            if (!avatarUrl.isEmpty() && ivAvatar != null) {
                Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_account)
                    .error(R.drawable.ic_account)
                    .circleCrop()
                    .into(ivAvatar);
            }
            
            // Actualizar estado del plan
            updatePlanStatus(planStatus, planExpiry);
            
            // Intentar cargar datos del backend si está disponible
            fetchUserDataFromBackend();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al cargar datos", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePlanStatus(String status, String expiry) {
        if (cardPlanStatus != null) {
            TextView planText = cardPlanStatus.findViewById(R.id.tvPlanType);
            TextView expiryText = cardPlanStatus.findViewById(R.id.tvPlanExpiry);
            
            if (planText != null) planText.setText(status);
            if (expiryText != null) expiryText.setText("Vencimiento: " + expiry);
        }
    }

    private void fetchUserDataFromBackend() {
        // TODO: Implementar llamada a API para obtener datos del usuario
        // ApiClient.getInstance().getUserProfile(userId, new Callback<UserProfile>() {
        //     @Override
        //     public void onSuccess(UserProfile profile) {
        //         updateUserUI(profile);
        //         saveUserDataLocally(profile);
        //     }
        //     
        //     @Override
        //     public void onError(String error) {
        //         // Usar datos locales si hay error
        //     }
        // });
    }

    private void setupListeners() {
        btnMyAccount.setOnClickListener(v -> navigateToActivity(LoginAty.class));
        btnFavorites.setOnClickListener(v -> {
            MainAty mainAty = MainAty.getInstance();
            if (mainAty != null) {
                mainAty.switchFragment(new FavoritesFragment(), R.id.nav_profile);
            }
        });
        btnHistory.setOnClickListener(v -> navigateToActivity(RecordsAty.class));
        
        // Navegar a Fragmentos usando MainAty para mantener la barra inferior actualizada
        btnDownloads.setOnClickListener(v -> {
            MainAty mainAty = MainAty.getInstance();
            if (mainAty != null) {
                mainAty.switchFragment(new DownloadsFragment(), R.id.nav_profile);
            }
        });
        btnSettings.setOnClickListener(v -> {
            MainAty mainAty = MainAty.getInstance();
            if (mainAty != null) {
                mainAty.switchFragment(new SettingsFragment(), R.id.nav_profile);
            }
        });
        cardPlanStatus.setOnClickListener(v -> {
            MainAty mainAty = MainAty.getInstance();
            if (mainAty != null) {
                mainAty.switchFragment(new BenefitsFragment(), R.id.nav_profile);
            }
        });

        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        if (getActivity() != null) {
            // Limpiar SharedPreferences
            sharedPreferences.edit().clear().apply();
            
            // Mostrar confirmación
            Toast.makeText(getActivity(), "Sesión cerrada", Toast.LENGTH_SHORT).show();
            
            // Navegar a Login
            Intent intent = new Intent(getActivity(), LoginAty.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void navigateToActivity(Class<?> cls) {
        if (getActivity() != null) {
            startActivity(new Intent(getActivity(), cls));
        }
    }
}

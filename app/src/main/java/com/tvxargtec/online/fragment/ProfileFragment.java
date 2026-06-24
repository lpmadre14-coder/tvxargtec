package com.tvxargtec.online.fragment;

import android.content.Intent;
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
import com.tvxargtec.online.activity.RecordsAty;
import com.tvxargtec.online.mine.activity.LoginAty;
import com.tvxargtec.online.mine.activity.VIPMemberActivity;
import com.tvxargtec.online.utils.AuthManager;

public class ProfileFragment extends Fragment {

    private MaterialCardView btnMyAccount, btnFavorites, btnHistory, btnDownloads, btnSettings, btnLogout;
    private MaterialCardView cardPlanStatus;
    private TextView tvUserName, tvUserEmail, tvPlanStatus, tvPlanExpiry;
    private ImageView ivAvatar;
    private AuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        authManager = AuthManager.getInstance(requireContext());

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
        if (!authManager.isLoggedIn()) {
            if (tvUserName != null) tvUserName.setText("Invitado");
            if (tvUserEmail != null) tvUserEmail.setText("Toca para iniciar sesión");
            updatePlanStatus("Free", "");
            btnLogout.setVisibility(View.GONE);
            return;
        }

        String userName = authManager.getUserName();
        String userEmail = authManager.getEmail();
        String planStatus = authManager.getPlanType();
        String planExpiry = authManager.getPlanExpiry();

        if (tvUserName != null) tvUserName.setText(userName != null ? userName : "Usuario");
        if (tvUserEmail != null) tvUserEmail.setText(userEmail != null ? userEmail : "");
        updatePlanStatus(planStatus, planExpiry);
        btnLogout.setVisibility(View.VISIBLE);

        fetchUserDataFromBackend();
    }

    private void updatePlanStatus(String status, String expiry) {
        if (cardPlanStatus != null) {
            TextView planText = cardPlanStatus.findViewById(R.id.tvPlanType);
            TextView expiryText = cardPlanStatus.findViewById(R.id.tvPlanExpiry);
            if (planText != null) planText.setText(status != null ? status : "Free");
            if (expiryText != null) {
                expiryText.setText(expiry != null && !expiry.isEmpty() ? "Vencimiento: " + expiry : "");
            }
        }
    }

    private void fetchUserDataFromBackend() {
        authManager.fetchProfile(new AuthManager.ProfileCallback() {
            @Override
            public void onSuccess(com.tvxargtec.online.models.UserProfile profile) {
                if (tvUserName != null && profile.getName() != null) tvUserName.setText(profile.getName());
                if (tvUserEmail != null && profile.getEmail() != null) tvUserEmail.setText(profile.getEmail());
                updatePlanStatus(profile.getPlanType(), profile.getPlanExpiry());
                if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty() && ivAvatar != null) {
                    Glide.with(ProfileFragment.this)
                        .load(profile.getAvatarUrl())
                        .placeholder(R.drawable.ic_account)
                        .error(R.drawable.ic_account)
                        .circleCrop()
                        .into(ivAvatar);
                }
            }

            @Override
            public void onError(String error) {
                // Datos locales ya cargados, continuar offline
            }
        });
    }

    private void setupListeners() {
        btnMyAccount.setOnClickListener(v -> {
            if (authManager.isLoggedIn()) {
                startActivity(new Intent(getActivity(), VIPMemberActivity.class));
            } else {
                startActivity(new Intent(getActivity(), LoginAty.class));
            }
        });
        btnFavorites.setOnClickListener(v -> {
            MainAty mainAty = MainAty.getInstance();
            if (mainAty != null) {
                mainAty.switchFragment(new FavoritesFragment(), R.id.nav_profile);
            }
        });
        btnHistory.setOnClickListener(v -> navigateToActivity(RecordsAty.class));
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
            if (authManager.isLoggedIn()) {
                MainAty mainAty = MainAty.getInstance();
                if (mainAty != null) {
                    mainAty.switchFragment(new BenefitsFragment(), R.id.nav_profile);
                }
            } else {
                startActivity(new Intent(getActivity(), LoginAty.class));
            }
        });
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        authManager.logout(new AuthManager.LogoutCallback() {
            @Override
            public void onDone() {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Sesión cerrada", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), LoginAty.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });
    }

    private void navigateToActivity(Class<?> cls) {
        if (getActivity() != null) {
            startActivity(new Intent(getActivity(), cls));
        }
    }
}

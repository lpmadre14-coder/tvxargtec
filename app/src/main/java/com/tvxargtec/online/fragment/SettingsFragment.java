package com.tvxargtec.online.fragment;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tvxargtec.online.R;
import com.tvxargtec.online.activity.MainAty;
import com.tvxargtec.online.utils.UpdateManager;

public class SettingsFragment extends Fragment {

    private UpdateManager updateManager;
    private TextView tvUpdateStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_setting, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        TextView tvLanguage = view.findViewById(R.id.tvLanguage);
        TextView tvNotifications = view.findViewById(R.id.tvNotifications);
        TextView tvVersionDisplay = view.findViewById(R.id.tvVersionDisplay);
        MaterialCardView llAbout = view.findViewById(R.id.llAbout);
        MaterialCardView llLogout = view.findViewById(R.id.llLogout);
        MaterialCardView llCheckUpdate = view.findViewById(R.id.llCheckUpdate);
        tvUpdateStatus = view.findViewById(R.id.tvUpdateStatus);

        try {
            String vn = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            if (tvVersionDisplay != null) tvVersionDisplay.setText("v" + vn);
        } catch (PackageManager.NameNotFoundException ignored) {}

        updateManager = new UpdateManager(requireContext(), "lpmadre14-coder", "tvxargtec");

        if (tvLanguage != null) {
            tvLanguage.setOnClickListener(v -> pushFragment(new LanguageFragment()));
        }

        if (tvNotifications != null) {
            tvNotifications.setOnClickListener(v -> pushFragment(new NotificationSettingsFragment()));
        }

        if (llCheckUpdate != null) {
            llCheckUpdate.setOnClickListener(v -> checkForUpdates());
        }

        if (llAbout != null) {
            llAbout.setOnClickListener(v -> pushFragment(new AboutFragment()));
        }

        if (llLogout != null) {
            llLogout.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Sesión cerrada", Toast.LENGTH_SHORT).show());
        }
    }

    private void checkForUpdates() {
        if (tvUpdateStatus != null) tvUpdateStatus.setText("Verificando...");
        Toast.makeText(getActivity(), "Buscando actualizaciones...", Toast.LENGTH_SHORT).show();

        updateManager.checkForUpdates(new UpdateManager.UpdateListener() {
            @Override
            public void onUpdateAvailable(String version, String notes, String apkUrl) {
                if (tvUpdateStatus != null) tvUpdateStatus.setText("¡" + version + " disponible!");
                showUpdateDialog(version, notes, apkUrl);
            }

            @Override
            public void onUpToDate() {
                if (tvUpdateStatus != null) tvUpdateStatus.setText("Actualizado");
                Toast.makeText(getActivity(), "Ya tienes la última versión", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                if (tvUpdateStatus != null) tvUpdateStatus.setText("Error");
                Toast.makeText(getActivity(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pushFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showUpdateDialog(String version, String notes, String apkUrl) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Nueva versión " + version)
                .setMessage(notes.isEmpty() ? "¿Descargar e instalar la actualización?" : notes)
                .setPositiveButton("Actualizar", (dialog, which) -> {
                    updateManager.downloadAndInstall(apkUrl);
                    Toast.makeText(getActivity(), "Descargando...", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Más tarde", null)
                .show();
    }
}

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

import android.widget.EditText;
import android.widget.Toast;
import com.tvxargtec.online.R;
import com.tvxargtec.online.activity.MainAty;
import com.tvxargtec.online.utils.ChannelDataManager;
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
        MaterialCardView llPlaylist = view.findViewById(R.id.llPlaylist);
        tvUpdateStatus = view.findViewById(R.id.tvUpdateStatus);

        try {
            String vn = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            if (tvVersionDisplay != null) tvVersionDisplay.setText("v" + vn);
        } catch (PackageManager.NameNotFoundException ignored) {}

        updateManager = new UpdateManager(requireContext(), "lpmadre14-coder", "tvxargtec");

        if (llPlaylist != null) {
            llPlaylist.setOnClickListener(v -> showPlaylistDialog());
        }

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

    private void showPlaylistDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("https://ejemplo.com/lista.m3u8");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        String saved = requireContext().getSharedPreferences("playlist_prefs", 0).getString("custom_m3u_url", "");
        input.setText(saved);
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(requireContext())
                .setTitle("Playlist personalizada")
                .setMessage("Ingresa la URL de tu lista M3U/M3U8 para agregar canales adicionales")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty() && (url.startsWith("http://") || url.startsWith("https://"))) {
                        requireContext().getSharedPreferences("playlist_prefs", 0)
                                .edit().putString("custom_m3u_url", url).apply();
                        ChannelDataManager.addCustomM3USource(url);
                        Toast.makeText(getActivity(), "Playlist guardada. Recarga la app para ver los cambios.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "URL inválida. Debe comenzar con http:// o https://", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Eliminar", (dialog, which) -> {
                    requireContext().getSharedPreferences("playlist_prefs", 0)
                            .edit().remove("custom_m3u_url").apply();
                    ChannelDataManager.clearCustomM3USource();
                    Toast.makeText(getActivity(), "Playlist eliminada.", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Cancelar", null)
                .show();
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

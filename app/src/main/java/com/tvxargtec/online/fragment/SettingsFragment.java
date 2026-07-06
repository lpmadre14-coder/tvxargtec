package com.tvxargtec.online.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.tvxargtec.online.R;
import com.tvxargtec.online.activity.MainAty;
import com.tvxargtec.online.utils.BackupManager;
import com.tvxargtec.online.utils.BackupCallback;
import com.tvxargtec.online.utils.BackupManager;
import com.tvxargtec.online.utils.ChannelDataManager;
import com.tvxargtec.online.utils.ParentalControlHelper;
import com.tvxargtec.online.utils.UpdateManager;

import java.util.HashSet;
import java.util.Set;


public class SettingsFragment extends Fragment {

    private UpdateManager updateManager;
    private TextView tvUpdateStatus;
    private TextView tvTimeLimitValue;
    private TextView tvThemeValue;

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
        MaterialCardView llCustomCategories = view.findViewById(R.id.llCustomCategories);
        MaterialCardView llParentalControl = view.findViewById(R.id.llParentalControl);
        MaterialCardView llBackup = view.findViewById(R.id.llBackup);
        MaterialCardView llTimeLimit = view.findViewById(R.id.llTimeLimit);
        MaterialCardView llTheme = view.findViewById(R.id.llTheme);
        tvUpdateStatus = view.findViewById(R.id.tvUpdateStatus);
        tvTimeLimitValue = view.findViewById(R.id.tvTimeLimitValue);
        tvThemeValue = view.findViewById(R.id.tvThemeValue);

        try {
            String vn = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            if (tvVersionDisplay != null) tvVersionDisplay.setText("v" + vn);
        } catch (PackageManager.NameNotFoundException ignored) {}

        updateManager = new UpdateManager(requireContext(), "lpmadre14-coder", "tvxargtec");

        if (llPlaylist != null) {
            llPlaylist.setOnClickListener(v -> showPlaylistDialog());
        }

        if (llBackup != null) {
            llBackup.setOnClickListener(v -> showBackupDialog());
        }

        if (tvLanguage != null) {
            tvLanguage.setOnClickListener(v -> pushFragment(new LanguageFragment()));
        }

        if (tvNotifications != null) {
            tvNotifications.setOnClickListener(v -> pushFragment(new NotificationSettingsFragment()));
        }

        if (llCustomCategories != null) {
            llCustomCategories.setOnClickListener(v -> pushFragment(new CustomCategoriesFragment()));
        }

        if (llParentalControl != null) {
            llParentalControl.setOnClickListener(v -> showParentalControlDialog());
        }

        if (llTimeLimit != null) {
            llTimeLimit.setOnClickListener(v -> showTimeLimitDialog());
        }

        if (llTheme != null) {
            llTheme.setOnClickListener(v -> showThemeDialog());
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

        updateTimeLimitDisplay();
        updateThemeDisplay();
    }

    private void updateTimeLimitDisplay() {
        if (tvTimeLimitValue == null) return;
        ParentalControlHelper pc = new ParentalControlHelper(requireContext());
        int limit = pc.getTimeLimitMinutes();
        if (limit <= 0) {
            tvTimeLimitValue.setText("Sin límite");
        } else {
            int remaining = pc.remainingTimeMinutes();
            if (remaining == Integer.MAX_VALUE) {
                tvTimeLimitValue.setText(limit + " min");
            } else {
                tvTimeLimitValue.setText(limit + " min (" + remaining + " restantes)");
            }
        }
    }

    private void updateThemeDisplay() {
        if (tvThemeValue == null) return;
        int mode = requireContext().getSharedPreferences("theme_prefs", 0).getInt("theme_mode", 0);
        switch (mode) {
            case 1: tvThemeValue.setText("Claro"); break;
            case 2: tvThemeValue.setText("Oscuro"); break;
            default: tvThemeValue.setText("Sistema"); break;
        }
    }

    private void showTimeLimitDialog() {
        ParentalControlHelper pc = new ParentalControlHelper(requireContext());
        int currentLimit = pc.getTimeLimitMinutes();

        String[] options = {"Sin límite", "30 min", "60 min", "90 min", "120 min", "180 min"};
        int[] values = {0, 30, 60, 90, 120, 180};
        int checked = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == currentLimit) { checked = i; break; }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Límite de tiempo diario")
                .setSingleChoiceItems(options, checked, (dialog, which) -> {
                    pc.setTimeLimitMinutes(values[which]);
                    updateTimeLimitDisplay();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showThemeDialog() {
        int current = requireContext().getSharedPreferences("theme_prefs", 0).getInt("theme_mode", 0);
        String[] options = {"Sistema", "Claro", "Oscuro"};
        int[] modes = {0, 1, 2};
        int checked = 0;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == current) { checked = i; break; }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Tema de la app")
                .setSingleChoiceItems(options, checked, (dialog, which) -> {
                    requireContext().getSharedPreferences("theme_prefs", 0)
                            .edit().putInt("theme_mode", modes[which]).apply();
                    updateThemeDisplay();
                    dialog.dismiss();
                    requireActivity().recreate();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showParentalControlDialog() {
        ParentalControlHelper pcHelper = new ParentalControlHelper(requireContext());
        String currentPin = pcHelper.getPin();
        Set<String> blocked = pcHelper.getBlockedCategories();

        EditText pinInput = new EditText(requireContext());
        pinInput.setHint("PIN de 6 dígitos");
        pinInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        pinInput.setText(currentPin);
        pinInput.setSelection(pinInput.getText().length());

        String[] allCategories = {"movies", "series", "sports", "news", "entertainment", "music",
                "documentaries", "kids", "education", "anime"};
        String[] categoryLabels = {"Películas", "Series", "Deportes", "Noticias", "Entretenimiento",
                "Música", "Documentales", "Infantil", "Educación", "Anime"};
        boolean[] checked = new boolean[allCategories.length];
        for (int i = 0; i < allCategories.length; i++) {
            checked[i] = blocked.contains(allCategories[i]);
        }

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 16, 40, 16);

        TextView tvPinLabel = new TextView(requireContext());
        tvPinLabel.setText("PIN de control parental");
        tvPinLabel.setTextColor(getResources().getColor(R.color.text_secondary));
        tvPinLabel.setTextSize(13f);
        layout.addView(tvPinLabel);
        layout.addView(pinInput);

        TextView tvBlockLabel = new TextView(requireContext());
        tvBlockLabel.setText("Categorías bloqueadas:");
        tvBlockLabel.setTextColor(getResources().getColor(R.color.text_secondary));
        tvBlockLabel.setTextSize(13f);
        tvBlockLabel.setPadding(0, 24, 0, 0);
        layout.addView(tvBlockLabel);

        for (int i = 0; i < allCategories.length; i++) {
            CheckBox cb = new CheckBox(requireContext());
            cb.setText(categoryLabels[i]);
            cb.setTextColor(getResources().getColor(R.color.text_primary));
            cb.setChecked(checked[i]);
            final int idx = i;
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> checked[idx] = isChecked);
            layout.addView(cb);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Control parental")
                .setView(layout)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String newPin = pinInput.getText().toString().trim();
                    if (newPin.length() < 4) {
                        Toast.makeText(getActivity(), "El PIN debe tener al menos 4 dígitos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pcHelper.setPin(newPin);
                    Set<String> newBlocked = new java.util.HashSet<>();
                    for (int i = 0; i < allCategories.length; i++) {
                        if (checked[i]) newBlocked.add(allCategories[i]);
                    }
                    pcHelper.setBlockedCategories(newBlocked);
                    int count = newBlocked.size();
                    String msg = count > 0 ? count + " categorías bloqueadas" : "Control parental desactivado";
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
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

    private void showBackupDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Respaldo en la nube")
                .setMessage("Elige una opcion para respaldar o restaurar tus datos (favoritos, historial y categorias personalizadas).")
                .setPositiveButton("Respaldar ahora", (dialog, which) -> {
                    ProgressDialog progress = new ProgressDialog(requireContext());
                    progress.setMessage("Respaldando datos...");
                    progress.setCancelable(false);
                    progress.show();
                    new BackupManager(requireContext()).backup(new BackupCallback() {
                        @Override
                        public void onComplete(boolean success, String message) {
                            requireActivity().runOnUiThread(() -> {
                                progress.dismiss();
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Restaurar", (dialog, which) -> {
                    ProgressDialog progress = new ProgressDialog(requireContext());
                    progress.setMessage("Restaurando datos...");
                    progress.setCancelable(false);
                    progress.show();
                    new BackupManager(requireContext()).restore(new BackupCallback() {
                        @Override
                        public void onComplete(boolean success, String message) {
                            requireActivity().runOnUiThread(() -> {
                                progress.dismiss();
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                })
                .setNeutralButton("Cancelar", null)
                .show();
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

package com.tvxargtec.online.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tvxargtec.online.R;

public class NotificationSettingsFragment extends Fragment {

    private Switch switchAlerts, switchContent, switchPromos, switchSystem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        switchAlerts = view.findViewById(R.id.switchAlerts);
        switchContent = view.findViewById(R.id.switchContent);
        switchPromos = view.findViewById(R.id.switchPromos);
        switchSystem = view.findViewById(R.id.switchSystem);

        if (switchAlerts != null)
            switchAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> savePreference("alerts", isChecked));
        if (switchContent != null)
            switchContent.setOnCheckedChangeListener((buttonView, isChecked) -> savePreference("content", isChecked));
        if (switchPromos != null)
            switchPromos.setOnCheckedChangeListener((buttonView, isChecked) -> savePreference("promos", isChecked));
        if (switchSystem != null)
            switchSystem.setOnCheckedChangeListener((buttonView, isChecked) -> savePreference("system", isChecked));

        loadPreferences();
    }

    private void savePreference(String key, boolean value) {
        requireActivity().getSharedPreferences("notifications", 0)
                .edit().putBoolean(key, value).apply();
    }

    private void loadPreferences() {
        var prefs = requireActivity().getSharedPreferences("notifications", 0);
        if (switchAlerts != null) switchAlerts.setChecked(prefs.getBoolean("alerts", true));
        if (switchContent != null) switchContent.setChecked(prefs.getBoolean("content", true));
        if (switchPromos != null) switchPromos.setChecked(prefs.getBoolean("promos", true));
        if (switchSystem != null) switchSystem.setChecked(prefs.getBoolean("system", true));
    }
}

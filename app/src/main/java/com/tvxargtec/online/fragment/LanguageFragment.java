package com.tvxargtec.online.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tvxargtec.online.R;
import com.tvxargtec.online.activity.MainAty;
import com.tvxargtec.online.activity.TransitionLanguageAty;

public class LanguageFragment extends Fragment {

    private final String[][] languages = {
        {"en", "English"},
        {"es", "Español"},
        {"pt", "Português"},
        {"fr", "Français"},
        {"de", "Deutsch"},
        {"it", "Italiano"},
        {"ru", "Русский"},
        {"zh", "中文"}
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_language, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ListView listView = view.findViewById(R.id.listView);
        String[] names = new String[languages.length];
        for (int i = 0; i < languages.length; i++) names[i] = languages[i][1];

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, names);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, v, position, id) -> {
            Intent intent = new Intent(requireContext(), TransitionLanguageAty.class);
            intent.putExtra("lang", languages[position][0]);
            startActivity(intent);
        });
    }
}

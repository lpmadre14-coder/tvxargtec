package com.tvxargtec.online.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tvxargtec.online.R;

public class DownloadsFragment extends Fragment {

    private RecyclerView rvDownloads;
    private TextView tvEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_downloads, container, false);
        
        rvDownloads = view.findViewById(R.id.rvDownloads);
        tvEmptyState = view.findViewById(R.id.tvEmpty);
        
        setupRecyclerView();
        loadDownloads();
        
        return view;
    }

    private void setupRecyclerView() {
        if (rvDownloads != null) {
            rvDownloads.setLayoutManager(new LinearLayoutManager(getContext()));
            // TODO: Implementar adapter para mostrar descargas
        }
    }

    private void loadDownloads() {
        // TODO: Cargar descargas desde backend o base de datos local
        // Por ahora mostrar estado vacío
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No hay descargas disponibles");
        }
    }
}

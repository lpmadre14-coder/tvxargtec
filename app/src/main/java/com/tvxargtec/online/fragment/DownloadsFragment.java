package com.tvxargtec.online.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.tvxargtec.online.adapter.DownloadsAdapter;
import com.tvxargtec.online.utils.OfflineManager;

public class DownloadsFragment extends Fragment {

    private RecyclerView rvDownloads;
    private TextView tvEmptyState;
    private DownloadsAdapter adapter;
    private OfflineManager offlineManager;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_downloads, container, false);

        rvDownloads = view.findViewById(R.id.rvDownloads);
        tvEmptyState = view.findViewById(R.id.tvEmpty);

        offlineManager = OfflineManager.Companion.getInstance(requireContext());
        setupRecyclerView();
        refreshDownloads();

        return view;
    }

    private void setupRecyclerView() {
        rvDownloads.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DownloadsAdapter(requireContext(), offlineManager.getDownloads());
        rvDownloads.setAdapter(adapter);
    }

    private void refreshDownloads() {
        if (!isAdded()) return;
        var downloads = offlineManager.getDownloads();
        if (downloads.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvDownloads.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvDownloads.setVisibility(View.VISIBLE);
            if (adapter != null) {
                adapter.updateDownloads(downloads);
            }
        }
        handler.postDelayed(this::refreshDownloads, 2000);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshDownloads();
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}

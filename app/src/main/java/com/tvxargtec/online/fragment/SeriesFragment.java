package com.tvxargtec.online.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.tvxargtec.online.R;
import com.tvxargtec.online.adapter.ChannelAdapter;
import com.tvxargtec.online.utils.Channel;
import com.tvxargtec.online.utils.ChannelDataManager;

import java.util.List;
import kotlin.Unit;

public class SeriesFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvSeries;
    private FrameLayout loadingContainer;
    private Chip chipAll, chipAction, chipDrama, chipComedy, chipSciFi;
    private Chip selectedChip;
    private String selectedCategory = "series";
    private ChannelAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_series, container, false);
        initViews(view);
        setupListeners();
        loadChannels();
        return view;
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        rvSeries = view.findViewById(R.id.rvSeries);
        loadingContainer = view.findViewById(R.id.loadingContainer);

        swipeRefresh.setColorSchemeColors(
                getResources().getColor(R.color.brand_violet),
                getResources().getColor(R.color.brand_cyan),
                getResources().getColor(R.color.brand_green)
        );

        int spanCount = ChannelDataManager.isTelevision(requireContext()) ? 4 : 2;
        rvSeries.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        chipAll = view.findViewById(R.id.chipAll);
        chipAction = view.findViewById(R.id.chipAction);
        chipDrama = view.findViewById(R.id.chipDrama);
        chipComedy = view.findViewById(R.id.chipComedy);
        chipSciFi = view.findViewById(R.id.chipSciFi);

        selectedChip = chipAll;
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadChannels);

        View.OnClickListener chipListener = v -> {
            Chip chip = (Chip) v;
            if (selectedChip != null) selectedChip.setSelected(false);
            chip.setSelected(true);
            selectedChip = chip;

            if (chip == chipAll) selectedCategory = "series";
            else if (chip == chipAction) selectedCategory = "series";
            else if (chip == chipDrama) selectedCategory = "series";
            else if (chip == chipComedy) selectedCategory = "series";
            else if (chip == chipSciFi) selectedCategory = "series";

            loadChannels();
        };

        chipAll.setOnClickListener(chipListener);
        chipAction.setOnClickListener(chipListener);
        chipDrama.setOnClickListener(chipListener);
        chipComedy.setOnClickListener(chipListener);
        chipSciFi.setOnClickListener(chipListener);
    }

    private void loadChannels() {
        if (loadingContainer != null) loadingContainer.setVisibility(View.VISIBLE);

        ChannelDataManager.fetchRemoteM3USources(requireContext(), new ChannelDataManager.DataCallback() {
            @Override
            public void onDataLoaded(List<Channel> channels) {
                List<Channel> filtered = ChannelDataManager.getChannels(requireContext(), selectedCategory);
                if (adapter == null) {
                    adapter = new ChannelAdapter(requireContext(), filtered, channel -> Unit.INSTANCE);
                    rvSeries.setAdapter(adapter);
                } else {
                    adapter.updateChannels(filtered);
                }
                if (loadingContainer != null) loadingContainer.setVisibility(View.GONE);
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onError(Exception e) {
                List<Channel> channels = ChannelDataManager.getChannels(requireContext(), selectedCategory);
                if (adapter == null) {
                    adapter = new ChannelAdapter(requireContext(), channels, channel -> Unit.INSTANCE);
                    rvSeries.setAdapter(adapter);
                } else {
                    adapter.updateChannels(channels);
                }
                if (loadingContainer != null) loadingContainer.setVisibility(View.GONE);
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            }
        });
    }
}

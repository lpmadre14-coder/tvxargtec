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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.tvxargtec.online.R;
import com.tvxargtec.online.adapter.ChannelAdapter;
import com.tvxargtec.online.utils.Channel;
import com.tvxargtec.online.utils.ChannelDataManager;

import java.util.List;
import kotlin.Unit;

public class MoviesFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvMovies;
    private FrameLayout loadingContainer;
    private MaterialButton btnWatchFeatured;
    private Chip chipAll, chipAction, chipComedy, chipDrama, chipHorror;
    private Chip selectedChip;
    private String selectedCategory = "movies";
    private ChannelAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movies, container, false);
        initViews(view);
        setupListeners();
        loadChannels();
        return view;
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        rvMovies = view.findViewById(R.id.rvMovies);
        loadingContainer = view.findViewById(R.id.loadingContainer);
        btnWatchFeatured = view.findViewById(R.id.btnWatchFeatured);

        swipeRefresh.setColorSchemeColors(
                getResources().getColor(R.color.brand_violet),
                getResources().getColor(R.color.brand_cyan),
                getResources().getColor(R.color.brand_green)
        );

        rvMovies.setLayoutManager(new GridLayoutManager(getContext(), 2));

        chipAll = view.findViewById(R.id.chipAll);
        chipAction = view.findViewById(R.id.chipAction);
        chipComedy = view.findViewById(R.id.chipComedy);
        chipDrama = view.findViewById(R.id.chipDrama);
        chipHorror = view.findViewById(R.id.chipHorror);

        selectedChip = chipAll;
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadChannels);

        btnWatchFeatured.setOnClickListener(v -> {
            if (getActivity() != null) {
                android.widget.Toast.makeText(getActivity(), "Reproduciendo película destacada", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        View.OnClickListener chipListener = v -> {
            Chip chip = (Chip) v;
            if (selectedChip != null) selectedChip.setSelected(false);
            chip.setSelected(true);
            selectedChip = chip;

            if (chip == chipAll) selectedCategory = "movies";
            else if (chip == chipAction) selectedCategory = "movies";
            else if (chip == chipComedy) selectedCategory = "movies";
            else if (chip == chipDrama) selectedCategory = "movies";
            else if (chip == chipHorror) selectedCategory = "movies";

            loadChannels();
        };

        chipAll.setOnClickListener(chipListener);
        chipAction.setOnClickListener(chipListener);
        chipComedy.setOnClickListener(chipListener);
        chipDrama.setOnClickListener(chipListener);
        chipHorror.setOnClickListener(chipListener);
    }

    private void loadChannels() {
        if (loadingContainer != null) loadingContainer.setVisibility(View.VISIBLE);

        List<Channel> channels = ChannelDataManager.getChannels(requireContext(), selectedCategory);
        if (adapter == null) {
            adapter = new ChannelAdapter(requireContext(), channels, channel -> Unit.INSTANCE);
            rvMovies.setAdapter(adapter);
        } else {
            adapter.updateChannels(channels);
        }

        if (loadingContainer != null) loadingContainer.setVisibility(View.GONE);
        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
    }
}

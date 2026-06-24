package com.tvxargtec.online.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.tvxargtec.online.R;
import com.tvxargtec.online.adapter.ChannelAdapter;
import com.tvxargtec.online.utils.Channel;
import com.tvxargtec.online.utils.ChannelDataManager;

import java.util.List;
import kotlin.Unit;

public class HomeFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private FrameLayout loadingContainer;
    private RecyclerView rvContinueWatching, rvLiveChannels, rvRecommended, rvNewReleases, rvTrending;
    private RecyclerView rvSearchResults;
    private MaterialButton btnWatchNow, btnMoreInfo;
    private EditText etSearch;
    private ImageView btnClearSearch;
    private View heroBanner, contentContainer;
    private ChannelAdapter adapterContinueWatching, adapterLiveChannels, adapterRecommended, adapterNewReleases, adapterTrending;
    private ChannelAdapter searchAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(view);
        setupListeners();
        loadChannels();
        return view;
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        loadingContainer = view.findViewById(R.id.loadingContainer);
        btnWatchNow = view.findViewById(R.id.btnWatchNow);
        btnMoreInfo = view.findViewById(R.id.btnMoreInfo);
        heroBanner = view.findViewById(R.id.heroBanner);
        contentContainer = view.findViewById(R.id.contentContainer);

        etSearch = view.findViewById(R.id.etSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);

        swipeRefresh.setColorSchemeColors(
                getResources().getColor(R.color.brand_violet),
                getResources().getColor(R.color.brand_cyan),
                getResources().getColor(R.color.brand_green)
        );

        rvContinueWatching = view.findViewById(R.id.rvContinueWatching);
        rvLiveChannels = view.findViewById(R.id.rvLiveChannels);
        rvRecommended = view.findViewById(R.id.rvRecommended);
        rvNewReleases = view.findViewById(R.id.rvNewReleases);
        rvTrending = view.findViewById(R.id.rvTrending);

        setupHorizontalRecycler(rvContinueWatching);
        setupHorizontalRecycler(rvLiveChannels);
        setupHorizontalRecycler(rvRecommended);
        setupHorizontalRecycler(rvNewReleases);
        setupHorizontalRecycler(rvTrending);

        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupHorizontalRecycler(RecyclerView rv) {
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadChannels);

        btnWatchNow.setOnClickListener(v -> {
            if (getActivity() != null) {
                android.widget.Toast.makeText(getActivity(), "Reproduciendo contenido destacado", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        btnMoreInfo.setOnClickListener(v -> {
            if (getActivity() != null) {
                android.widget.Toast.makeText(getActivity(), "Mostrando información detallada", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    hideSearch();
                } else {
                    showSearchResults(query);
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            hideSearch();
        });
    }

    private void showSearchResults(String query) {
        if (heroBanner != null) heroBanner.setVisibility(View.GONE);
        if (contentContainer != null) contentContainer.setVisibility(View.GONE);
        btnClearSearch.setVisibility(View.VISIBLE);
        rvSearchResults.setVisibility(View.VISIBLE);

        List<Channel> results = ChannelDataManager.searchChannels(requireContext(), query);
        if (searchAdapter == null) {
            searchAdapter = new ChannelAdapter(requireContext(), results, channel -> Unit.INSTANCE);
            rvSearchResults.setAdapter(searchAdapter);
        } else {
            searchAdapter.updateChannels(results);
        }
    }

    private void hideSearch() {
        if (heroBanner != null) heroBanner.setVisibility(View.VISIBLE);
        if (contentContainer != null) contentContainer.setVisibility(View.VISIBLE);
        btnClearSearch.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.GONE);
    }

    private void loadChannels() {
        if (loadingContainer != null) loadingContainer.setVisibility(View.VISIBLE);

        ChannelDataManager.fetchRemoteM3USources(requireContext(), new ChannelDataManager.DataCallback() {
            @Override
            public void onDataLoaded(List<Channel> channels) {
                updateChannelSections(channels);
            }

            @Override
            public void onError(Exception e) {
                updateChannelSections(ChannelDataManager.getChannels(requireContext(), ""));
            }
        });
    }

    private void updateChannelSections(List<Channel> allChannels) {
        List<Channel> continueWatching = allChannels.subList(0, Math.min(5, allChannels.size()));
        if (adapterContinueWatching == null) {
            adapterContinueWatching = new ChannelAdapter(requireContext(), continueWatching, channel -> Unit.INSTANCE);
            rvContinueWatching.setAdapter(adapterContinueWatching);
        } else {
            adapterContinueWatching.updateChannels(continueWatching);
        }

        if (adapterLiveChannels == null) {
            adapterLiveChannels = new ChannelAdapter(requireContext(), allChannels, channel -> Unit.INSTANCE);
            rvLiveChannels.setAdapter(adapterLiveChannels);
        } else {
            adapterLiveChannels.updateChannels(allChannels);
        }

        List<Channel> recommended = allChannels.subList(0, Math.min(5, allChannels.size()));
        if (adapterRecommended == null) {
            adapterRecommended = new ChannelAdapter(requireContext(), recommended, channel -> Unit.INSTANCE);
            rvRecommended.setAdapter(adapterRecommended);
        } else {
            adapterRecommended.updateChannels(recommended);
        }

        List<Channel> newReleases = allChannels.subList(Math.max(0, allChannels.size() - 5), allChannels.size());
        if (adapterNewReleases == null) {
            adapterNewReleases = new ChannelAdapter(requireContext(), newReleases, channel -> Unit.INSTANCE);
            rvNewReleases.setAdapter(adapterNewReleases);
        } else {
            adapterNewReleases.updateChannels(newReleases);
        }

        int start = Math.max(0, (allChannels.size() - 5) / 2);
        int end = Math.min(allChannels.size(), start + 5);
        List<Channel> trending = allChannels.subList(start, end);
        if (adapterTrending == null) {
            adapterTrending = new ChannelAdapter(requireContext(), trending, channel -> Unit.INSTANCE);
            rvTrending.setAdapter(adapterTrending);
        } else {
            adapterTrending.updateChannels(trending);
        }

        if (loadingContainer != null) loadingContainer.setVisibility(View.GONE);
        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
    }
}

package com.tvxargtec.online.fragment;

import android.content.Intent;
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
import com.tvxargtec.online.activity.PlayAty;
import com.tvxargtec.online.database.AppDatabase;
import com.tvxargtec.online.database.entity.FavoriteEntity;
import com.tvxargtec.online.utils.Channel;
import com.tvxargtec.online.utils.ChannelDataManager;
import com.tvxargtec.online.utils.ChannelItem;
import com.tvxargtec.online.utils.LocalDataManager;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private TextView tvEmpty;
    private FavoriteListAdapter adapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        rvFavorites = view.findViewById(R.id.rvFavorites);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        db = AppDatabase.getInstance(requireContext());
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        loadFavorites();
        return view;
    }

    private void loadFavorites() {
        List<FavoriteEntity> favEntities = db.favoriteDao().getAllFavorites();
        List<Channel> allChannels = ChannelDataManager.getChannels(requireContext(), "");
        List<ChannelItem> favorites = new ArrayList<>();

        for (FavoriteEntity fav : favEntities) {
            for (Channel ch : allChannels) {
                if (ch.getId().equals(fav.contentId)) {
                    favorites.add(new ChannelItem(ch.getId(), ch.getTitle(), ch.getUrl(), ch.getLogo(), ch.getCategoryName()));
                    break;
                }
            }
        }

        if (favorites.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.VISIBLE);
            adapter = new FavoriteListAdapter(favorites, item -> {
                Intent intent = new Intent(getActivity(), PlayAty.class);
                intent.putExtra("url", item.url);
                intent.putExtra("title", item.title);
                startActivity(intent);
            });
            rvFavorites.setAdapter(adapter);
        }
    }

    private static class FavoriteListAdapter extends RecyclerView.Adapter<FavoriteListAdapter.ViewHolder> {
        private final List<ChannelItem> items;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onClick(ChannelItem item);
        }

        FavoriteListAdapter(List<ChannelItem> items, OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_simple, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChannelItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvCategory.setText(item.getCategory() != null ? item.getCategory() : "");
            holder.itemView.setOnClickListener(v -> listener.onClick(item));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvCategory;
            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvFavTitle);
                tvCategory = v.findViewById(R.id.tvFavCategory);
            }
        }
    }
}

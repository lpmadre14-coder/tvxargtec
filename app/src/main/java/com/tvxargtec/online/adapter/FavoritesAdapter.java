package com.tvxargtec.online.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tvxargtec.online.R;
import com.tvxargtec.online.models.Content;

import java.util.List;

/**
 * Adaptador para mostrar favoritos en RecyclerView
 */
public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private List<Content> favoritesList;
    private Context context;
    private OnFavoriteActionListener listener;

    public FavoritesAdapter(Context context, List<Content> favoritesList, OnFavoriteActionListener listener) {
        this.context = context;
        this.favoritesList = favoritesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Content content = favoritesList.get(position);
        holder.bind(content);
    }

    @Override
    public int getItemCount() {
        return favoritesList != null ? favoritesList.size() : 0;
    }

    public void updateList(List<Content> newList) {
        this.favoritesList = newList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < favoritesList.size()) {
            favoritesList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPoster;
        private TextView tvTitle, tvCategory, tvRating;
        private ImageButton btnRemove;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnRemove = itemView.findViewById(R.id.btnRemoveFavorite);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(favoritesList.get(getAdapterPosition()));
                }
            });

            btnRemove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position >= 0) {
                    listener.onRemoveFavorite(favoritesList.get(position), position);
                }
            });
        }

        public void bind(Content content) {
            // Cargar poster con Glide
            Glide.with(context)
                .load(content.getPosterUrl())
                .placeholder(R.drawable.ic_account)
                .error(R.drawable.ic_account)
                .into(ivPoster);

            // Configurar textos
            tvTitle.setText(content.getTitle());
            tvCategory.setText(content.getCategory());
            tvRating.setText(String.format("%.1f ★", content.getRating()));
        }
    }

    public interface OnFavoriteActionListener {
        void onFavoriteClick(Content content);
        void onRemoveFavorite(Content content, int position);
    }
}

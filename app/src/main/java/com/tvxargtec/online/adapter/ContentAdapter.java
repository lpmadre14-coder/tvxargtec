package com.tvxargtec.online.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tvxargtec.online.R;
import com.tvxargtec.online.models.Content;

import java.util.List;

/**
 * Adaptador para mostrar contenido en RecyclerView
 */
public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ContentViewHolder> {

    private List<Content> contentList;
    private Context context;
    private OnContentClickListener listener;

    public ContentAdapter(Context context, List<Content> contentList, OnContentClickListener listener) {
        this.context = context;
        this.contentList = contentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_content, parent, false);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContentViewHolder holder, int position) {
        Content content = contentList.get(position);
        holder.bind(content);
    }

    @Override
    public int getItemCount() {
        return contentList != null ? contentList.size() : 0;
    }

    public void updateList(List<Content> newList) {
        this.contentList = newList;
        notifyDataSetChanged();
    }

    public class ContentViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPoster;
        private TextView tvTitle, tvCategory, tvRating;

        public ContentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvRating = itemView.findViewById(R.id.tvRating);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContentClick(contentList.get(getAdapterPosition()));
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

    public interface OnContentClickListener {
        void onContentClick(Content content);
    }
}

package com.tvxargtec.online.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.exoplayer.offline.Download;
import androidx.recyclerview.widget.RecyclerView;

import com.tvxargtec.online.R;
import com.tvxargtec.online.activity.PlayAty;
import com.tvxargtec.online.utils.OfflineManager;

import java.util.List;

public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.ViewHolder> {

    private final Context context;
    private List<Download> downloads;

    public DownloadsAdapter(Context context, List<Download> downloads) {
        this.context = context;
        this.downloads = downloads;
    }

    public void updateDownloads(List<Download> newDownloads) {
        this.downloads = newDownloads;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_download, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Download download = downloads.get(position);
        final String title;
        String rawTitle = new String(download.request.data != null ? download.request.data : new byte[0]);
        title = rawTitle.isEmpty() ? "Contenido" : rawTitle;

        holder.tvTitle.setText(title);
        final String statusText;
        String baseStatus = downloadStateToString(download.state);
        if (download.state == Download.STATE_DOWNLOADING) {
            float pct = OfflineManager.Companion.getInstance(context).getProgress(download.request.id);
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressBar.setProgress((int) (pct * 100));
            statusText = baseStatus + " " + (int) (pct * 100) + "%";
        } else {
            holder.progressBar.setVisibility(View.GONE);
            statusText = baseStatus;
        }
        holder.tvStatus.setText(statusText);

        holder.btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayAty.class);
            intent.putExtra("url", download.request.uri.toString());
            intent.putExtra("title", title);
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            OfflineManager.Companion.getInstance(context).removeDownload(download.request.id);
            downloads.remove(position);
            notifyItemRemoved(position);
            if (downloads.isEmpty()) {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return downloads.size();
    }

    private String downloadStateToString(int state) {
        switch (state) {
            case Download.STATE_DOWNLOADING: return "Descargando";
            case Download.STATE_COMPLETED: return "Completado";
            case Download.STATE_FAILED: return "Error";
            case Download.STATE_REMOVING: return "Eliminando";
            case Download.STATE_RESTARTING: return "Reiniciando";
            case Download.STATE_QUEUED: return "En cola";
            default: return "Pendiente";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvStatus;
        ProgressBar progressBar;
        ImageView btnPlay, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvDownloadTitle);
            tvStatus = itemView.findViewById(R.id.tvDownloadStatus);
            progressBar = itemView.findViewById(R.id.progressDownload);
            btnPlay = itemView.findViewById(R.id.btnPlayDownload);
            btnDelete = itemView.findViewById(R.id.btnDeleteDownload);
        }
    }
}

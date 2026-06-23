package com.tvxargtec.online.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tvxargtec.online.R
import com.tvxargtec.online.activity.PlayAty
import com.tvxargtec.online.utils.Channel
import com.tvxargtec.online.utils.LocalDataManager

class ChannelAdapter(
    private val context: Context,
    private var channels: List<Channel>,
    private val listener: (Channel) -> Unit
) : RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {

    private val dataManager = LocalDataManager(context)

    fun updateChannels(newChannels: List<Channel>) {
        channels = newChannels
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_channel, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val channel = channels[position]
        holder.bind(channel)
    }

    override fun getItemCount() = channels.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivLogo: ImageView = itemView.findViewById(R.id.ivChannelLogo)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvChannelName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvChannelCategory)
        private val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)

        fun bind(channel: Channel) {
            tvTitle.text = channel.title
            tvCategory.text = channel.categoryName

            Glide.with(context)
                .load(channel.logo)
                .placeholder(R.drawable.ic_play)
                .error(R.drawable.ic_play)
                .into(ivLogo)

            val isFav = dataManager.isFavorite(channel.id)
            ivFavorite.setImageResource(
                if (isFav) android.R.drawable.star_on else android.R.drawable.star_off
            )

            ivFavorite.setOnClickListener {
                if (dataManager.isFavorite(channel.id)) {
                    dataManager.removeFavorite(channel.id)
                    ivFavorite.setImageResource(android.R.drawable.star_off)
                } else {
                    dataManager.addFavorite(
                        com.tvxargtec.online.utils.ChannelItem(
                            channel.id, channel.title, channel.url, channel.logo, channel.categoryName
                        )
                    )
                    ivFavorite.setImageResource(android.R.drawable.star_on)
                }
            }

            itemView.setOnClickListener {
                listener(channel)
                val intent = Intent(context, PlayAty::class.java).apply {
                    putExtra("url", channel.url)
                    putExtra("title", channel.title)
                }
                context.startActivity(intent)
                dataManager.addToHistory(
                    com.tvxargtec.online.utils.ChannelItem(
                        channel.id, channel.title, channel.url, channel.logo, channel.categoryName
                    )
                )
            }
        }
    }
}

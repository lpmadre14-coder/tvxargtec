package com.tvxargtec.online.adapter

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tvxargtec.online.R
import com.tvxargtec.online.activity.PlayAty
import com.tvxargtec.online.database.AppDatabase
import com.tvxargtec.online.database.entity.FavoriteEntity
import com.tvxargtec.online.utils.Channel
import com.tvxargtec.online.utils.EpgHelper
import com.tvxargtec.online.utils.EpgProgramme
import com.tvxargtec.online.utils.LocalDataManager
import com.tvxargtec.online.utils.ParentalControlHelper
import java.util.HashSet

class ChannelAdapter @JvmOverloads constructor(
    private val context: Context,
    private var channels: List<Channel>,
    private val listener: (Channel) -> Unit,
    private val onBlockedClick: java.util.function.Consumer<Channel>? = null
) : RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {

    private val dataManager = LocalDataManager(context)
    private val favoriteIds = HashSet<String>()
    private val db = AppDatabase.getInstance(context)

    init {
        for (fav in db.favoriteDao().getAllFavorites()) {
            favoriteIds.add(fav.contentId)
        }
        setHasStableIds(true)
    }

    fun updateChannels(newChannels: List<Channel>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = channels.size
            override fun getNewListSize() = newChannels.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
                return channels[oldPos].id == newChannels[newPos].id
            }
            override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
                val old = channels[oldPos]
                val new = newChannels[newPos]
                return old.title == new.title && old.url == new.url && old.logo == new.logo
            }
        })
        channels = newChannels
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemId(position: Int): Long {
        return channels[position].id.hashCode().toLong()
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
        private val tvEpg: TextView = itemView.findViewById(R.id.tvChannelEpg)
        private val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)

        fun bind(channel: Channel) {
            tvTitle.text = channel.title
            tvCategory.text = channel.categoryName

            Glide.with(context)
                .load(channel.logo)
                .placeholder(R.drawable.ic_play)
                .error(R.drawable.ic_play)
                .into(ivLogo)

            val isFav = favoriteIds.contains(channel.id)
            ivFavorite.setImageResource(
                if (isFav) android.R.drawable.star_on else android.R.drawable.star_off
            )

            ivFavorite.setOnClickListener {
                if (favoriteIds.contains(channel.id)) {
                    favoriteIds.remove(channel.id)
                    db.favoriteDao().deleteFavoriteByContentId(channel.id)
                    ivFavorite.setImageResource(android.R.drawable.star_off)
                } else {
                    favoriteIds.add(channel.id)
                    db.favoriteDao().addFavorite(FavoriteEntity(channel.id))
                    ivFavorite.setImageResource(android.R.drawable.star_on)
                }
            }

            itemView.setOnClickListener {
                val pcHelper = ParentalControlHelper(context)
                if (pcHelper.isCategoryBlocked(channel.categoryId)) {
                    onBlockedClick?.accept(channel)
                } else {
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

            fetchEpg(channel)
        }

        private fun fetchEpg(channel: Channel) {
            EpgHelper.fetchNowPlaying(channel.title, object : EpgHelper.Callback {
                override fun invoke(current: EpgProgramme?, next: EpgProgramme?) {
                    Handler(Looper.getMainLooper()).post {
                        if (current != null && current.title != null && current.title.isNotEmpty()) {
                            tvEpg.text = "▶ ${current.title}"
                            tvEpg.visibility = View.VISIBLE
                        } else {
                            tvEpg.visibility = View.GONE
                        }
                    }
                }
            })
        }
    }
}

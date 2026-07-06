package com.tvxargtec.online.tv

import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.tvxargtec.online.R
import com.tvxargtec.online.utils.Channel

class TvCardPresenter : Presenter() {

    companion object {
        private const val CARD_WIDTH = 313
        private const val CARD_HEIGHT = 176
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val channel = item as Channel
        val cardView = viewHolder.view as ImageCardView

        cardView.titleText = channel.title
        cardView.contentText = channel.categoryName.ifEmpty { "Canal" }

        cardView.setBackgroundColor(0xFF1A1A2E.toInt())

        Glide.with(cardView.context)
            .load(channel.logo)
            .placeholder(R.drawable.ic_tv)
            .error(R.drawable.ic_tv)
            .fitCenter()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(cardView.mainImageView)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.badgeImage = null
    }
}

package com.tvxargtec.online.tv

import android.os.Bundle
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import com.tvxargtec.online.R
import com.tvxargtec.online.activity.PlayAty
import com.tvxargtec.online.utils.Channel
import com.tvxargtec.online.utils.ChannelDataManager

class TvBrowseFragment : BrowseSupportFragment() {

    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupUI()
        loadChannels()
    }

    private fun setupUI() {
        title = "Tvxargtec"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = 0xFF7C3AED.toInt()
        adapter = rowsAdapter
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is Channel) {
                val intent = android.content.Intent(requireContext(), PlayAty::class.java).apply {
                    putExtra("url", item.url)
                    putExtra("title", item.title)
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }
        }
    }

    private fun loadChannels() {
        val context = context ?: return
        val channels = ChannelDataManager.getChannels(context)
        val grouped = channels.groupBy { it.categoryName.ifEmpty { "Otros" } }

        grouped.forEach { (category, channelList) ->
            val cardAdapter = ArrayObjectAdapter(TvCardPresenter())
            channelList.forEach { cardAdapter.add(it) }
            val header = HeaderItem(category.hashCode().toLong(), category)
            rowsAdapter.add(ListRow(header, cardAdapter))
        }
    }
}

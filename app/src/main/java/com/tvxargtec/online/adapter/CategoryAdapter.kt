package com.tvxargtec.online.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tvxargtec.online.R
import com.tvxargtec.online.utils.Category

class CategoryAdapter(
    private val context: Context,
    private var categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var selectedPosition = 0

    fun updateData(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category, position == selectedPosition)
    }

    override fun getItemCount() = categories.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvCategoryName)

        fun bind(category: Category, isSelected: Boolean) {
            tvName.text = category.name
            tvName.isSelected = isSelected
            tvName.setBackgroundResource(
                if (isSelected) R.drawable.bg_category_selected
                else R.drawable.bg_category_normal
            )
            tvName.setTextColor(
                if (isSelected) 0xFFE94560.toInt()
                else 0xFFB0B0B0.toInt()
            )

            itemView.setOnClickListener {
                val oldPos = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(oldPos)
                notifyItemChanged(selectedPosition)
                onCategoryClick(category)
            }
        }
    }
}

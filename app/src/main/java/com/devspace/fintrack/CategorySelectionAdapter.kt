package com.devspace.fintrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devspace.fintrack.R

class CategorySelectionAdapter(
    private val onCategorySelected: (CategoryUiData) -> Unit
) : ListAdapter<CategoryUiData, CategorySelectionAdapter.ViewHolder>(DIFF_CALLBACK) {

    private var onLongClickListener: ((CategoryUiData) -> Unit)? = null

    fun setOnLongClickListener(listener: (CategoryUiData) -> Unit) {
        onLongClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.iv_category_icon)
        private val nameView: TextView = itemView.findViewById(R.id.tv_category_name)

        fun bind(category: CategoryUiData) {
            iconView.setImageResource(category.iconRes)
            
            // Esconde o nome para o botão de adicionar
            if (category.name == "+") {
                nameView.visibility = View.GONE
            } else {
                nameView.visibility = View.VISIBLE
                nameView.text = category.name
            }
            
            val background = if (category.isSelected) {
                R.drawable.circle_background_selected
            } else {
                R.drawable.circle_background
            }
            iconView.setBackgroundResource(background)

            itemView.setOnClickListener {
                onCategorySelected(category)
            }

            itemView.setOnLongClickListener { 
                onLongClickListener?.invoke(category)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_selection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CategoryUiData>() {
            override fun areItemsTheSame(oldItem: CategoryUiData, newItem: CategoryUiData): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: CategoryUiData, newItem: CategoryUiData): Boolean {
                return oldItem == newItem
            }
        }
    }
} 
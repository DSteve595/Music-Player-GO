package com.iven.musicplayergo.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iven.musicplayergo.R
import com.iven.musicplayergo.musicPlayerGoExAppPreferences

class HiddenItemsAdapter :
    RecyclerView.Adapter<HiddenItemsAdapter.HiddenItemsHolder>() {

    private val mHiddenItems: MutableList<String> =
        musicPlayerGoExAppPreferences.hiddenItems!!.toMutableList()

    init {
        mHiddenItems.sort()
    }

    fun getUpdatedHiddenItems(): Set<String> {
        return mHiddenItems.toSet()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiddenItemsHolder {
        return HiddenItemsHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recycler_view_hidden_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mHiddenItems.size
    }

    override fun onBindViewHolder(holder: HiddenItemsHolder, position: Int) {
        holder.bindItems(mHiddenItems[holder.adapterPosition])
    }

    inner class HiddenItemsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(item: String) {

            val title = itemView.findViewById<TextView>(R.id.title)
            title.text = item
            title.isSelected = true

            val checkBox = itemView.findViewById<CheckBox>(R.id.checkbox)

            itemView.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
                if (!checkBox.isChecked) mHiddenItems.remove(item) else mHiddenItems.add(item)
            }
        }
    }
}

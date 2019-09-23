package com.iven.musicplayergo.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iven.musicplayergo.R
import com.iven.musicplayergo.musicPlayerGoExAppPreferences

class CheckableAdapter(
    private val context: Context,
    private val listItems: MutableList<String>,
    private val isHiddenItemsDialog: Boolean
) :
    RecyclerView.Adapter<CheckableAdapter.HiddenItemsHolder>() {

    private val mItemsToRemove = mutableListOf<String>()

    private val mCheckableItems = if (isHiddenItemsDialog) {
        listItems.sort()
        listItems
    } else {
        musicPlayerGoExAppPreferences.activeFragments!!.toMutableList()
    }

    fun getUpdatedItems(): Set<String> {
        mCheckableItems.removeAll(mItemsToRemove.toSet())
        return mCheckableItems.toSet()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiddenItemsHolder {
        return HiddenItemsHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recycler_view_checkable_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    override fun onBindViewHolder(holder: HiddenItemsHolder, position: Int) {
        holder.bindItems(listItems[holder.adapterPosition])
    }

    inner class HiddenItemsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(item: String) {

            val title = itemView.findViewById<TextView>(R.id.title)
            title.text = item
            title.isSelected = true

            val checkBox = itemView.findViewById<CheckBox>(R.id.checkbox)
            if (!isHiddenItemsDialog) {
                itemView.isEnabled = adapterPosition != listItems.size - 1
                checkBox.isEnabled = itemView.isEnabled
                checkBox.isChecked = mCheckableItems.contains(adapterPosition.toString())
            } else {
                checkBox.isChecked = listItems.contains(item)
            }

            itemView.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
                if (isHiddenItemsDialog) {
                    if (!checkBox.isChecked) {
                        mItemsToRemove.add(item)
                    } else {
                        if (mItemsToRemove.contains(item)) mItemsToRemove.remove(item)
                    }
                } else {
                    if (!checkBox.isChecked) mCheckableItems.remove(adapterPosition.toString()) else mCheckableItems.add(
                        adapterPosition.toString()
                    )
                    if (mCheckableItems.size < listItems.size - 2) {
                        Utils.makeToast(
                            context,
                            R.string.active_fragments_pref_warning,
                            R.drawable.ic_warning,
                            R.color.yellow
                        )
                        mCheckableItems.add(adapterPosition.toString())
                        checkBox.isChecked = true
                    }
                }
            }
        }
    }
}

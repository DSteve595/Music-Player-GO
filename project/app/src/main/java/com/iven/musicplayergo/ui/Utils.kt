package com.iven.musicplayergo.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.widget.SearchView
import com.afollestad.recyclical.datasource.DataSource
import com.iven.musicplayergo.R
import com.iven.musicplayergo.musicPlayerGoExAppPreferences
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import java.util.*

object Utils {

    @JvmStatic
    fun makeUnknownErrorToast(context: Context, message: Int) {
        val errorColor = ThemeHelper.getColor(context, R.color.red, R.color.red)
        val tintColor = if (ThemeHelper.isThemeNight()) Color.BLACK else Color.WHITE
        DynamicToast.make(
            context,
            context.getString(message),
            context.getDrawable(R.drawable.ic_error),
            tintColor,
            errorColor
        )
            .show()
    }

    @JvmStatic
    fun setupSearchViewForStringLists(
        searchView: SearchView,
        list: List<String>,
        dataSource: DataSource<Any>
    ) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override
            fun onQueryTextChange(newText: String): Boolean {
                processQueryForStringsLists(newText, list, dataSource)
                return false
            }

            override
            fun onQueryTextSubmit(query: String): Boolean {
                return false
            }
        })
    }

    @JvmStatic
    @SuppressLint("DefaultLocale")
    private fun processQueryForStringsLists(
        query: String,
        list: List<String>,
        dataSource: DataSource<Any>
    ) {
        // in real app you'd have it instantiated just once
        val results = mutableListOf<Any>()

        try {
            // case insensitive search
            list.iterator().forEach {
                if (it.toLowerCase().startsWith(query.toLowerCase())) {
                    results.add(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (results.size > 0) {
            dataSource.set(results)
        }
    }

    @JvmStatic
    fun getSortedList(
        id: Int,
        list: MutableList<String>,
        defaultList: MutableList<String>
    ): MutableList<String> {
        return when (id) {

            R.id.ascending_sorting -> {

                Collections.sort(list, String.CASE_INSENSITIVE_ORDER)
                list
            }

            R.id.descending_sorting -> {

                Collections.sort(list, String.CASE_INSENSITIVE_ORDER)
                list.asReversed()
            }

            else -> defaultList
        }
    }

    @JvmStatic
    fun addToHiddenItems(item: String) {
        val hiddenArtistsFolders = musicPlayerGoExAppPreferences.hiddenItems?.toMutableList()
        hiddenArtistsFolders?.add(item)
        musicPlayerGoExAppPreferences.hiddenItems = hiddenArtistsFolders?.toSet()
    }

    @JvmStatic
    fun removeHiddenItems(newItems: Set<String>) {
        musicPlayerGoExAppPreferences.hiddenItems = newItems
    }
}

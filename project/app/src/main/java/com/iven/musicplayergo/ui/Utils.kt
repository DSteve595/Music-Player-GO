package com.iven.musicplayergo.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.appcompat.widget.SearchView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.recyclical.datasource.DataSource
import com.google.android.material.snackbar.Snackbar
import com.iven.musicplayergo.R
import com.iven.musicplayergo.musicPlayerGoExAppPreferences
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import java.util.*

object Utils {

    @JvmStatic
    fun makeToast(context: Context, message: Int, icon: Int, color: Int) {
        val errorColor = ThemeHelper.getColor(context, color, color)
        val tintColor = if (ThemeHelper.isThemeNight()) Color.BLACK else Color.WHITE
        DynamicToast.make(
            context,
            context.getString(message),
            context.getDrawable(icon),
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
    fun removeFromHiddenItems(item: String) {
        val hiddenArtistsFolders = musicPlayerGoExAppPreferences.hiddenItems?.toMutableList()
        hiddenArtistsFolders?.remove(item)
        musicPlayerGoExAppPreferences.hiddenItems = hiddenArtistsFolders?.toSet()
    }

    @JvmStatic
    fun makeHideItemDialog(
        context: Context,
        item: Pair<Int, String>,
        stringsList: MutableList<String>,
        dataSource: DataSource<Any>,
        snackBarContextView: View
    ): MaterialDialog {

        return MaterialDialog(context).show {

            cornerRadius(res = R.dimen.md_radius)
            title(text = item.second)
            message(text = context.getString(R.string.hidden_items_pref_message, item.second))
            positiveButton {

                stringsList.remove(item.second)
                dataSource.set(stringsList)
                addToHiddenItems(item.second)

                Snackbar.make(
                    snackBarContextView,
                    context.getString(R.string.hidden_item_pref_result, item.second),
                    Snackbar.LENGTH_LONG
                ).setAction(R.string.hidden_items_pref_undo) {
                    stringsList.add(item.first, item.second)
                    dataSource.set(stringsList)
                    removeFromHiddenItems(item.second)
                }.show()
            }
            negativeButton {}
        }
    }

    @JvmStatic
    fun removeCheckableItems(newCheckableItems: Set<String>, isHiddenItemsDialog: Boolean) {
        if (isHiddenItemsDialog) musicPlayerGoExAppPreferences.hiddenItems = newCheckableItems
        else musicPlayerGoExAppPreferences.activeFragments = newCheckableItems
    }
}

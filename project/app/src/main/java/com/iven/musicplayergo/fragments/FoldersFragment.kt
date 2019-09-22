package com.iven.musicplayergo.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.datasource.dataSourceOf
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.swipe.SwipeLocation
import com.afollestad.recyclical.swipe.withSwipeAction
import com.afollestad.recyclical.withItem
import com.google.android.material.snackbar.Snackbar
import com.iven.musicplayergo.R
import com.iven.musicplayergo.musicLibrary
import com.iven.musicplayergo.musicPlayerGoExAppPreferences
import com.iven.musicplayergo.ui.GenericViewHolder
import com.iven.musicplayergo.ui.ThemeHelper
import com.iven.musicplayergo.ui.UIControlInterface
import com.iven.musicplayergo.ui.Utils
import kotlinx.android.synthetic.main.fragment_folders.*
import kotlinx.android.synthetic.main.search_toolbar.*
import java.io.File

/**
 * A simple [Fragment] subclass.
 * Use the [FoldersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FoldersFragment : Fragment() {

    private lateinit var mUIControlInterface: UIControlInterface

    private lateinit var mFolders: MutableList<String>
    private lateinit var mDataSource: DataSource<Any>
    private var mSelectedFolder = ""

    private lateinit var mSearchToolbar: Toolbar

    fun updateFolders() {
        setupFilteredFolders()
        mDataSource.set(mFolders)
    }

    private fun setupFilteredFolders() {

        mFolders = Utils.getSortedList(
            musicPlayerGoExAppPreferences.foldersSorting,
            musicLibrary.allCategorizedMusicByFolder.keys.toMutableList(),
            musicLibrary.allCategorizedMusicByFolder.keys.toMutableList()
        )
        musicPlayerGoExAppPreferences.hiddenItems?.iterator()?.forEach {
            if (mFolders.contains(it)) mFolders.remove(it)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mUIControlInterface = activity as UIControlInterface
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement MyInterface ")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_folders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (context != null) {

            mSearchToolbar = search_toolbar
            mSearchToolbar.inflateMenu(R.menu.menu_search)
            mSearchToolbar.title = getString(R.string.folders)
            val itemSearch = mSearchToolbar.menu.findItem(R.id.action_search)

            val isSearchBarEnabled = musicPlayerGoExAppPreferences.isSearchBarEnabled
            itemSearch.isVisible = isSearchBarEnabled

            setMenuOnItemClickListener()

            setupFilteredFolders()

            mDataSource = dataSourceOf(mFolders)

            // setup{} is an extension method on RecyclerView
            folders_rv.setup {
                withDataSource(mDataSource)
                withItem<String, GenericViewHolder>(R.layout.recycler_view_main_item) {
                    onBind(::GenericViewHolder) { _, item ->

                        // GenericViewHolder is `this` here
                        title.text = item

                        subtitle.text = getString(R.string.in_directory, getParentFolder(item))
                    }

                    onClick {
                        // item is a `val` in `this` here
                        if (::mUIControlInterface.isInitialized) {

                            if (mSelectedFolder != item) {
                                mUIControlInterface.onPopulateAndShowSongsSheet(
                                    true,
                                    item,
                                    getString(R.string.in_directory, getParentFolder(item)),
                                    musicLibrary.allCategorizedMusicByFolder.getValue(item).toMutableList()
                                )
                            } else {
                                mUIControlInterface.onShowSongsSheet()
                            }
                        }
                    }
                }

                withSwipeAction(SwipeLocation.RIGHT, SwipeLocation.LEFT) {
                    icon(R.drawable.ic_hide)
                    color(R.color.red)
                    text(
                        res = R.string.hidden_items_pref_hide,
                        typefaceRes = R.font.raleway_black,
                        color = if (ThemeHelper.isThemeNight()) android.R.color.black else android.R.color.white
                    )

                    callback { index, item ->

                        val hiddenItem = item.toString()

                        mFolders.remove(hiddenItem)
                        Utils.addToHiddenItems(hiddenItem)

                        Snackbar.make(
                            context_view,
                            getString(R.string.hidden_item_pref_result, hiddenItem),
                            Snackbar.LENGTH_LONG
                        ).setAction(R.string.hidden_items_pref_undo) {
                            mFolders.add(index, hiddenItem)
                            mDataSource.set(mFolders)
                            Utils.removeFromHiddenItems(hiddenItem)
                        }.show()

                        true
                    }
                }
            }

            if (isSearchBarEnabled) {
                val searchView = itemSearch.actionView as SearchView
                Utils.setupSearchViewForStringLists(searchView, mFolders, mDataSource)
            }
        }
    }

    //getting parent path of the first song
    private fun getParentFolder(item: String): String {
        val songRootPath =
            musicLibrary.allCategorizedMusicByFolder.getValue(item)[0].path
        return File(songRootPath!!).parentFile?.parent.toString()
    }

    private fun setMenuOnItemClickListener() {
        mSearchToolbar.setOnMenuItemClickListener {

            mFolders = Utils.getSortedList(
                it.itemId,
                mFolders,
                musicLibrary.allCategorizedMusicByFolder.keys.toMutableList()
            )

            mDataSource.set(mFolders)

            musicPlayerGoExAppPreferences.foldersSorting = it.itemId

            return@setOnMenuItemClickListener true
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment NowPlaying.
         */
        @JvmStatic
        fun newInstance() = FoldersFragment()
    }
}

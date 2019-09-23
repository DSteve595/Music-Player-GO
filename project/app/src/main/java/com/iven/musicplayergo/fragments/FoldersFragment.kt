package com.iven.musicplayergo.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.datasource.dataSourceOf
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import com.iven.musicplayergo.R
import com.iven.musicplayergo.musicLibrary
import com.iven.musicplayergo.musicPlayerGoExAppPreferences
import com.iven.musicplayergo.ui.GenericViewHolder
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

    private lateinit var mSearchToolbar: Toolbar
    private lateinit var mHideItemDialog: MaterialDialog

    private lateinit var mFolders: MutableList<String>
    private lateinit var mDataSource: DataSource<Any>
    private var mSelectedFolder = ""

    private lateinit var mUIControlInterface: UIControlInterface

    override fun onPause() {
        super.onPause()
        if (::mHideItemDialog.isInitialized && mHideItemDialog.isShowing) mHideItemDialog.dismiss()
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
                    onLongClick { index ->
                        mHideItemDialog = Utils.makeHideItemDialog(
                            activity!!,
                            Pair(index, item),
                            mFolders,
                            mDataSource,
                            context_view
                        )
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

    fun updateFoldersList() {
        if (::mDataSource.isInitialized) {
            setupFilteredFolders()
            mDataSource.set(mFolders)
        }
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

package com.iven.musicplayergo.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.datasource.dataSourceOf
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.swipe.SwipeLocation
import com.afollestad.recyclical.swipe.withSwipeAction
import com.afollestad.recyclical.withItem
import com.google.android.material.snackbar.Snackbar
import com.iven.musicplayergo.R
import com.iven.musicplayergo.music.MusicUtils
import com.iven.musicplayergo.musicLibrary
import com.iven.musicplayergo.musicPlayerGoExAppPreferences
import com.iven.musicplayergo.ui.GenericViewHolder
import com.iven.musicplayergo.ui.ThemeHelper
import com.iven.musicplayergo.ui.UIControlInterface
import com.iven.musicplayergo.ui.Utils
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import com.reddit.indicatorfastscroll.FastScrollerThumbView
import com.reddit.indicatorfastscroll.FastScrollerView
import kotlinx.android.synthetic.main.fragment_artists.*
import kotlinx.android.synthetic.main.search_toolbar.*

/**
 * A simple [Fragment] subclass.
 * Use the [ArtistsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ArtistsFragment : Fragment() {

    //views
    private lateinit var mArtistsRecyclerView: RecyclerView
    private lateinit var mSearchToolbar: Toolbar

    //indicator fast scroller by reddit
    private lateinit var mIndicatorFastScrollerView: FastScrollerView
    private lateinit var mIndicatorFastScrollThumb: FastScrollerThumbView

    private lateinit var mArtists: MutableList<String>
    private lateinit var mDataSource: DataSource<Any>
    private var mSelectedArtist = ""

    private lateinit var mUIControlInterface: UIControlInterface

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
        return inflater.inflate(R.layout.fragment_artists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (context != null) {
            mSearchToolbar = search_toolbar
            mSearchToolbar.inflateMenu(R.menu.menu_search)
            mSearchToolbar.title = getString(R.string.artists)

            val itemSearch = mSearchToolbar.menu.findItem(R.id.action_search)

            val isSearchBarEnabled = musicPlayerGoExAppPreferences.isSearchBarEnabled
            itemSearch.isVisible = isSearchBarEnabled

            setMenuOnItemClickListener()

            mArtistsRecyclerView = artists_rv

            setupFilteredArtists()

            mDataSource = dataSourceOf(mArtists)

            // setup{} is an extension method on RecyclerView
            mArtistsRecyclerView.setup {
                // item is a `val` in `this` here
                withDataSource(mDataSource)
                withItem<String, GenericViewHolder>(R.layout.recycler_view_main_item) {
                    onBind(::GenericViewHolder) { _, item ->
                        // GenericViewHolder is `this` here
                        title.text = item
                        subtitle.text = getArtistSubtitle(item)
                    }

                    onClick {
                        if (::mUIControlInterface.isInitialized) {

                            if (mSelectedArtist != item) {

                                mSelectedArtist = item

                                mUIControlInterface.onPopulateAndShowSongsSheet(
                                    false,
                                    item,
                                    getArtistSubtitle(item),
                                    MusicUtils.buildSortedArtistAlbums(
                                        resources,
                                        musicLibrary.allCategorizedMusic.getValue(item)
                                    )[0].music!!
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

                        mArtists.remove(hiddenItem)
                        Utils.addToHiddenItems(hiddenItem)

                        Snackbar.make(
                            context_view,
                            getString(R.string.hidden_item_pref_result, hiddenItem),
                            Snackbar.LENGTH_LONG
                        ).setAction(R.string.hidden_items_pref_undo) {
                            mArtists.add(index, hiddenItem)
                            mDataSource.set(mArtists)
                            Utils.removeFromHiddenItems(hiddenItem)
                        }.show()

                        true
                    }
                }
            }

            if (musicPlayerGoExAppPreferences.isFastScrollEnabled) {
                //indicator fast scroller view
                mIndicatorFastScrollerView = fastscroller
                mIndicatorFastScrollThumb = fastscroller_thumb

                setupIndicatorFastScrollerView()
            }

            if (isSearchBarEnabled) {
                val searchView = itemSearch.actionView as SearchView
                Utils.setupSearchViewForStringLists(searchView, mArtists, mDataSource)
            }
        }
    }

    private fun getArtistSubtitle(item: String): String {
        val albums = musicLibrary.allCategorizedMusic.getValue(item)

        return getString(
            R.string.artist_count,
            albums.keys.size,
            MusicUtils.getArtistSongsCount(albums)
        )
    }

    @SuppressLint("DefaultLocale")
    private fun setupIndicatorFastScrollerView() {

        //set indexes if artists rv is scrollable
        mArtistsRecyclerView.afterMeasured {
            if (mArtistsRecyclerView.computeVerticalScrollRange() > height) {

                mIndicatorFastScrollerView.setupWithRecyclerView(
                    mArtistsRecyclerView,
                    { position ->
                        val item = mArtists[position] // Get your model object
                        // or fetch the section at [position] from your database

                        FastScrollItemIndicator.Text(
                            item.substring(
                                0,
                                1
                            ).toUpperCase() // Grab the first letter and capitalize it
                        ) // Return a text indicator
                    }
                )

                mIndicatorFastScrollerView.afterMeasured {

                    //set margin for artists recycler to improve fast scroller visibility
                    mArtistsRecyclerView.setPadding(0, 0, width, 0)

                    //set margin for thumb view
                    val newLayoutParams =
                        mIndicatorFastScrollThumb.layoutParams as FrameLayout.LayoutParams
                    newLayoutParams.marginEnd = width
                    mIndicatorFastScrollThumb.layoutParams = newLayoutParams
                }
                mIndicatorFastScrollThumb.setupWithFastScroller(mIndicatorFastScrollerView)

                mIndicatorFastScrollerView.useDefaultScroller = false
                mIndicatorFastScrollerView.itemIndicatorSelectedCallbacks += object :
                    FastScrollerView.ItemIndicatorSelectedCallback {
                    override fun onItemIndicatorSelected(
                        indicator: FastScrollItemIndicator,
                        indicatorCenterY: Int,
                        itemPosition: Int
                    ) {
                        mArtistsRecyclerView.scrollToPosition(itemPosition)
                    }
                }

            } else {
                mIndicatorFastScrollerView.visibility = View.GONE
            }
        }
    }

    private fun setMenuOnItemClickListener() {
        mSearchToolbar.setOnMenuItemClickListener {

            mArtists = Utils.getSortedList(
                it.itemId,
                mArtists,
                musicLibrary.allCategorizedMusic.keys.toMutableList()
            )

            mDataSource.set(mArtists)

            musicPlayerGoExAppPreferences.artistsSorting = it.itemId

            return@setOnMenuItemClickListener true
        }
    }

    fun updateArtistsList() {
        if (::mDataSource.isInitialized) {
            setupFilteredArtists()
            mDataSource.set(mArtists)
        }
    }

    private fun setupFilteredArtists() {
        mArtists = Utils.getSortedList(
            musicPlayerGoExAppPreferences.artistsSorting,
            musicLibrary.allCategorizedMusic.keys.toMutableList(),
            musicLibrary.allCategorizedMusic.keys.toMutableList()
        )

        musicPlayerGoExAppPreferences.hiddenItems?.iterator()?.forEach {
            if (mArtists.contains(it)) mArtists.remove(it)
        }
    }

    //viewTreeObserver extension to measure layout params
    //https://antonioleiva.com/kotlin-ongloballayoutlistener/
    private inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    f()
                }
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment MusicFragment.
         */
        @JvmStatic
        fun newInstance() = ArtistsFragment()
    }
}

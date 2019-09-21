package com.iven.musicplayergo.ui

import com.iven.musicplayergo.music.Music

interface UIControlInterface {
    fun onPopulateAndShowSongsSheet(
        isFolder: Boolean,
        header: String,
        subheading: String,
        songs: MutableList<Music>
    )

    fun onShowSongsSheet()
    fun onSongSelected(song: Music)
    fun onVisibleItemsUpdated()
}

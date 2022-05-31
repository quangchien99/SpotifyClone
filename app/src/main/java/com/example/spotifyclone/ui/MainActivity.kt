/*
 * Copyright (c) 2022 All Rights Reserved, Quang Chien Pham.
 */
package com.example.spotifyclone.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.example.spotifyclone.adapter.SwipeSongAdapter
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.exoplayer.toSong
import com.example.spotifyclone.logger.Logger
import com.example.spotifyclone.ui.viewmodels.MainViewModel
import com.example.spotifyclone.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private var currentPlayingSong: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        subscribeToObserver()
        vpSong.adapter = swipeSongAdapter
    }

    private fun switchViewPagerToCurrentSong(song: Song) {
        Logger.d("switchViewPagerToCurrentSong: $song")
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if (newItemIndex != 1) {
            vpSong.currentItem = newItemIndex
            currentPlayingSong = song
        }
    }

    private fun subscribeToObserver() {
        Logger.d("subscribeToObserver")
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                Logger.d("subscribeToObserver: ${result.status}")
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            swipeSongAdapter.songs = songs
                            if (songs.isNotEmpty()) {
                                glide.load((currentPlayingSong ?: songs[0].imageUrl))
                                    .into(ivCurSongImage)
                            }
                            switchViewPagerToCurrentSong(currentPlayingSong ?: return@observe)
                        }
                    }
                    Status.ERROR -> {
                        Unit
                    }
                    Status.LOADING -> {
                        Unit
                    }
                }
            }
        }
        mainViewModel.currentPlayingSong.observe(this) {
            if (it == null) return@observe
            currentPlayingSong = it.toSong()
            glide.load(currentPlayingSong?.imageUrl).into(ivCurSongImage)
            switchViewPagerToCurrentSong(currentPlayingSong ?: return@observe)
        }
    }

}
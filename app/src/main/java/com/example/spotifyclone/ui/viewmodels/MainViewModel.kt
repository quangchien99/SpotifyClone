/*
 * Copyright (c) 2022 All Rights Reserved, Quang Chien Pham.
 */
package com.example.spotifyclone.ui.viewmodels

import android.media.MediaMetadata.METADATA_KEY_MEDIA_ID
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.exoplayer.SongServiceConnection
import com.example.spotifyclone.exoplayer.isPlayEnabled
import com.example.spotifyclone.exoplayer.isPlaying
import com.example.spotifyclone.exoplayer.isPrepare
import com.example.spotifyclone.logger.Logger
import com.example.spotifyclone.utils.Constants.MEDIA_ROOT_ID
import com.example.spotifyclone.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val songServiceConnection: SongServiceConnection
) : ViewModel() {
    private val _mediaItem = MutableLiveData<Resource<List<Song>>>()
    val mediaItem: LiveData<Resource<List<Song>>> = _mediaItem

    val isConnected = songServiceConnection.isConnected
    val networkError = songServiceConnection.networkError
    val currentPlayingSong = songServiceConnection.currentPlayingSong
    val playbackState = songServiceConnection.playbackState

    init {
        Logger.d("Init MainViewModel")
        _mediaItem.postValue(Resource.loading(null))
        songServiceConnection.subscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {

                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    super.onChildrenLoaded(parentId, children)
                    val items = children.map {
                        Song(
                            songId = it.mediaId?.toLongOrNull() ?: 0L,
                            title = it.description.title.toString(),
                            author = it.description.subtitle.toString(),
                            songUrl = it.description.mediaUri.toString(),
                            imageUrl = it.description.iconUri.toString()
                        )
                    }
                    _mediaItem.postValue(Resource.success(items))
                }

            })
    }

    fun skipToNextSong() {
        Logger.d("skipToNextSong")
        songServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong() {
        Logger.d("skipToPreviousSong")
        songServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(position: Long) {
        Logger.d("seekTo position: $position")
        songServiceConnection.transportControls.seekTo(position)
    }

    fun playOrToggleSong(song: Song, toggle: Boolean = false) {
        Logger.d("playOrToggleSong: song: $song - toggle: $toggle")
        val isPrepare = playbackState.value?.isPrepare ?: false
        if (isPrepare && song.songId == currentPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)
                ?.toLongOrNull()
        ) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) songServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> songServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            songServiceConnection.transportControls.playFromMediaId(song.songId.toString(), null)
        }
    }

    // No need to pass callback here -> add an empty one
    override fun onCleared() {
        Logger.d("onCleared")
        super.onCleared()
        songServiceConnection.unSubscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }
}

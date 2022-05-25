/*
 * Copyright (c) 2022 All Rights Reserved, Quang Chien Pham.
 */
package com.example.spotifyclone.exoplayer

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.spotifyclone.logger.Logger
import com.example.spotifyclone.utils.Constants.NETWORK_ERROR
import com.example.spotifyclone.utils.Event
import com.example.spotifyclone.utils.Resource

class SongServiceConnection(
    context: Context
) {
    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected: LiveData<Event<Resource<Boolean>>> = _isConnected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError: LiveData<Event<Resource<Boolean>>> = _networkError

    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState: LiveData<PlaybackStateCompat?> = _playbackState

    private val _currentPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val currentPlayingSong: LiveData<MediaMetadataCompat?> = _currentPlayingSong

    lateinit var mediaController: MediaControllerCompat

    //To pause, next, resume,... player
    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(
            context,
            SongService::class.java
        ),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        Logger.d("mediaBrowser - connect")
        connect()
    }

    //call from view model to start the subscription to specific media id and to get access to our media item from fire base
    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        Logger.d("mediaBrowser - subscribe - $parentId")
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unSubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        Logger.d("mediaBrowser - unsubscribe - $parentId")
        mediaBrowser.unsubscribe(parentId, callback)
    }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            Logger.d("MediaBrowserConnectionCallback connected")
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }

            _isConnected.postValue(Event(Resource.success(true)))
        }

        override fun onConnectionSuspended() {
            Logger.d("MediaBrowserConnectionCallback Connection Suspended")
            _isConnected.postValue(
                Event(
                    Resource.error(
                        "The connection is suspended",
                        false
                    )
                )
            )
        }

        override fun onConnectionFailed() {
            Logger.d("MediaBrowserConnectionCallback Connection Failed")
            _isConnected.postValue(
                Event(
                    Resource.error(
                        "Couldn't connect to media browser",
                        false
                    )
                )
            )
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            Logger.d("MediaControllerCallback onPlaybackStateChanged: $state")
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Logger.d("MediaControllerCallback onMetadataChanged: ${metadata?.description}")
            _currentPlayingSong.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            Logger.d("MediaControllerCallback onSessionEvent: $event")
            super.onSessionEvent(event, extras)
            when (event) {
                NETWORK_ERROR -> _networkError.postValue(
                    Event(
                        Resource.error(
                            "Could not connect to server. Please check your internet connection.",
                            null
                        )
                    )
                )
            }
        }

        override fun onSessionDestroyed() {
            Logger.d("MediaControllerCallback onSessionDestroyed")
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}
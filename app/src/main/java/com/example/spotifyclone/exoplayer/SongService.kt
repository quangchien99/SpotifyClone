/*
 * Copyright (c) 2022 All Rights Reserved, Quang Chien Pham.
 */
package com.example.spotifyclone.exoplayer

import android.app.PendingIntent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.spotifyclone.exoplayer.callbacks.SongPlaybackPreparer
import com.example.spotifyclone.exoplayer.callbacks.SongPlayerEventListener
import com.example.spotifyclone.exoplayer.callbacks.SongPlayerNotificationListener
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.DefaultDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import javax.inject.Inject

private const val SERVICE_TAG = "SongService"

@AndroidEntryPoint
class SongService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSource.Factory

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var fireBaseSongSource: FireBaseSongSource

    private val serviceJob = Job()

    /*
        serviceScope -> deal with the cancellation of the coroutine
        When the service die -> the coroutine will be canceled, avoid memory leak

        Dispatchers.Main + serviceJob ->
     */
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var songNotificationManager: SongNotificationManager

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForeGroundService = false

    private var currentPlayingSong: MediaMetadataCompat? = null

    override fun onCreate() {
        super.onCreate()

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }
        sessionToken = mediaSession.sessionToken

        songNotificationManager = SongNotificationManager(
            this,
            mediaSession.sessionToken,
            SongPlayerNotificationListener(this)
        ) {
            //update later
        }

        val songPlaybackPreparer = SongPlaybackPreparer(fireBaseSongSource) {
            currentPlayingSong = it
            preparePlayer(
                fireBaseSongSource.songs,
                it,
                true
            )
        }



        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(songPlaybackPreparer)
        mediaSessionConnector.setPlayer(exoPlayer)

        exoPlayer.addListener(SongPlayerEventListener(this))

        songNotificationManager.showNotification(exoPlayer)
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        songToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        val currentSongIndex = if (currentPlayingSong == null) {
            0
        } else {
            songs.indexOf(songToPlay)
        }

        exoPlayer.setMediaSource(fireBaseSongSource.convertToMediaSource(dataSourceFactory))
        exoPlayer.prepare()
        exoPlayer.seekTo(currentSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        TODO("Not yet implemented")
    }


    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        TODO("Not yet implemented")
    }
}

/*
 * Copyright (c) 2022 All Rights Reserved, Quang Chien Pham.
 */
package com.example.spotifyclone.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.spotifyclone.exoplayer.callbacks.SongPlaybackPreparer
import com.example.spotifyclone.exoplayer.callbacks.SongPlayerEventListener
import com.example.spotifyclone.exoplayer.callbacks.SongPlayerNotificationListener
import com.example.spotifyclone.logger.Logger
import com.example.spotifyclone.utils.Constants.MEDIA_ROOT_ID
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
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

    private var isPlayerInitialized: Boolean = false

    private lateinit var songPlayerEventListener: SongPlayerEventListener

    companion object {
        var currentSongDuration = 0L
            private set
    }

    override fun onCreate() {
        super.onCreate()

        serviceScope.launch {
            Logger.d("Fetching data ...")
            fireBaseSongSource.fetchMediaData()
        }

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
            currentSongDuration = exoPlayer.duration
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
        mediaSessionConnector.setQueueNavigator(SongQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        songPlayerEventListener = SongPlayerEventListener(this)
        exoPlayer.addListener(songPlayerEventListener)

        songNotificationManager.showNotification(exoPlayer)
    }

    private inner class SongQueueNavigator : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return fireBaseSongSource.songs[windowIndex].description
        }

    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        songToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        Logger.d("Prepare player")
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

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d("Release resource")
        serviceScope.cancel()
        exoPlayer.removeListener(songPlayerEventListener)
        exoPlayer.release()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }


    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            MEDIA_ROOT_ID -> {
                val resultsSent = fireBaseSongSource.isReady { isInitialized ->
                    if (isInitialized) {
                        result.sendResult(fireBaseSongSource.convertToMediaItem())
                        if (!isPlayerInitialized && fireBaseSongSource.songs.isNotEmpty()) {
                            preparePlayer(
                                fireBaseSongSource.songs,
                                fireBaseSongSource.songs[0],
                                false
                            )
                            isPlayerInitialized = true
                        } else {
                            result.sendResult(null)
                        }
                    }
                }
                if (!resultsSent) {
                    result.detach()
                }
            }
        }
    }
}

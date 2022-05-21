/*
 * Copyright (c) 2022 All Rights Reserved, Quang Chien Pham.
 */
package com.example.spotifyclone.exoplayer.callbacks

import android.widget.Toast
import com.example.spotifyclone.exoplayer.SongService
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player

class SongPlayerEventListener(
    private val songService: SongService
) : Player.Listener{

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == Player.STATE_READY) {
            songService.stopForeground(false)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(songService, "An unknown error happened", Toast.LENGTH_LONG).show()
    }

}

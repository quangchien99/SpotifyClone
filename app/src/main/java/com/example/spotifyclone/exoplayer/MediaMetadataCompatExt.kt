/*
 * Copyright (c) 2022 All Rights Reserved, Quang Chien Pham.
 */
package com.example.spotifyclone.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.example.spotifyclone.data.entities.Song

fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Song(
            songId = it.mediaId ?: "",
            title = it.title.toString(),
            author = it.subtitle.toString(),
            songUrl = it.mediaUri.toString(),
            imageUrl = it.iconUri.toString()
        )
    }
}
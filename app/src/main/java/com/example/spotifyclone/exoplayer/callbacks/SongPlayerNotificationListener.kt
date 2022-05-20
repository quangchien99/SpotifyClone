/*
 * Copyright (c) 2022 All Rights Reserved, Quang Chien Pham.
 */
package com.example.spotifyclone.exoplayer.callbacks

import android.app.Notification
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.spotifyclone.exoplayer.SongService
import com.example.spotifyclone.utils.Constants.NOTIFICATION_ID
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class SongPlayerNotificationListener(
    private val songService: SongService
) : PlayerNotificationManager.NotificationListener {

    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)
        songService.apply {
            stopForeground(true)
            isForeGroundService = false
            stopSelf()
        }
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)
        songService.apply {
            if (ongoing && !isForeGroundService) {
                ContextCompat.startForegroundService(
                    this,
                    Intent(applicationContext, this::class.java)
                )
                startForeground(NOTIFICATION_ID, notification)
                isForeGroundService = true
            }
        }
    }
}
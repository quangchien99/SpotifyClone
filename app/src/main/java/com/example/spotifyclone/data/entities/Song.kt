/*
 * Copyright (c) 2022 All Rights Reserved, Quang Chien Pham.
 */
package com.example.spotifyclone.data.entities

data class Song(
    val songId: Long = 0,
    val title: String = "",
    val author: String = "",
    val songUrl: String = "",
    val imageUrl: String = ""
)

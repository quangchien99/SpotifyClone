/*
 * Copyright (c) 2022 All Rights Reserved, Quang Chien Pham.
 */
package com.example.spotifyclone.adapter

import androidx.recyclerview.widget.AsyncListDiffer
import com.example.spotifyclone.R
import com.example.spotifyclone.data.entities.Song
import kotlinx.android.synthetic.main.item_song.view.*
import kotlinx.android.synthetic.main.item_swipe_song.view.*
import javax.inject.Inject

class SwipeSongAdapter @Inject constructor() : BaseSongAdapter(
    R.layout.item_swipe_song
) {

    override val differ: AsyncListDiffer<Song> = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {
            val text = "${song.title} - ${song.author}"
            tvPrimary.text = text
            tvPrimary.isSelected = true
            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }
}

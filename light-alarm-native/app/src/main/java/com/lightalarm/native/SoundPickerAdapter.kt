package com.lightalarm.nativeapp

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SoundPickerAdapter(
    private val context: Context,
    private val sounds: List<SoundItem>,
    private val onSoundSelected: (SoundItem) -> Unit
) : RecyclerView.Adapter<SoundPickerAdapter.SoundViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var playingPosition: Int = -1

    inner class SoundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val soundName: TextView = itemView.findViewById(R.id.soundName)
        val playPauseButton: ImageButton = itemView.findViewById(R.id.playPauseButton)
        val selectButton: TextView = itemView.findViewById(R.id.selectButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_sound, parent, false)
        return SoundViewHolder(view)
    }

    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        val sound = sounds[position]
        
        // Set sound name
        holder.soundName.text = sound.displayName
        
        // Set play/pause icon based on current playback state
        if (position == playingPosition) {
            holder.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
        } else {
            holder.playPauseButton.setImageResource(android.R.drawable.ic_media_play)
        }
        
        // Play/Pause button click listener
        holder.playPauseButton.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener
            
            if (currentPosition == playingPosition) {
                // User clicked pause on currently playing sound
                stopPlayback()
                notifyItemChanged(currentPosition)
            } else {
                // User clicked play on a new sound
                val oldPlayingPosition = playingPosition
                
                // Stop any existing playback
                stopPlayback()
                
                // Start new playback
                try {
                    mediaPlayer = MediaPlayer.create(context, sound.resourceId)
                    mediaPlayer?.setOnCompletionListener {
                        // Sound finished playing naturally
                        val wasPlayingPosition = playingPosition
                        playingPosition = -1
                        if (wasPlayingPosition != -1) {
                            notifyItemChanged(wasPlayingPosition)
                        }
                    }
                    mediaPlayer?.start()
                    playingPosition = currentPosition
                    
                    // Update UI for both old and new positions
                    if (oldPlayingPosition != -1) {
                        notifyItemChanged(oldPlayingPosition)
                    }
                    notifyItemChanged(currentPosition)
                    
                    Log.d("SoundPickerAdapter", "Playing sound: ${sound.displayName}")
                } catch (e: Exception) {
                    Log.e("SoundPickerAdapter", "Failed to play sound: ${sound.displayName}", e)
                    playingPosition = -1
                }
            }
        }
        
        // Select button click listener
        holder.selectButton.setOnClickListener {
            stopPlayback() // Stop any playing sound when selecting
            onSoundSelected(sound)
        }
    }

    override fun getItemCount(): Int = sounds.size

    private fun stopPlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        playingPosition = -1
    }

    // Public function to release MediaPlayer (call from Activity onStop/onDestroy)
    fun releasePlayer() {
        val wasPlaying = playingPosition
        stopPlayback()
        if (wasPlaying != -1) {
            notifyItemChanged(wasPlaying)
        }
    }
}






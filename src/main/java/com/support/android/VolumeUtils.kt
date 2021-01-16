package com.support.android

import android.content.Context
import android.media.AudioManager

fun getCurrentVolume(context: Context): Int {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val volumeLevel: Int = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    val maxVolumeLevel: Int = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    return (volumeLevel.toFloat() / maxVolumeLevel * 100).toInt()
}
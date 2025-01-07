package com.pon.smsprocessor

import android.media.MediaPlayer
import com.pon.smsprocessor.App.Companion.appContext

object SoundEffectsPlayer {

    private val mediaPlayer: MediaPlayer? = MediaPlayer.create(appContext, R.raw.beep2)

    fun playSound() {
        if (mediaPlayer?.isPlaying !=null) mediaPlayer.stop()
        mediaPlayer?.start()

    }
}

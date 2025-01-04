package com.pon.smsprocessor

import android.media.MediaPlayer
import com.pon.smsprocessor.App.Companion.appContext

object SoundEffectsPlayer {
    fun playSound() {
        val mediaPlayer = MediaPlayer.create(appContext, R.raw.beep2)
            ?: return
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener { player: MediaPlayer ->
            player.reset()
            player.release()
        }
    }
}

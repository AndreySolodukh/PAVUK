package com.game.pavuk

import com.badlogic.gdx.Gdx

class Music {

    var isAllowed = true
    var isOn = false
    private val theme = Gdx.audio.newMusic(Gdx.files.internal("android/assets/SolverTheme.mp3"))

    fun switch() {
        if (isAllowed) {
            if (!isOn) {
                isOn = true
                theme.play()
            } else {
                isOn = false
                theme.stop()
                theme.dispose()
            }
        }
    }

}
package com.game.pavuk

import com.badlogic.gdx.Game
import com.game.pavuk.screens.MainMenu

class Pavuk : Game() {

    var allowMusic = true

    override fun create() {
        setScreen(MainMenu(this))
    }
}
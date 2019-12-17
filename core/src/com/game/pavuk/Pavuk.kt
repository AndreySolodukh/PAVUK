package com.game.pavuk

import com.badlogic.gdx.Game
import com.game.pavuk.screens.MainMenu

class Pavuk : Game() {

    override fun create() {
        setScreen(MainMenu(this))
    }
}
package com.game.pavuk

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.game.pavuk.screens.MainMenu

class Pavuk : Game() {

    var start = true
    var victory = false
    var defeat = false

    var suitnumber = 1

    override fun create() {
        setScreen(MainMenu(this, Music()))
    }
}
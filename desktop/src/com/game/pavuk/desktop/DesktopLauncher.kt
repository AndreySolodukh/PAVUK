package com.game.pavuk.desktop

import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.game.pavuk.Pavuk

fun main(args: Array<String>) {

    val config = LwjglApplicationConfiguration()
    //config.addIcon("icon32.ico", Files.FileType.Internal)
    config.title = "PAVUK"
    config.width = 1024
    config.height = 768
    config.vSyncEnabled = false
    config.foregroundFPS = 60
    config.x = 250
    config.y = 20
    LwjglApplication(Pavuk(), config)
}
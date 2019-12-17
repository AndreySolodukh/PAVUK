package com.game.pavuk.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.game.pavuk.*
import com.game.pavuk.objects.TextGameButton

class MainMenu(val game: Pavuk) : Screen {

    private val res = Resource()
    private val camera = OrthographicCamera(2000f, 1000f)

    private val play = TextGameButton("PLAY", "button", "pressedbutton",
            0.4f * res.width, 0.4f * res.height, 0.2f * res.width, 0.1f * res.width)
    private val exit = TextGameButton("exit", "button", "pressedbutton",
            0.4f * res.width, 0.2f * res.height, 0.2f * res.width, 0.1f * res.width)

    private val stage = Stage()
    private val input = InputMultiplexer()


    init {
        res.background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        camera.position.set(Vector3(1000f, 500f, 0f))
        stage.addActor(exit.button)
        stage.addActor(play.button)
        input.addProcessor(stage)
        Gdx.input.inputProcessor = input
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        res.batch.projectionMatrix = camera.combined

        res.batch.begin()
        res.batch.draw(res.background, 0f, 0f, 2000f, 1000f)
        res.batch.end()
        stage.draw()

        if (play.button.isChecked) {
            game.screen = PlayScreen(game)
            dispose()
        }
        if (exit.button.isChecked) {
            Gdx.app.exit()
            dispose()
        }
    }

    override fun dispose() {
        game.dispose()
        res.dispose()
        play.dispose()
        exit.dispose()
        stage.dispose()
    }

    override fun hide() {}
    override fun show() {}
    override fun pause() {}
    override fun resume() {}
    override fun resize(width: Int, height: Int) {}
}
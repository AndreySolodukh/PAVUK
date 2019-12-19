package com.game.pavuk.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.game.pavuk.*
import com.game.pavuk.objects.TextGameButton

class MainMenu(val game: Pavuk) : Screen {

    private val res = Resource()
    private val camera = OrthographicCamera(res.width, res.height)

    private val play = TextGameButton("PLAY", "button", "pressedbutton",
            0.4f * res.width, 0.4f * res.height, 0.2f * res.width, 0.1f * res.width)
    private val exit = TextGameButton("exit", "button", "pressedbutton",
            0.4f * res.width, 0.15f * res.height, 0.2f * res.width, 0.1f * res.width)

    private val stage = Stage()
    private val input = InputMultiplexer()
    private val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
    private val generator = FreeTypeFontGenerator(Gdx.files.internal("pixel.ttf"))

    init {
        parameter.size = (res.height / 10).toInt()
        parameter.color = Color.GREEN
        res.background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        camera.position.set(Vector3(res.width / 2, res.height / 2, 0f))
        stage.addActor(exit.button)
        stage.addActor(play.button)
        input.addProcessor(stage)
        Gdx.input.inputProcessor = input
    }

    private val font = generator.generateFont(parameter)

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        res.batch.projectionMatrix = camera.combined

        res.batch.begin()
        res.batch.draw(res.background, 0f, 0f, res.width, res.height)
        font.draw(res.batch, "PAVUK SUPER COOL CARD GAME", res.width / 2, res.height * 0.85f,
                0f, 1, false)
        font.draw(res.batch, "WITH RESCHATEL'", res.width / 2, res.height * 0.75f,
                0f, 1, false)

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
package com.game.pavuk.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.game.pavuk.*
import com.game.pavuk.objects.TextGameButton

class MainMenu(val game: Pavuk, private val music: Music) : Screen {

    private val screenWidth = Gdx.graphics.width.toFloat()
    private val screenHeight = Gdx.graphics.height.toFloat()

    private val background = Texture("android/assets/background.png")

    private val camera = OrthographicCamera(screenWidth, screenHeight)

    private val batch = SpriteBatch()

    private val play = TextGameButton("PLAY", "button", "pressedbutton",
            0.4f * screenWidth, 0.4f * screenHeight, 0.2f * screenWidth, 0.08f * screenWidth)
    private val exit = TextGameButton("exit", "button", "pressedbutton",
            0.4f * screenWidth, 0.15f * screenHeight, 0.2f * screenWidth, 0.08f * screenWidth)
    private val musicButton = TextGameButton("music", "pressedbutton", "button",
            0.03f * screenWidth, 0.03f * screenHeight, 0.12f * screenWidth, 0.06f * screenWidth)
    private val statistics = TextGameButton("stats", "button", "pressedbutton",
            0.85f * screenWidth, 0.03f * screenHeight, 0.12f * screenWidth, 0.06f * screenWidth)
    private val more = TextGameButton("->", "button", "pressedbutton",
            0.57f * screenWidth, 0.29f * screenHeight, 0.08f * screenWidth, 0.06f * screenWidth)
    private val less = TextGameButton("<-", "button", "pressedbutton",
            0.35f * screenWidth, 0.29f * screenHeight, 0.08f * screenWidth, 0.06f * screenWidth)

    private val stage = Stage()
    private val input = InputMultiplexer()
    private val titleParameter = FreeTypeFontGenerator.FreeTypeFontParameter()
    private val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
    private val generator = FreeTypeFontGenerator(Gdx.files.internal("android/assets/pixel.ttf"))

    init {
        titleParameter.size = (screenHeight / 10).toInt()
        titleParameter.color = Color.GREEN
        parameter.size = (screenHeight / 20).toInt()
        parameter.color = Color.WHITE
        parameter.borderColor = Color.BLACK
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        camera.position.set(Vector3(screenWidth / 2, screenHeight / 2, 0f))
        stage.addActor(exit.button)
        stage.addActor(play.button)
        stage.addActor(musicButton.button)
        stage.addActor(statistics.button)
        stage.addActor(less.button)
        stage.addActor(more.button)
        input.addProcessor(stage)
        Gdx.input.inputProcessor = input
    }

    private val titleFont = generator.generateFont(titleParameter)
    private val font = generator.generateFont(parameter)

    private var delay = 0f

    override fun render(delta: Float) {

        if (delay > 0f) delay -= delta

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        batch.projectionMatrix = camera.combined

        batch.begin()
        batch.draw(background, 0f, 0f, screenWidth, screenHeight)
        titleFont.draw(batch, "PAVUK SUPER COOL CARD GAME", screenWidth / 2, screenHeight * 0.85f,
                0f, 1, false)
        titleFont.draw(batch, "WITH RESCHATEL'", screenWidth / 2, screenHeight * 0.75f,
                0f, 1, false)
        font.draw(batch, "SUITS: ${game.suitnumber}", screenWidth / 2, screenHeight * 0.34f,
                0f, 1, false)

        batch.end()
        stage.draw()

        if (more.button.isChecked && delay <= 0f) {
            when {
                game.suitnumber == 1 -> game.suitnumber = 2
                game.suitnumber == 2 -> game.suitnumber = 4
                else -> {}
            }
            delay = 0.5f
            more.button.toggle()
        }

        if (less.button.isChecked && delay <= 0f) {
            when {
                game.suitnumber == 4 -> game.suitnumber = 2
                game.suitnumber == 2 -> game.suitnumber = 1
                else -> {}
            }
            delay = 0.5f
            less.button.toggle()
        }

        if (play.button.isChecked) {
            game.screen = PlayScreen(game, music)
            dispose()
        }

        if (statistics.button.isChecked) {
            game.screen = Statistics(game, music)
            dispose()
        }

        music.isAllowed = musicButton.button.isChecked

        if (exit.button.isChecked) {
            Gdx.app.exit()
            dispose()
        }
    }

    override fun dispose() {
        generator.dispose()
        titleFont.dispose()
        game.dispose()
        batch.dispose()
        play.dispose()
        exit.dispose()
        musicButton.dispose()
        less.dispose()
        more.dispose()
        statistics.dispose()
        stage.dispose()
        background.dispose()
    }

    override fun hide() {}
    override fun show() {}
    override fun pause() {}
    override fun resume() {}
    override fun resize(width: Int, height: Int) {}
}
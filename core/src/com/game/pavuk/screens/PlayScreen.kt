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

class PlayScreen(val game: Pavuk, val music: Music) : Screen {

    private val screenWidth = Gdx.graphics.width.toFloat()
    private val screenHeight = Gdx.graphics.height.toFloat()

    private val background = Texture("android/assets/background.png")

    private val camera = OrthographicCamera(screenWidth, screenHeight)

    private val batch = SpriteBatch()

    private val menu = TextGameButton("menu", "button", "pressedbutton", 0.72f * screenWidth,
            0.02f * screenHeight, 0.08f * screenWidth, 0.04f * screenWidth)
    private val new = TextGameButton("new", "button", "pressedbutton", 0.2f * screenWidth,
            0.02f * screenHeight, 0.08f * screenWidth, 0.04f * screenWidth)
    private val hint = TextGameButton("hint", "button", "pressedbutton", 0.46f * screenWidth,
            0.02f * screenHeight, 0.08f * screenWidth, 0.04f * screenWidth)
    private val auto = TextGameButton("auto", "button", "pressedbutton", 0.46f * screenWidth,
            0.12f * screenHeight, 0.08f * screenWidth, 0.04f * screenWidth)

    private val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
    private val generator = FreeTypeFontGenerator(Gdx.files.internal("android/assets/pixel.ttf"))

    private val stage = Stage()
    private val input = InputMultiplexer()


    private val deck = Deck(game)

    init {

        game.start = true
        game.defeat = false
        game.victory = false

        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        camera.position.set(Vector3(screenWidth / 2, screenHeight / 2, 0f))
        stage.addActor(menu.button)
        stage.addActor(new.button)
        stage.addActor(hint.button)
        stage.addActor(auto.button)
        input.addProcessor(stage)
        Gdx.input.inputProcessor = input
        parameter.color = Color.WHITE
        parameter.borderColor = Color.BLACK
        parameter.size = (screenHeight * 0.04f).toInt()
        parameter.spaceX = (screenHeight * 0.0065f).toInt()

        deck.buildDeck()
    }

    private val font = generator.generateFont(parameter)

    private var delay = 0f
    private var countdown = 4f

    override fun render(delta: Float) {

        if (delay > 0f) delay -= delta

        val cycle = Dynamics(deck)

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        batch.projectionMatrix = camera.combined

        batch.begin()

        batch.draw(background, 0f, 0f, screenWidth, screenHeight)

        font.draw(batch, "In - ${deck.backup}", 0.068f * screenWidth,
                0.05f * screenHeight, 0f, 1, false)
        if (deck.backup == 0 && !cycle.isOver())
            font.draw(batch, "Press 'new' to give up", 0.24f * screenWidth,
                    0.18f * screenHeight, 0f, 1, false)
        font.draw(batch, "Out - ${deck.finished}", 0.93f * screenWidth,
                0.05f * screenHeight, 0f, 1, false)

        if (cycle.isOver()) {
            if (game.victory) {
                font.draw(batch, "You won!", screenWidth / 2,
                        0.44f * screenHeight, 0f, 1, false)
                font.draw(batch, "press 'menu' to exit", screenWidth / 2,
                        0.36f * screenHeight, 0f, 1, false)
            }
            if (game.defeat) {
                font.draw(batch, "Game over", screenWidth / 2,
                        0.44f * screenHeight, 0f, 1, false)
                font.draw(batch, "press 'menu' to exit", screenWidth / 2,
                        0.36f * screenHeight, 0f, 1, false)
            }
        } else cycle.move()


        for (i in 0..Logic(deck).lastLine()) {
            for (card in deck.deck.filter { it.indicator !in deck.moving && it.line == i }) {
                card.updateCoords()
                card.draw(batch)
            }
        }

        if (!cycle.isOver()) {
            if (deck.moving.isEmpty() && deck.from != -1 && deck.to != -1) {
                font.draw(batch, "From", 0.072f * screenWidth + 0.098f * screenWidth * deck.from, 0.36f * screenHeight, 0f, 1, false)
                font.draw(batch, "To", 0.072f * screenWidth + 0.098f * screenWidth * deck.to, 0.36f * screenHeight, 0f, 1, false)
            }
            if (deck.moving.isNotEmpty()) {
                deck.from = -1
                deck.to = -1
            }
        }

        for (card in deck.deck.filter { it.indicator in deck.moving }.sortedBy { it.line }) {
            card.updateMoving(deck.moving)
            card.draw(batch)
        }

        if (music.isOn && countdown.toInt() > 0) {
            font.draw(batch, "Solving starts in", screenWidth / 2,
                    0.44f * screenHeight, 0f, 1, false)
            font.draw(batch, "${countdown.toInt()}", screenWidth / 2,
                    0.36f * screenHeight, 0f, 1, false)
            countdown -= delta
        }

        batch.end()

        stage.draw()

        if (menu.button.isChecked) {
            menu.button.toggle()
            game.screen = MainMenu(game, music)
            dispose()
        }

        if (auto.button.isChecked && delay <= 0) {
            if (!cycle.isOver()) {
                delay = 0.075f
                deck.from = -1
                deck.to = -1
                if (!music.isOn) music.switch()
                if (countdown.toInt() == 0 || !music.isAllowed) Solver(deck).step()
            } else {
                if (music.isOn) music.switch()
                auto.button.toggle()
                countdown = 4f
            }
        }

        if (hint.button.isChecked && delay <= 0f)
            if (!cycle.isOver()) {
                Solver(deck).step()
                delay = 0.5f
                hint.button.toggle()
            } else hint.button.toggle()

        if (new.button.isChecked && delay <= 0f)
            if (!cycle.isOver()) {
                cycle.new()
                delay = 0.5f
                new.button.toggle()
            } else new.button.toggle()
    }

    override fun dispose() {
        game.dispose()
        generator.dispose()
        new.dispose()
        hint.dispose()
        auto.dispose()
        menu.dispose()
        stage.dispose()
        font.dispose()
        background.dispose()
        batch.dispose()
    }

    override fun show() {}
    override fun pause() {}
    override fun resume() {}
    override fun resize(width: Int, height: Int) {}
    override fun hide() {}
}
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

class Statistics(val game: Pavuk, private val music: Music) : Screen {

    private val screenWidth = Gdx.graphics.width.toFloat()
    private val screenHeight = Gdx.graphics.height.toFloat()

    private var games = 1

    private val background = Texture("android/assets/background.png")

    private val camera = OrthographicCamera(screenWidth, screenHeight)

    private val batch = SpriteBatch()

    private val launch = TextGameButton("LAUNCH", "button", "pressedbutton",
            0.4f * screenWidth, 0.4f * screenHeight, 0.2f * screenWidth, 0.08f * screenWidth)
    private val exit = TextGameButton("menu", "button", "pressedbutton",
            0.4f * screenWidth, 0.05f * screenHeight, 0.2f * screenWidth, 0.08f * screenWidth)
    private val more = TextGameButton("->", "button", "pressedbutton",
            0.59f * screenWidth, 0.29f * screenHeight, 0.08f * screenWidth, 0.06f * screenWidth)
    private val less = TextGameButton("<-", "button", "pressedbutton",
            0.33f * screenWidth, 0.29f * screenHeight, 0.08f * screenWidth, 0.06f * screenWidth)
    private val onePlus = TextGameButton("+1", "button", "pressedbutton",
            0.59f * screenWidth, 0.18f * screenHeight, 0.08f * screenWidth, 0.06f * screenWidth)
    private val tenPlus = TextGameButton("+10", "button", "pressedbutton",
            0.70f * screenWidth, 0.18f * screenHeight, 0.08f * screenWidth, 0.06f * screenWidth)
    private val oneMinus = TextGameButton("-1", "button", "pressedbutton",
            0.33f * screenWidth, 0.18f * screenHeight, 0.08f * screenWidth, 0.06f * screenWidth)
    private val tenMinus = TextGameButton("-10", "button", "pressedbutton",
            0.22f * screenWidth, 0.18f * screenHeight, 0.08f * screenWidth, 0.06f * screenWidth)

    private val stage = Stage()
    private val input = InputMultiplexer()
    private val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
    private val generator = FreeTypeFontGenerator(Gdx.files.internal("android/assets/pixel.ttf"))

    private val deck = Deck(game)

    init {

        parameter.size = (screenHeight / 20).toInt()
        parameter.color = Color.WHITE
        parameter.borderColor = Color.BLACK
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        camera.position.set(Vector3(screenWidth / 2, screenHeight / 2, 0f))
        stage.addActor(exit.button)
        stage.addActor(launch.button)
        stage.addActor(less.button)
        stage.addActor(more.button)
        stage.addActor(onePlus.button)
        stage.addActor(tenPlus.button)
        stage.addActor(oneMinus.button)
        stage.addActor(tenMinus.button)
        input.addProcessor(stage)
        Gdx.input.inputProcessor = input
    }

    private val font = generator.generateFont(parameter)

    private var delay = 0f
    var launched = false
    var victories = 0
    var defeats = 0

    private var pathfinder = Pathfinder(deck)

    override fun render(delta: Float) {

        if (delay > 0f) delay -= delta

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        batch.projectionMatrix = camera.combined

        batch.begin()
        batch.draw(background, 0f, 0f, screenWidth, screenHeight)
        font.draw(batch, "SUITS: ${game.suitnumber}", screenWidth / 2, screenHeight * 0.34f,
                0f, 1, false)
        font.draw(batch, "GAMES: $games", screenWidth / 2, screenHeight * 0.235f,
                0f, 1, false)

        if (launched && games != 0) {

            val cycle = Dynamics(deck)

            if (game.start) {
                deck.deck.clear()
                deck.buildDeck()
                pathfinder = Pathfinder(deck)
            }

            if (cycle.isOver()) {
                if (game.victory) {
                    victories++
                    game.victory = false
                    game.defeat = false
                    deck.backup = 5
                    deck.finished = 0
                    game.start = true
                    games--
                }
                if (game.defeat) {
                    defeats++
                    game.victory = false
                    game.defeat = false
                    deck.backup = 5
                    deck.finished = 0
                    game.start = true
                    games--
                }
            } else {
                cycle.move()
                pathfinder.makeMove() //OneSolver(deck).step(0, setOf())
            }
        }

        if (launched && games == 0) {
            launched = false
            if (launch.button.isChecked) launch.button.toggle()
            games = 1
        }

        font.draw(batch, "VICTORIES - $victories", screenWidth / 2, screenHeight * 0.83f,
                0f, 1, false)
        font.draw(batch, "DEFEATS - $defeats", screenWidth / 2, screenHeight * 0.73f,
                0f, 1, false)

        batch.end()
        stage.draw()

        if (more.button.isChecked && delay <= 0f) {
            when {
                game.suitnumber == 1 -> game.suitnumber = 2
                game.suitnumber == 2 -> game.suitnumber = 4
                else -> {
                }
            }
            delay = 0.5f
            more.button.toggle()
        }

        if (less.button.isChecked && delay <= 0f) {
            when {
                game.suitnumber == 4 -> game.suitnumber = 2
                game.suitnumber == 2 -> game.suitnumber = 1
                else -> {
                }
            }
            delay = 0.5f
            less.button.toggle()
        }

        if (launch.button.isChecked && !launched) {
            victories = 0
            defeats = 0
            game.start = true
            game.defeat = false
            game.victory = false
            deck.finished = 0
            deck.backup = 5
            launched = true
        }

        if (tenMinus.button.isChecked) {
            if (games > 10) games -= 10 else games = 1
            delay = 0.5f
            tenMinus.button.toggle()
        }

        if (oneMinus.button.isChecked) {
            if (games > 1) games--
            delay = 0.5f
            oneMinus.button.toggle()
        }


        if (onePlus.button.isChecked) {
            if (games < 100) games++
            delay = 0.5f
            onePlus.button.toggle()
        }

        if (tenPlus.button.isChecked) {
            if (games <= 90) games += 10 else games = 100
            delay = 0.5f
            tenPlus.button.toggle()
        }

        if (exit.button.isChecked) {
            game.screen = MainMenu(game, music)
        }
    }

    override fun dispose() {
        generator.dispose()
        game.dispose()
        batch.dispose()
        launch.dispose()
        exit.dispose()
        less.dispose()
        more.dispose()
        stage.dispose()
        background.dispose()
    }

    override fun hide() {}
    override fun show() {}
    override fun pause() {}
    override fun resume() {}
    override fun resize(width: Int, height: Int) {}


}
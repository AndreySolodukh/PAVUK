package com.game.pavuk.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.game.pavuk.*
import com.game.pavuk.objects.TextGameButton

class PlayScreen(val game: Pavuk) : Screen {

    private val res = Resource(game)
    private val camera = OrthographicCamera(res.width, res.height)

    private val menu = TextGameButton("menu", "button", "pressedbutton", 0.72f * res.width,
            0.02f * res.height, 0.08f * res.width, 0.04f * res.width)
    private val new = TextGameButton("new", "button", "pressedbutton", 0.2f * res.width,
            0.02f * res.height, 0.08f * res.width, 0.04f * res.width)
    private val hint = TextGameButton("hint", "button", "pressedbutton", 0.46f * res.width,
            0.02f * res.height, 0.08f * res.width, 0.04f * res.width)
    private val auto = TextGameButton("auto", "button", "pressedbutton", 0.46f * res.width,
            0.12f * res.height, 0.08f * res.width, 0.04f * res.width)

    private val stage = Stage()
    private val input = InputMultiplexer()
    private val font = res.generator.generateFont(res.parameter)

    init {
        res.background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        camera.position.set(Vector3(res.width / 2, res.height / 2, 0f))
        stage.addActor(menu.button)
        stage.addActor(new.button)
        stage.addActor(hint.button)
        stage.addActor(auto.button)
        input.addProcessor(stage)
        Gdx.input.inputProcessor = input
        res.parameter.spaceX = (res.height * 0.0065f).toInt()
        res.parameter.borderColor = Color.BLACK
        res.parameter.size = (res.height * 0.091f).toInt()

        res.buildDeck()
    }

    private var delay = 0f

    override fun render(delta: Float) {

        if (delay > 0f) delay -= delta

        val cycle = Dynamics(res)

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        res.batch.projectionMatrix = camera.combined

        res.batch.begin()

        res.batch.draw(res.background, 0f, 0f, res.width, res.height)

        font.draw(res.batch, "In - ${res.backup}", 0.068f * res.width,
                0.05f * res.height, 0f, 1, false)
        if (res.backup == 0 && !cycle.isOver())
            font.draw(res.batch, "Press 'new' to give up", 0.24f * res.width,
                    0.18f * res.height, 0f, 1, false)
        font.draw(res.batch, "Out - ${res.finished}", 0.93f * res.width,
                0.05f * res.height, 0f, 1, false)

        if (cycle.isOver()) {
            if (res.victory) {
                font.draw(res.batch, "You won!", res.width / 2,
                        0.44f * res.height, 0f, 1, false)
                font.draw(res.batch, "press 'menu' to exit", res.width / 2,
                        0.36f * res.height, 0f, 1, false)
            }
            if (res.defeat) {
                font.draw(res.batch, "Game over", res.width / 2,
                        0.44f * res.height, 0f, 1, false)
                font.draw(res.batch, "press 'menu' to exit", res.width / 2,
                        0.36f * res.height, 0f, 1, false)
            }
        } else cycle.move()


        for (i in 0..Logic(res).lastLine()) {
            for (card in res.deck.filter { it.indicator !in res.moving && it.line == i }) {
                card.updateCoords()
                card.draw(res.batch)
            }
        }

        if (!cycle.isOver()) {
            if (res.moving.isEmpty() && res.from != -1 && res.to != -1) {
                font.draw(res.batch, "From", 0.072f * res.width + 0.098f * res.width * res.from, 0.36f * res.height, 0f, 1, false)
                font.draw(res.batch, "To", 0.072f * res.width + 0.098f * res.width * res.to, 0.36f * res.height, 0f, 1, false)
            }
            if (res.moving.isNotEmpty()) {
                res.from = -1
                res.to = -1
            }
        }

        for (card in res.deck.filter { it.indicator in res.moving }.sortedBy { it.line }) {
            card.updateMoving(res.moving)
            card.draw(res.batch)
        }

        res.batch.end()

        stage.draw()

        if (menu.button.isChecked) {
            menu.button.toggle()
            game.screen = MainMenu(game)
            dispose()
        }

        if (auto.button.isChecked && delay <= 0) {
            if (!cycle.isOver()) {
                delay = 0.075f
                res.from = -1
                res.to = -1
                Solver(res).step()
            } else {
                auto.button.toggle()
            }
        }

        if (hint.button.isChecked && delay <= 0f)
            if (!cycle.isOver()) {
                Solver(res).step()
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
        res.dispose()
        new.dispose()
        hint.dispose()
        auto.dispose()
        menu.dispose()
        stage.dispose()
        font.dispose()
    }

    override fun show() {}
    override fun pause() {}
    override fun resume() {}
    override fun resize(width: Int, height: Int) {}
    override fun hide() {}
}
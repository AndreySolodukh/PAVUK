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

    private val res = Resource()
    private val camera = OrthographicCamera(1024f, 768f)

    private val menu = TextGameButton("menu", "button", "pressedbutton", 0.72f * res.width,
            0.02f * res.height, 0.08f * res.width, 0.04f * res.width)
    private val new = TextGameButton("new", "button", "pressedbutton", 0.2f * res.width,
            0.02f * res.height, 0.08f * res.width, 0.04f * res.width)
    private val hint = TextGameButton("hint", "button", "pressedbutton", 0.46f * res.width,
            0.02f * res.height, 0.08f * res.width, 0.04f * res.width)
    private val auto = TextGameButton("auto", "button", "pressedbutton", 0.46f * res.width,
            0.08f * res.height, 0.08f * res.width, 0.04f * res.width)

    private val stage = Stage()
    private val input = InputMultiplexer()
    private val font = res.generator.generateFont(res.parameter)

    init {
        res.background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        camera.position.set(Vector3(512f, 384f, 0f))
        stage.addActor(menu.button)
        stage.addActor(new.button)
        stage.addActor(hint.button)
        stage.addActor(auto.button)
        input.addProcessor(stage)
        Gdx.input.inputProcessor = input
        res.parameter.spaceX = 5
        res.parameter.borderColor = Color.BLACK
        res.parameter.size = 70

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

        res.batch.draw(res.background, 0f, 0f, 1024f, 768f)

        font.draw(res.batch, "In - ${res.backup}", 70f, 40f, 0f, 1, false)
        if (res.backup == 0 && !cycle.isOver())
            font.draw(res.batch, "Press 'new' to give up", 240f, 100f, 0f, 1, false)
        font.draw(res.batch, "Out - ${res.finished}", 954f, 40f, 0f, 1, false)

        if (cycle.isOver()) {
            if (res.victory) {
                font.draw(res.batch, "You won!", 512f, 400f, 0f, 1, false)
                font.draw(res.batch, "press 'menu' to exit", 512f, 340f, 0f, 1, false)
            }
            if (res.defeat) {
                font.draw(res.batch, "Game over", 512f, 400f, 0f, 1, false)
                font.draw(res.batch, "press 'menu' to exit", 512f, 340f, 0f, 1, false)
            }
        } else cycle.move()

        for (i in 0..50) {
            val cards = res.deck.filter { it.indicator !in res.moving && it.line == i }
            if (cards.isEmpty()) break
            for (card in cards) {
                card.updateCoords()
                card.draw(res.batch)
            }
        }

        if (!cycle.isOver()) {
            if (res.moving.isEmpty() && res.from != -1 && res.to != -1) {
                font.draw(res.batch, "From", 74f + 100f * res.from, 280f, 0f, 1, false)
                font.draw(res.batch, "To", 74f + 100f * res.to, 280f, 0f, 1, false)
            }
            if (res.moving.isNotEmpty()) {
                res.from = -1
                res.to = -1
            }
        }

        for (card in res.deck.filter { it.indicator in res.moving }.sortedBy { it.line }) {
            card.upgradeMoving(res.moving)
            card.draw(res.batch)
        }

        res.batch.end()
        stage.draw()

        if (menu.button.isChecked) {
            menu.button.toggle()
            game.screen = MainMenu(game)
            dispose()
        }

        if (auto.button.isChecked && delay <= 0f)
            if (!cycle.isOver()) {
            Solver(res).step()
            delay = 0.2f
            } else auto.button.toggle()

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
    }

    override fun show() {}
    override fun pause() {}
    override fun resume() {}
    override fun resize(width: Int, height: Int) {}
    override fun hide() {}
}
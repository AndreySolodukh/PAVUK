package com.game.pavuk.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.game.pavuk.Pavuk
import com.game.pavuk.Resource
import com.game.pavuk.objects.Card
import com.game.pavuk.objects.TextGameButton

class PlayScreen(val game: Pavuk) : Screen {

    private val res = Resource()
    private val camera = OrthographicCamera(1024f, 768f)

    private val menu = TextGameButton("menu", "button", "pressedbutton", 0.72f * res.width,
            0.02f * res.height, 0.08f * res.width, 0.04f * res.width)
    private val new = TextGameButton("new", "button", "pressedbutton", 0.2f * res.width,
            0.02f * res.height, 0.08f * res.width, 0.04f * res.width)
    private val hint = TextGameButton("hint", "button", "pressedbutton", 0.44f * res.width,
            0.02f * res.height, 0.08f * res.width, 0.04f * res.width)

    private val stage = Stage()
    private val input = InputMultiplexer()
    private val font = res.generator.generateFont(res.parameter)

    init {
        res.background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        camera.position.set(Vector3(512f, 384f, 0f))
        stage.addActor(menu.button)
        stage.addActor(new.button)
        stage.addActor(hint.button)
        input.addProcessor(stage)
        Gdx.input.inputProcessor = input
        res.parameter.spaceX = 5
        res.parameter.borderColor = Color.BLACK
        res.parameter.size = 70
    }

    // *** Создание колоды ***
    private val deck = mutableListOf<Card>()
    private var start = true
    private var moving = mutableListOf<Card>()
    private var oldColumn = 0
    private var backup = 0
    private var finished = 0
    private var delay = 0f
    private var from = 0f
    private var to = 0f

    init {
        val random = mutableListOf<Int>()
        for (i in 0..103)
            random.add(i % 13)
        for ((i, elem) in random.shuffled().withIndex()) {
            deck.add(Card(Sprite(res.atlas.findRegion("$elem")), 76f,
                    114f, true, elem, i))
        }
    }
    // ***********************
    // На будущее: deck[0] - абстрактная карта, с которой
    // берутся характеристики вроде ширины или длины.

    private fun hasCards(column: Int) = deck.any { it.bounds.x == 24f + 100 * column }

    private fun lastCard(column: Int): Card? {
        if (!hasCards(column)) return null
        var minY = res.height
        var sum: Card? = null
        for (card in deck.filter { it.bounds.x == 24f + 100 * column }) {
            if (card.bounds.y < minY) {
                sum = card
                minY = card.bounds.y
            }
        }
        return sum
    }

    private fun above(card: Card): Card? {
        if (card.bounds.y == 752f - deck[0].height) return null
        return deck.first {
            it.bounds.x in card.bounds.x - 1..card.bounds.x + 1 &&
                    it.bounds.y in card.bounds.y + 16f..card.bounds.y + 18f
        }
    }

    private fun upOrder(card: Card): Boolean {
        if (above(card) == null || !above(card)!!.isOpened) return false
        return (card.grade == above(card)!!.grade - 1)
    }

    private fun hasKing(column: Int): Boolean {
        if (!hasCards(column)) return false
        val x = 24f + 100f * column
        return deck.filter { it.bounds.x == x && it.isOpened }.any { it.grade == 12 }
    }

    private fun sequence(column: Int): Boolean {
        if (!hasKing(column)) return false
        var card = lastCard(column)!!
        for (i in 0..12) {
            if (card.grade != i || !card.isOpened) return false
            if (i < 12) card = above(card) ?: return false
        }
        return true
    }

    override fun render(delta: Float) {
        if (delay > 0f) delay -= delta
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        res.batch.projectionMatrix = camera.combined

        res.batch.begin()
        res.batch.draw(res.background, 0f, 0f, 1024f, 768f)
        font.draw(res.batch, "$finished", 940f, 70f, 0f, 1, false)

        if (finished == 8) {
            from = 0f
            to = 0f
            font.draw(res.batch, "You won!", 512f, 400f, 0f, 1, false)
            font.draw(res.batch, "press 'menu' to exit", 512f, 340f, 0f, 1, false)
        }

        if (moving.isEmpty() && setOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).any { sequence(it) }) {
            val i = setOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).first { sequence(it) }
            var card = lastCard(i)!!
            for (j in 0..12) {
                val above = above(card) ?: card
                card.bounds.y = -1f
                card.bounds.x = -1f
                card = above
            }
            finished++
        }

        if (start) {
            start = false
            var x = 24f
            var y = 752f - deck[0].height
            for (i in 0..53) {
                if (i < 44) deck[i].switch()
                deck[i].bounds.y = y
                deck[i].bounds.x = x
                x += deck[0].width + 24f
                if (x >= res.width) {
                    y -= 17f
                    x = 24f
                }
            }
            y = 26f
            x = 25f
            for (i in 54..103) {
                deck[i].switch()
                deck[i].bounds.y = y
                deck[i].bounds.x = x
                if (i % 10 == 3) x += 15f
            }
        } else {
            for (i in 0..9) {
                if (hasCards(i) && moving.isEmpty())
                    if (!lastCard(i)!!.isOpened) lastCard(i)!!.switch()
            }
            if (Gdx.input.isTouched(0)) {
                val x = Gdx.input.x
                val y = res.height - Gdx.input.y
                if (moving.isEmpty()) {
                    val column = if ((x - 12) / 100 < 10) (x - 12) / 100 else 9
                    if (hasCards(column)) {
                        var card = lastCard(column)
                        while (card != null) {
                            moving.add(card)
                            if (card.bounds.y + deck[0].height >= y) break
                            if (upOrder(card)) {
                                card = if (above(card)!!.isOpened) above(card) else null
                            } else card = null
                        }
                        if (moving.last().bounds.y + deck[0].height < y ||
                                moving.first().bounds.y > y) moving.clear()
                        if (moving.isNotEmpty()) {
                            val pos = moving.first().bounds.x.toInt()
                            oldColumn = if ((pos - 12) / 100 < 10) (pos - 12) / 100 else 9
                        }
                    }
                } else {
                    var yPos = y - 99f
                    for (card in moving.reversed()) {
                        card.bounds.x = x - deck[0].width / 2
                        card.bounds.y = yPos
                        yPos -= 17f
                    }
                }
            }
            if (!Gdx.input.isTouched && moving.isNotEmpty()) {
                val x = (moving.first().bounds.x + deck[0].width / 2).toInt()
                val column: Int = if ((x - 12) / 100 < 10) (x - 12) / 100 else 9
                if (!hasCards(column)) {
                    var y = 752f - deck[0].height
                    for (card in moving.reversed()) {
                        card.bounds.x = 24f + 100f * column
                        card.bounds.y = y
                        y -= 17f
                    }
                    moving.clear()
                } else {
                    // column != oldColumn - нужно, чтобы предотвратить вылет (наверное)
                    if (column != oldColumn && lastCard(column)!!.grade == moving.last().grade + 1) {
                        var y = lastCard(column)!!.bounds.y - 17f
                        for (card in moving.reversed()) {
                            card.bounds.x = 24f + 100f * column
                            card.bounds.y = y
                            y -= 17f
                        }
                        moving.clear()
                    } else {
                        var y = if (hasCards(oldColumn)) lastCard(oldColumn)!!.bounds.y - 17f
                        else 752f - deck[0].height
                        for (card in moving.reversed()) {
                            card.bounds.x = 24f + 100f * oldColumn
                            card.bounds.y = y
                            y -= 17f
                        }
                        moving.clear()
                    }
                }
            }

        }
        for (i in (752f - deck[0].height).toInt() downTo 26 step 17) {
            for (card in deck.filter {
                it.indicator !in moving.map { it.indicator }
                        && it.bounds.y.toInt() in i - 1..i + 1
            }) {
                card.draw(res.batch)
            }
        }
        for (card in moving.reversed()) card.draw(res.batch)

        if (from != 0f && to != 0f) {
            if (moving.isNotEmpty()) {
                from = 0f
                to = 0f
            } else {
                res.batch.draw(Texture("from.png"), from, 300f,
                        deck[0].width, deck[0].height)
                res.batch.draw(Texture("to.png"), to, 300f,
                        deck[0].width, deck[0].height)
            }
        }

        res.batch.end()
        stage.draw()

        if (menu.button.isChecked) {
            game.screen = MainMenu(game)
            dispose()
        }

        if (hint.button.isChecked && delay <= 0f) {
            delay = 1f
            Solver().step()
            hint.button.toggle()
        }

        if (new.button.isChecked) {
            if (backup < 5 && moving.isEmpty()) {
                for ((i, card) in deck.filter
                { it.bounds.y == 26f && it.bounds.x == 25f + 15f * backup }.withIndex()) {
                    val y = if (lastCard(i) != null) lastCard(i)!!.bounds.y - 17f
                    else 752f - deck[0].height
                    card.bounds.x = 24f + 100f * i
                    card.bounds.y = y
                    card.switch()
                }
                new.button.toggle()
                backup++
            } else new.button.toggle()
        }
    }

    override fun dispose() {
        game.dispose()
        res.dispose()
        menu.dispose()
        stage.dispose()
    }

    inner class Solver {

        fun step(/* n: Int */) {
            val lastCards = mutableListOf<MutableList<Card?>>()
            for (i in 0..9) lastCards.add(mutableListOf())
            //for (j in n downTo 0) {
                for (i in 0..9) {
                    lastCards[i].add(lastCard(i))
                    while (lastCards[i].last() != null && upOrder(lastCards[i].last()!!))
                        lastCards[i].add(above(lastCards[i].last()!!))
                    lastCards[i].remove(null)
                }
            //}

            //val aboveLast = mutableListOf<MutableList<Card?>>()
            //for (i in 0..9) {
            //    aboveLast.add(mutableListOf())
            //    aboveLast[i].add(above(lastCards[i].last()!!))
            //    while (aboveLast[i].last() != null && upOrder(aboveLast[i].last()!!))
            //        aboveLast[i].add(above(aboveLast[i].last()!!))
            //    aboveLast[i].remove(null)
            //}

            fun exact(set: List<Card>): Int {
                val upper = lastCards.filter {
                    it.isNotEmpty() && it.first()!!.grade == set.last().grade + 1
                }.map { lastCards.indexOf(it) }.toMutableSet()
                var num = -1
                for (elem in upper) {
                    val now = lastCards[elem].size
                    var max = -1
                    if (now > max) {
                        max = now
                        num = elem
                    }
                }
                return num
            }

            for (i in 0..11) {
                val lower = lastCards.filter { it.isNotEmpty() && it.last()!!.grade == i }.map {
                    lastCards.indexOf(it)
                }.toMutableSet()
                val upper = lastCards.filter { it.isNotEmpty() && it.first()!!.grade == i + 1 }.map {
                    lastCards.indexOf(it)
                }.toMutableSet()
                var num = -1
                var max = -1
                for (elem in upper) {
                    val now = lastCards[elem].size
                    if (now > max) {
                        max = now
                        num = elem
                    }
                }
                if (num != -1 && lower.isNotEmpty()) {
                    val index = lower.first()
                    var i = 0
                    from = lastCards[index].first()!!.bounds.x
                    to = lastCards[num].last()!!.bounds.x
                    for (elem in lastCards[index].reversed()) {
                        i++
                        elem!!.bounds.x = lastCards[num].last()!!.bounds.x
                        elem.bounds.y = lastCards[num].first()!!.bounds.y - i * 17f
                    }
                    break
                }
            }
        }

    }

    override fun show() {}
    override fun pause() {}
    override fun resume() {}
    override fun resize(width: Int, height: Int) {}
    override fun hide() {}
}
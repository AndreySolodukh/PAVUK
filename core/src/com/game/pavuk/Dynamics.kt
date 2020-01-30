package com.game.pavuk

import com.badlogic.gdx.Gdx

class Dynamics(private val deck: Deck) {

    private val height = deck.cardHeight
    private val width = deck.cardWidth
    private val logic = Logic(deck)

    init {
        if (deck.finished == 8) deck.game.victory = true
    }

    fun isOver() = deck.game.victory || deck.game.defeat

    fun move() {

        if (deck.moving.isEmpty() && deck.columns.any { logic.sequence(it) }) {
            val column = deck.columns.first { logic.sequence(it) }
            val last = logic.lastCard(column)!!.line
            for (i in last downTo last - 12) {
                deck.deck.first { it.column == column && it.line == i }.column = 10
            }
            deck.finished++
        }

        if (deck.game.start) {
            deck.game.start = false
            var column = 0
            var line = 0
            for (i in 0..53) {
                if (i < 44) deck.deck[i].switch()
                deck.deck[i].column = column
                deck.deck[i].line = line
                column++
                if (column == 10) {
                    line++
                    column = 0
                }
            }
            column = -1
            for (i in 54..103) {
                deck.deck[i].switch()
                deck.deck[i].column = column
            }
        } else {
            for (i in 0..9) {
                if (logic.hasCards(i) && deck.moving.isEmpty())
                    if (!logic.lastCard(i)!!.isOpened)
                        logic.lastCard(i)!!.switch()
            }
            if (Gdx.input.isTouched(0)) {
                val x = Gdx.input.x
                val y = deck.screenHeight - Gdx.input.y
                if (deck.moving.isEmpty()) {
                    val column = if ((x - 0.012f * deck.screenWidth) / (0.098f * deck.screenWidth) < 10)
                        ((x - 0.012f * deck.screenWidth) / (0.098f * deck.screenWidth)).toInt()
                    else 9
                    if (logic.hasCards(column)) {
                        var card = logic.lastCard(column)
                        while (card != null) {
                            deck.moving.add(card.indicator)
                            if (card.bounds.y + height >= y) break
                            if (logic.upOrder(card)) {
                                card = if (logic.above(card)!!.isOpened) logic.above(card) else null
                            } else card = null
                        }
                        val low = deck.deck.first { it.indicator == deck.moving.first() }
                        val high = deck.deck.first { it.indicator == deck.moving.last() }
                        if (high.bounds.y + height < y || low.bounds.y > y) deck.moving.clear()
                        if (deck.moving.isNotEmpty()) {
                            val pos = low.bounds.x.toInt()
                            deck.oldColumn =
                                    if ((pos - 0.012f * deck.screenWidth) / (0.098f * deck.screenWidth) < 10)
                                        ((pos - 0.012f * deck.screenWidth) / (0.098f * deck.screenWidth)).toInt()
                                    else 9
                            for (i in deck.moving) {
                                deck.deck.first { it.indicator == i }.column = -1
                            }
                        }
                    }
                }
            }
            if (!Gdx.input.isTouched && deck.moving.isNotEmpty()) {
                val low = deck.deck.first { it.indicator == deck.moving.first() }
                val high = deck.deck.first { it.indicator == deck.moving.last() }
                val x = (low.bounds.x + width / 2).toInt()
                val column: Int = if ((x - 0.012f * deck.screenWidth) / (0.098f * deck.screenWidth) < 10)
                    ((x - 0.012f * deck.screenWidth) / (0.098f * deck.screenWidth)).toInt()
                else 9
                var line = 0
                if (!logic.hasCards(column)) {
                    for (i in deck.moving.reversed()) {
                        val card = deck.deck.first { it.indicator == i }
                        card.column = column
                        card.line = line
                        line++
                    }
                    deck.moving.clear()
                } else {
                    if (column != deck.oldColumn && logic.lastCard(column)!!.grade == high.grade + 1) {
                        line = logic.lastCard(column)!!.line + 1
                        for (i in deck.moving.reversed()) {
                            val card = deck.deck.first { it.indicator == i }
                            card.column = column
                            card.line = line
                            line++
                        }
                        deck.moving.clear()
                    } else {
                        line = if (logic.hasCards(deck.oldColumn))
                            logic.lastCard(deck.oldColumn)!!.line + 1 else 0
                        for (i in deck.moving.reversed()) {
                            val card = deck.deck.first { it.indicator == i }
                            card.column = deck.oldColumn
                            card.line = line
                            line++
                        }
                        deck.moving.clear()
                    }
                }
            }
        }
    }

    fun new() {
        if (deck.backup == 0  && deck.moving.isEmpty()) {
            if (!deck.game.victory) deck.game.defeat = true
        }
        if (deck.backup > 0  && deck.moving.isEmpty()) {
            for (i in 0..9) {
                val card = deck.deck.first { it.column == -1 }
                card.line = if (logic.hasCards(i)) logic.lastCard(i)!!.line + 1 else 0
                card.column = i
                card.updateCoords()
            }
            deck.backup--
        }
    }
}
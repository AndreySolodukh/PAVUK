package com.game.pavuk

import com.badlogic.gdx.Gdx

class Dynamics(private val res: Resource) {

    private val height = res.deck[0].height
    private val width = res.deck[0].width
    private val logic = Logic(res.deck)

    init {
        if (res.finished == 8) res.victory = true
    }

    fun isOver() = res.victory || res.defeat

    fun move() {

        if (res.moving.isEmpty() && res.columns.any { logic.sequence(it) }) {
            val column = res.columns.first { logic.sequence(it) }
            val last = logic.lastCard(column)!!.line
            for (i in last downTo last - 12) {
                res.deck.first { it.column == column && it.line == i }.column = 10
            }
            res.finished++
        }

        if (res.start) {
            res.start = false
            var column = 0
            var line = 0
            for (i in 0..53) {
                if (i < 44) res.deck[i].switch()
                res.deck[i].column = column
                res.deck[i].line = line
                column++
                if (column == 10) {
                    line++
                    column = 0
                }
            }
            column = -1
            for (i in 54..103) {
                res.deck[i].switch()
                res.deck[i].column = column
            }
        } else {
            for (i in 0..9) {
                if (logic.hasCards(i) && res.moving.isEmpty())
                    if (!logic.lastCard(i)!!.isOpened)
                        logic.lastCard(i)!!.switch()
            }
            if (Gdx.input.isTouched(0)) {
                val x = Gdx.input.x
                val y = res.height - Gdx.input.y
                if (res.moving.isEmpty()) {
                    val column = if ((x - 0.012f * res.width) / (0.098f * res.width) < 10)
                        ((x - 0.012f * res.width) / (0.098f * res.width)).toInt()
                    else 9
                    if (logic.hasCards(column)) {
                        var card = logic.lastCard(column)
                        while (card != null) {
                            res.moving.add(card.indicator)
                            if (card.bounds.y + height >= y) break
                            if (logic.upOrder(card)) {
                                card = if (logic.above(card)!!.isOpened) logic.above(card) else null
                            } else card = null
                        }
                        val low = res.deck.first { it.indicator == res.moving.first() }
                        val high = res.deck.first { it.indicator == res.moving.last() }
                        if (high.bounds.y + height < y || low.bounds.y > y) res.moving.clear()
                        if (res.moving.isNotEmpty()) {
                            val pos = low.bounds.x.toInt()
                            res.oldColumn =
                                    if ((pos - 0.012f * res.width) / (0.098f * res.width) < 10)
                                        ((pos - 0.012f * res.width) / (0.098f * res.width)).toInt()
                                    else 9
                            for (i in res.moving) {
                                res.deck.first { it.indicator == i }.column = -1
                            }
                        }
                    }
                }
            }
            if (!Gdx.input.isTouched && res.moving.isNotEmpty()) {
                val low = res.deck.first { it.indicator == res.moving.first() }
                val high = res.deck.first { it.indicator == res.moving.last() }
                val x = (low.bounds.x + width / 2).toInt()
                val column: Int = if ((x - 0.012f * res.width) / (0.098f * res.width) < 10)
                    ((x - 0.012f * res.width) / (0.098f * res.width)).toInt()
                else 9
                var line = 0
                if (!logic.hasCards(column)) {
                    for (i in res.moving.reversed()) {
                        val card = res.deck.first { it.indicator == i }
                        card.column = column
                        card.line = line
                        line++
                    }
                    res.moving.clear()
                } else {
                    // column != oldColumn - нужно, чтобы предотвратить вылет (наверное)
                    if (column != res.oldColumn && logic.lastCard(column)!!.grade == high.grade + 1) {
                        line = logic.lastCard(column)!!.line + 1
                        for (i in res.moving.reversed()) {
                            val card = res.deck.first { it.indicator == i }
                            card.column = column
                            card.line = line
                            line++
                        }
                        res.moving.clear()
                    } else {
                        line = if (logic.hasCards(res.oldColumn))
                            logic.lastCard(res.oldColumn)!!.line + 1 else 0
                        for (i in res.moving.reversed()) {
                            val card = res.deck.first { it.indicator == i }
                            card.column = res.oldColumn
                            card.line = line
                            line++
                        }
                        res.moving.clear()
                    }
                }
            }
        }
    }

    fun new() {
        if (res.backup == 0  && res.moving.isEmpty()) {
            if (!res.victory) res.defeat = true
        }
        if (res.backup > 0  && res.moving.isEmpty()) {
            var column = 0
            for (card in res.deck.filter { it.column == -1 }) {
                if (column == 10) break
                card.line = if (logic.hasCards(column)) logic.lastCard(column)!!.line + 1 else 0
                card.column = column
                column++
            }
            res.backup--
        }
    }
}
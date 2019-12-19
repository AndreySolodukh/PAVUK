package com.game.pavuk

import com.game.pavuk.objects.Card

class Solver(private val res: Resource) {

    private val logic = Logic(res.deck)

    private fun formLastCards(repeats: Int): MutableList<MutableList<Card?>> {
        val lastCards = mutableListOf<MutableList<Card?>>()
        for (i in 0..9) lastCards.add(mutableListOf())
        for (j in 0..repeats) {
            for (i in 0..9) {
                if (j == 0)
                    lastCards[i] = mutableListOf(logic.lastCard(i))
                else
                    if (lastCards[i].isNotEmpty())
                        if (logic.above(lastCards[i].last()!!) != null
                                && logic.above(lastCards[i].last()!!)!!.isOpened) {
                            lastCards[i] = mutableListOf(logic.above(lastCards[i].last()!!))
                        } else lastCards[i].clear()
                while (lastCards[i].isNotEmpty() &&
                        lastCards[i].last() != null && logic.upOrder(lastCards[i].last()!!))
                    lastCards[i].add(logic.above(lastCards[i].last()!!))
                lastCards[i].remove(null)
            }
        }
        return lastCards
    }

    private val lastCards = formLastCards(0)

    private fun exact(pack: MutableList<Card?>, column: Int) {
        var index = column
        if (index == -1) {
            val upper = lastCards.filter {
                it.isNotEmpty() && it.first()!!.grade == pack.last()!!.grade + 1
            }.map { lastCards.indexOf(it) }.toMutableSet()
            var max = -1
            for (elem in upper) {
                val now = lastCards[elem].size
                if (now > max) {
                    max = now
                    index = elem
                }
            }
        }
        var line = if (logic.hasCards(index)) logic.lastCard(index)!!.line + 1 else 0
        res.from = pack.first()!!.column
        for (elem in pack.filter { it != null }.reversed()) {
            elem!!.line = line
            elem.column = index
            line++
        }
        res.to = pack.first()!!.column
    }

    private fun suitable(set: List<MutableList<Card?>>): MutableList<Card?> {
        if (set.isEmpty()) return mutableListOf()
        val priority = mutableListOf<Int>()
        for (i in set.indices) priority.add(0)
        for (i in set.indices) {
            priority[i] = set[i].size
        }

        for (i in set.indices)
            if (set[i].isNotEmpty()) {
                val above = logic.above(set[i].last()!!)
                if (above == null) priority[i] += 50 // очистить колонку - максимальный приоритет
                else if (!above.isOpened) priority[i] += 20 // открыть закрытую карту - повышенный приоритет
            }
        var max = -1
        for (elem in priority)
            if (max < elem) max = elem
        return set[priority.indexOfFirst { it == max }]
    }

    private fun directions(cards: MutableList<Card?>): Set<Int> =
            lastCards.filter {
                it.isNotEmpty()
                        && it.first()!!.grade == cards.last()!!.grade + 1
            }.map {
                it.first()!!.column
            }.toSet()


    fun step() {

        var committed = false

        if (res.columns.any { logic.gradeInColumn(it, 12) }) {
            val kings = lastCards.filter {
                it.isNotEmpty()
                        && logic.gradeInColumn(it.first()!!.column, 12)
            }
            for (elem in kings) {
                val suitable = lastCards.filter {
                    it.isNotEmpty()
                            && !logic.gradeInColumn(it.first()!!.column, 12)
                            && it.last()!!.grade + 1 >= elem.first()!!.grade
                }
                var size = 0
                var index = mutableListOf<Card?>()
                for (pack in suitable) {
                    if (pack.size > size) {
                        size = pack.size
                        index = pack
                    }
                }
                index = index.filter { it!!.grade < elem.first()!!.grade }.toMutableList()
                if (index.isEmpty()) {
                    continue
                }
                exact(index, elem.first()!!.column)
                committed = true
                break
            }
        }

        if (committed) return

        val toMove = lastCards.filter { it.isNotEmpty() && directions(it).isNotEmpty() }

        if (toMove.size == 1) {
            exact(toMove[0], -1)
            committed = true
        }

        if (committed) return

        if (toMove.isNotEmpty()) {
            var repeats = 1
            var preToMove = toMove
            while (true) {
                var nextToMove = formLastCards(repeats)
                nextToMove = nextToMove.filter { pack ->
                    pack.isNotEmpty()
                            && nextToMove.indexOf(pack) in toMove.map { it.first()!!.column }
                            && directions(pack).isNotEmpty()
                            && (directions(pack).first() != directions(toMove.first {
                        it.first()!!.column == pack.first()!!.column
                    }).first()
                            || directions(toMove.first {
                        it.first()!!.column == pack.first()!!.column
                    }).size > repeats)
                }.toMutableList()
                if (nextToMove.size == 1) {
                    val column = nextToMove.first().first()!!.column
                    exact(toMove.first { it.first()!!.column == column }, -1)
                    committed = true
                    break
                }
                if (nextToMove.size > 1) {
                    preToMove = nextToMove
                    repeats++
                    continue
                }
                exact(suitable(toMove.filter { pack -> pack.first()!!.column in preToMove.map {
                    it.first()!!.column } }), -1)
                committed = true
                break
            }
        }

        if (committed) return

        if (res.columns.any { !logic.hasCards(it) }) {
            val priority = if (suitable(formLastCards(1)).isNotEmpty())
                mutableListOf(lastCards[suitable(formLastCards(1)).first()!!.column]) else mutableListOf()

            if (priority.isEmpty())
                priority.addAll(lastCards.filter {
                    it.isNotEmpty()
                            && it.last()!!.line > 0
                            && !logic.above(it.last()!!)!!.isOpened
                })

            if (priority.isEmpty())
                if (suitable(formLastCards(1)).isNotEmpty())
                    priority.addAll(lastCards.filter {
                        it.isNotEmpty() && it.last()!!.line == 1
                    }.toMutableList())

            if (priority.isEmpty())
                for (grade in 12 downTo 0) {
                    priority.addAll(lastCards.filter {
                        it.isNotEmpty()
                                && it.last()!!.grade == grade && logic.above(it.last()!!) != null
                    })
                    if (priority.isNotEmpty()) break
                }

            var max = 0
            if (priority.isNotEmpty()) {
                var cards = priority.first()
                for (elem in priority) {
                    if (elem.size > max) {
                        max = elem.size
                        cards = elem
                    }
                }
                exact(cards.toMutableList(), res.columns.first { !logic.hasCards(it) })
                committed = true
            }
        }

        if (committed) return

        Dynamics(res).new()
    }


}
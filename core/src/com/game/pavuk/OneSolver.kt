package com.game.pavuk

import com.game.pavuk.objects.Card

class OneSolver(private val deck: Deck) {

    private val logic = Logic(deck)

    private val lastCards = mutableListOf<MutableList<Card?>>()

    // from - линия, с которой берутся карты
    // to - линия, на которую переставляются карты
    // size - количество карт
    var from = -1
    var to = -1
    var size = -1
    var stepPriority = 0
    var nothingHappened = false
    var solution = false
    //var prohibitedColumns = mutableSetOf<Int>()

    private fun formLastCards(repeats: Int, prohibitedColumns: Set<Int>): MutableList<MutableList<Card?>> {
        val lastCards = mutableListOf<MutableList<Card?>>()
        for (i in 0..9) lastCards.add(mutableListOf())
        for (j in 0..repeats) {
            for (i in 0..9) {
                if (j == 0)
                    if (i !in prohibitedColumns) lastCards[i] = mutableListOf(logic.lastCard(i))
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

    private fun exact(pack: MutableList<Card?>, column: Int) {

        stepPriority = 0

        val index = if (column == -1) lastCards.filter {
            it.isNotEmpty() && it.first()!!.grade == pack.last()!!.grade + 1
        }.map { lastCards.indexOf(it) }.maxBy { lastCards[it].size }!!
        else column

        from = pack.first { it != null }!!.column
        to = index
        size = pack.filterNotNull().size
        stepPriority = logic.highInColumn(index) * 5 + pack.size

        var line = if (logic.hasCards(index)) logic.lastCard(index)!!.line + 1 else 0
        for (elem in pack.filterNotNull().reversed()) {
            elem.line = line
            elem.column = index
            line++
        }
        if (logic.sequence(index)) solution = true
        if (solution) stepPriority += 5000
        if (!logic.hasCards(from)) stepPriority += 200
        else if (!logic.lastCard(from)!!.isOpened) stepPriority += 400
        if (logic.gradeInColumn(index, 12)) stepPriority += 600
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

    fun step(ignoreChecks: Int, prohibitedColumns: Set<Int>): Int {

        lastCards.clear()
        lastCards.addAll(formLastCards(0, prohibitedColumns))

        if (ignoreChecks < 1)
            if (deck.columns.any { logic.gradeInColumn(it, 12) }) {
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

                    val best = (suitable.maxBy { it.size } ?: mutableListOf()).filter {
                        it!!.grade < elem.first()!!.grade
                    }.toMutableList()
                    if (best.isEmpty()) {
                        continue
                    }
                    exact(best, elem.first()!!.column)
                    return 1
                }
            }

        val toMove = lastCards.filter { it.isNotEmpty() && directions(it).isNotEmpty() }
        if (ignoreChecks < 2)
            if (toMove.size == 1) {
                exact(toMove[0], -1)
                return 2
            }

        if (ignoreChecks < 3)
            if (toMove.isNotEmpty()) {
                var repeats = 1
                var preToMove = toMove
                while (true) {
                    var nextToMove = formLastCards(repeats, prohibitedColumns)
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
                        return 3
                    }
                    if (nextToMove.size > 1) {
                        preToMove = nextToMove
                        repeats++
                        continue
                    }
                    exact(suitable(toMove.filter { pack ->
                        pack.first()!!.column in preToMove.map {
                            it.first()!!.column
                        }
                    }), -1)
                    return 3
                }
            }
        if (ignoreChecks < 4)
            if (deck.columns.any { !logic.hasCards(it) }) {
                val priority = if (suitable(formLastCards(1, prohibitedColumns)).isNotEmpty())
                    listOf(lastCards[suitable(formLastCards(1, prohibitedColumns)).first()!!.column]).filter {
                        logic.above(it.last()!!) != null }.toMutableList() else mutableListOf()
                if (priority.isEmpty()) {
                    priority.addAll(lastCards.filter {
                        it.isNotEmpty()
                                && it.last()!!.line > 0
                                && !logic.above(it.last()!!)!!.isOpened
                    })
                }
                if (priority.isEmpty()) {
                    if (suitable(formLastCards(1, prohibitedColumns)).isNotEmpty())
                        priority.addAll(lastCards.filter {
                            it.isNotEmpty() && it.last()!!.line == 1
                        }.toMutableList())
                }
                if (priority.isEmpty()) {
                    for (grade in 12 downTo 0) {
                        priority.addAll(lastCards.filter {
                            it.isNotEmpty()
                                    && it.last()!!.grade == grade && logic.above(it.last()!!) != null
                        })
                        if (priority.isNotEmpty()) break
                    }
                }
                if (priority.isNotEmpty()) {
                    val cards = priority.maxBy { it.size }!!
                    exact(cards.toMutableList(), deck.columns.first { !logic.hasCards(it) })
                    return 4
                }
            }
        nothingHappened = true
        return 4
    }

    fun backStep(from: Int, to: Int, size: Int) {
        var card = logic.lastCard(to)!!
        val cards = mutableListOf(card)
        for (i in 2..size) {
            card = logic.above(card)!!
            cards.add(card)
        }
        var line = if (!logic.hasCards(from)) 0 else logic.lastCard(from)!!.line + 1
        for (elem in cards.reversed()) {
            elem.column = from
            elem.line = line
            line++
        }
    }
}
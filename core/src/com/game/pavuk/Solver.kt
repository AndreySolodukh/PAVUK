package com.game.pavuk

import com.game.pavuk.objects.Card

class Solver(private val deck: Deck) {

    private val logic = Logic(deck)

    private fun formLastCards(repeats: Int, suit: Char): MutableList<MutableList<Card?>> {
        val lastCards = mutableListOf<MutableList<Card?>>()
        for (i in 0..9) lastCards.add(mutableListOf())
        for (j in 0..repeats) {
            for (i in 0..9) {
                if (j == 0) {
                    if (logic.hasCards(i) && (logic.lastCard(i)!!.suit == suit || suit == 'i'))
                        lastCards[i] = mutableListOf(logic.lastCard(i))
                } else {
                    if (lastCards[i].isNotEmpty())
                        if (logic.above(lastCards[i].last()!!) != null
                                && logic.above(lastCards[i].last()!!)!!.isOpened
                                && (logic.above(lastCards[i].last()!!)!!.suit == suit
                                        || suit == 'i')) {
                            lastCards[i] = mutableListOf(logic.above(lastCards[i].last()!!))
                        } else lastCards[i].clear()
                }
                while (lastCards[i].isNotEmpty() &&
                        lastCards[i].last() != null && logic.upOrder(lastCards[i].last()!!))
                    lastCards[i].add(logic.above(lastCards[i].last()!!))
                lastCards[i].remove(null)
            }
        }
        return lastCards
    }

    private var lastCards = mutableListOf<MutableList<Card?>>()

    private fun exact(pack: MutableList<Card?>, column: Int, suit: Char) {
        var index = column
        if (index == -1) {
            if (suit == 'i') {
                val sizes = mutableListOf<Int>()
                for (elem in lastCards) {
                    var size = 0
                    var card = elem.first()
                    if (card != null && card.grade == pack.last()!!.grade + 1) {
                        while (card != null) {
                            size++
                            card = if (logic.above(card) != null
                                    && logic.above(card)!!.grade == card.grade + 1) {
                                logic.above(card)
                            } else null
                        }
                    }
                    sizes.add(size)
                }
                index = sizes.indexOf(sizes.max())
            } else {
                index = lastCards.filter {
                    it.isNotEmpty() && it.first()!!.grade == pack.last()!!.grade + 1
                }.maxBy { it.size }!!.first()!!.column
            }
        }
        var line = if (logic.hasCards(index)) logic.lastCard(index)!!.line + 1 else 0
        for (elem in pack.filterNotNull().reversed()) {
            elem.line = line
            elem.column = index
            line++
        }
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

        val max = priority.max()
        return set[priority.indexOfFirst { it == max }]
    }

    private fun directions(cards: MutableList<Card?>, suit: Char): Set<Int> {
        if (suit != 'i') {
            return lastCards.filter {
                it.isNotEmpty()
                        && it.first()!!.grade == cards.last()!!.grade + 1
            }.map {
                it.first()!!.column
            }.toSet()
        } else {
            // обход зацикливания
            if (logic.above(cards.last()!!) == null) return setOf()
            var upperCards = 0
            var card = cards.last()!!
            while (logic.above(card) != null && logic.above(card)!!.grade == card.grade + 1) {
                card = logic.above(card)!!
                upperCards++
            }
            return lastCards.filter {
                it.size > upperCards
                        && it.first()!!.grade == cards.last()!!.grade + 1
            }.map {
                it.first()!!.column
            }.toSet()
        }
    }


    class Picker {

        private val packsToMove = mutableListOf<MutableList<Card?>>()
        private val placesToMove = mutableListOf<Int>()
        private val packSuits = mutableListOf<Char>()
        private val movePriority = mutableListOf<Int>()
        private var bestLastCards = mutableListOf<MutableList<MutableList<Card?>>>()

        // defaults
        init {
            packsToMove.add(mutableListOf())
            placesToMove.add(-2)
            movePriority.add(-2)
            packSuits.add('x')
            bestLastCards.add(mutableListOf())
        }

        fun addPack(pack: MutableList<Card?>, place: Int, priority: Int, suit: Char,
                    lastCards: MutableList<MutableList<Card?>>) {
            packsToMove.add(pack)
            placesToMove.add(place)
            movePriority.add(priority)
            packSuits.add(suit)
            bestLastCards.add(lastCards)
        }

        private fun getBest(): Int {
            return movePriority.indexOf(movePriority.max())
        }

        fun getBestPack() = packsToMove[getBest()]
        fun getBestPlace() = placesToMove[getBest()]
        fun getBestSuit() = packSuits[getBest()]
        fun getBestLastCards() = bestLastCards[getBest()]
    }

    fun step() {

        val picker = Picker()

        for (suit in listOf('s', 'h', 'd', 'c', 'i')) {

            var broken = false
            lastCards = formLastCards(0, suit)

            if (deck.columns.any { logic.gradeInColumn(it, 12) }) {
                val kings = lastCards.filter {
                    it.isNotEmpty()
                            && logic.gradeInColumn(it.first()!!.column, 12)
                }
                for (elem in kings) {
                    val suitable = lastCards.filter {
                        it.isNotEmpty()
                                && !logic.gradeInColumn(it.first()!!.column, 12)
                                && ((it.last()!!.grade + 1 >= elem.first()!!.grade && suit != 'i')
                                || it.last()!!.grade + 1 == elem.first()!!.grade)
                                && logic.highInColumn(it.last()!!.column) != 12
                    }
                    val index = (suitable.maxBy { it.size } ?: mutableListOf()).filter {
                        it!!.grade < elem.first()!!.grade
                    }.toMutableList()

                    if (index.isEmpty()) continue

                    if (suit == 'i') {
                        exact(index, elem.first()!!.column, suit)
                        return
                    } else {
                        picker.addPack(index, elem.first()!!.column,
                                index.size * 10 + index.last()!!.grade + 600, suit, lastCards)
                        broken = true
                        break
                    }
                }
                if (broken) continue
            }

            // КАКИЕ можно передвинуть
            val toMove = lastCards.filter { it.isNotEmpty() && directions(it, suit).isNotEmpty() }

            if (toMove.size == 1) {
                if (suit == 'i') {
                    exact(toMove[0], -1, suit)
                    return
                } else {
                    picker.addPack(toMove[0], -1, toMove[0].size * 10 +
                            toMove[0].last()!!.grade + 200, suit, lastCards)
                    continue
                }
            }

            if (toMove.isNotEmpty()) {
                var repeats = 1
                var preToMove = toMove
                while (true) {
                    var nextToMove = formLastCards(repeats, suit)
                    nextToMove = nextToMove.filter { pack ->
                        pack.isNotEmpty()
                                && nextToMove.indexOf(pack) in toMove.map { it.first()!!.column }
                                && directions(pack, suit).isNotEmpty()
                                && (directions(pack, suit).first() != directions(toMove.first {
                            it.first()!!.column == pack.first()!!.column
                        }, suit).first()
                                || directions(toMove.first {
                            it.first()!!.column == pack.first()!!.column
                        }, suit).size > repeats)
                    }.toMutableList()
                    if (nextToMove.size == 1) {
                        val column = nextToMove.first().first()!!.column
                        if (suit == 'i') {
                            exact(toMove.first { it.first()!!.column == column }, -1, suit)
                            return
                        } else {
                            val pack = toMove.first { it.first()!!.column == column }
                            picker.addPack(pack, -1,
                                    pack.size * 10 + pack.last()!!.grade + 300,
                                    suit, lastCards)
                            broken = true
                            break
                        }
                    }
                    if (nextToMove.size > 1) {
                        preToMove = nextToMove
                        repeats++
                        continue
                    }
                    if (suit == 'i') {
                        exact(suitable(toMove.filter { pack ->
                            pack.first()!!.column in preToMove.map {
                                it.first()!!.column
                            }
                        }), -1, suit)
                        return
                    } else {
                        val pack = suitable(toMove.filter { pack ->
                            pack.first()!!.column in preToMove.map {
                                it.first()!!.column
                            }
                        })
                        picker.addPack(pack, -1, pack.size * 10 + pack.last()!!.grade +
                                300, suit, lastCards)
                        broken = true
                        break
                    }
                }
            }

            if (broken) continue

            if (deck.columns.any { !logic.hasCards(it) }) {
                val priority = if (suitable(formLastCards(1, suit)).isNotEmpty())
                    mutableListOf(lastCards[suitable(formLastCards(1, suit)).first()!!.column])
                else mutableListOf()

                if (priority.isEmpty()) {
                    priority.addAll(lastCards.filter {
                        it.isNotEmpty()
                                && it.last()!!.line > 0
                                && !logic.above(it.last()!!)!!.isOpened
                    })
                }

                if (priority.isEmpty()) {
                    for (grade in 12 downTo 0) {
                        priority.addAll(lastCards.filter {
                            it.isNotEmpty()
                                    && it.last()!!.grade == grade && logic.above(it.last()!!) != null
                                    && (logic.gradeInColumn(it.last()!!.column, 12)
                                    || logic.highInColumn(it.last()!!.column) != 12)
                        })
                        if (priority.isNotEmpty()) break
                    }
                }

                if (priority.isNotEmpty()) {
                    val cards = priority.maxBy { it.size }!!
                    if (suit == 'i') {
                        exact(cards.toMutableList(), deck.columns.first { !logic.hasCards(it) }, suit)
                        return
                    } else {
                        picker.addPack(cards.toMutableList(),
                                deck.columns.first { !logic.hasCards(it) },
                                cards.size * 10 + cards.last()!!.grade + 100, suit, lastCards)
                        continue
                    }
                }
            }

            if (suit == 'c' && picker.getBestPack().isNotEmpty()) {
                val bestpack = picker.getBestPack()
                val bestplace = picker.getBestPlace()
                val bestsuit = picker.getBestSuit()
                lastCards = picker.getBestLastCards()
                exact(bestpack, bestplace, bestsuit)
                return
            }

        }
        Dynamics(deck).new()
    }
}
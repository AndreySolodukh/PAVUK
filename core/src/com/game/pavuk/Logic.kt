package com.game.pavuk

import com.game.pavuk.objects.Card

class Logic(private val deck: MutableList<Card>) {

    private fun cardsInColumn(column: Int) = deck.filter { it.column == column }

    fun hasCards(column: Int) = deck.any { it.column == column }

    fun lastCard(column: Int): Card? {
        if (!hasCards(column)) return null
        val cards = cardsInColumn(column)
        var max = -1
        for (card in cards.map { it.line })
            if (card > max) max = card
        return cards.first { it.line == max }
    }

    fun above(card: Card): Card? {
        try {
            if (card.line < 1) return null
            return cardsInColumn(card.column).first { it.line == card.line - 1 }
        } catch (e: NoSuchElementException) {
            return null
        }
    }

    fun upOrder(card: Card): Boolean {
        if (above(card) == null) return false
        if (!above(card)!!.isOpened) return false
        return (card.grade == above(card)!!.grade - 1)
    }

    fun gradeInColumn(column: Int, grade: Int): Boolean {
        if (!hasCards(column)) return false
        var card = lastCard(column)!!
        while (card.grade != grade) {
            if (!upOrder(card)) return false
            else card = above(card)!!
        }
        return true
    }

    fun sequence(column: Int): Boolean =
            gradeInColumn(column, 12) && gradeInColumn(column, 0)

}
package com.game.pavuk

import com.game.pavuk.objects.Card
import kotlin.NoSuchElementException

class Logic(private val deck: Deck) {

    private fun cardsInColumn(column: Int) = deck.deck.filter { it.column == column }

    fun hasCards(column: Int) = deck.deck.any { it.column == column }

    fun lastLine(): Int {
        var max = 0
        for (column in deck.columns) {
            val last = lastCard(column)
            if (last != null && last.line > max) max = last.line
        }
        return max
    }

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
        return (card.grade == above(card)!!.grade - 1 && card.suit == above(card)!!.suit)
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

    fun highInColumn(column: Int): Int {
        var card = lastCard(column) ?: return -1
        while (above(card) != null && above(card)!!.grade == card.grade + 1 ) {
            card = above(card)!!
        }
        return card.grade
    }

    fun sequence(column: Int): Boolean =
            gradeInColumn(column, 12) && gradeInColumn(column, 0)

}
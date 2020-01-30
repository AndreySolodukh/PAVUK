package com.game.pavuk

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.game.pavuk.objects.Card

class Deck(val game: Pavuk) {


    val screenWidth = Gdx.graphics.width.toFloat()
    val screenHeight = Gdx.graphics.height.toFloat()
    val cardWidth = screenWidth * 0.074f
    val cardHeight = screenHeight * 0.15f

    val deck = mutableListOf<Card>()
    val columns = setOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    var moving = mutableListOf<Int>()
    var from = -1
    var to = -1
    var backup = 5
    var finished = 0
    var oldColumn = 0

    fun buildDeck() {
        val random = mutableListOf<Pair<Int, Char>>()
        when {
            game.suitnumber == 1 ->
                for (i in 0..103)
                    random.add(i % 13 to 's')
            game.suitnumber == 2 ->
                for (i in 0..51) {
                    random.add(i % 13 to 's')
                    random.add(i % 13 to 'h')
                }
            else ->
                for (i in 0..25) {
                    random.add(i % 13 to 's')
                    random.add(i % 13 to 'h')
                    random.add(i % 13 to 'd')
                    random.add(i % 13 to 'c')
                }
        }
        for ((i, elem) in random.shuffled().withIndex()) {
            deck.add(Card(true, elem.first, elem.second,  -1, 0, i))
        }
    }
}
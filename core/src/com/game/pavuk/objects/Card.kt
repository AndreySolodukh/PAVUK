package com.game.pavuk.objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle

/*
class Card(isOpened: Boolean, grade: Int, suit: Char, column: Int, line: Int, indicator: Int) :
        GameObject(isOpened, grade, suit, column, line, indicator)
*/

class Card(var isOpened: Boolean,
           val grade: Int, val suit: Char, var column: Int, var line: Int, val indicator: Int) {

    private var texture = if (isOpened) Sprite(Texture("android/assets/${this.grade}${this.suit}.png"))
    else Sprite(Texture("android/assets/back.png"))

    private val screenWidth = Gdx.graphics.width.toFloat()
    private val screenHeight = Gdx.graphics.height.toFloat()

    private val cardWidth = screenWidth * 0.074f
    private val cardHeight = screenHeight * 0.15f

    val bounds = Rectangle(0f, 0f, cardWidth, cardHeight)

    fun switch() {
        isOpened = !isOpened
        texture = if (isOpened) Sprite(Texture("android/assets/${this.grade}${this.suit}.png"))
        else Sprite(Texture("android/assets/back.png"))
    }

    fun updateCoords() {
        if (this.column !in 0..9) {
            bounds.y = -100f
            bounds.x = -100f
        } else {
            bounds.y = screenHeight * 0.97f - cardHeight - line * 0.022f * screenHeight
            bounds.x = 0.024f * screenWidth + 0.098f * screenWidth * column
        }
    }

    fun updateMoving(moving: MutableList<Int>) {
        bounds.y = Gdx.graphics.height.toFloat() - Gdx.input.y -
                cardHeight * 0.92f - 0.022f * screenHeight * (moving.lastIndex - moving.indexOf(this.indicator))
        bounds.x = Gdx.input.x - cardWidth / 2
    }

    fun draw(batch: SpriteBatch) {
        if (bounds.x to bounds.y != -100f to -100f) {

            val obj = Sprite(texture)
            obj.setOrigin(cardWidth / 2f, cardHeight / 2f)
            obj.setSize(cardWidth, cardHeight)
            obj.setPosition(bounds.x, bounds.y)
            obj.draw(batch)
        }
    }
}
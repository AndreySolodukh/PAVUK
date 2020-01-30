package com.game.pavuk.objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle

abstract class GameObject(var isOpened: Boolean, val grade: Int, val suit: Char,
                 var column: Int, var line: Int, val indicator: Int) {

    private var texture = if (isOpened)
        Texture("android/assets/${this.grade}${this.suit}.png")
    else Texture("android/assets/back.png")

    private val screenWidth = Gdx.graphics.width.toFloat()
    private val screenHeight = Gdx.graphics.height.toFloat()

    val width = screenWidth * 0.074f
    val height = screenHeight * 0.15f

    val bounds = Rectangle(0f, 0f, width, height)

    fun switch() {
        isOpened = !isOpened
        texture = if (isOpened) Texture("android/assets/${this.grade}${this.suit}.png")
        else Texture("android/assets/back.png")
    }

    fun updateCoords() {
        if (this.column !in 0..9) {
            bounds.y = -100f
            bounds.x = -100f
        } else {
            bounds.y = screenHeight * 0.97f - height - line * 0.022f * screenHeight
            bounds.x = 0.024f * screenWidth + 0.098f * screenWidth * column
        }
    }

    fun updateMoving(moving: MutableList<Int>) {
        bounds.y = Gdx.graphics.height.toFloat() - Gdx.input.y -
                height * 0.92f - 0.022f * screenHeight * (moving.lastIndex - moving.indexOf(this.indicator))
        bounds.x = Gdx.input.x - width / 2
    }

    fun draw(batch: SpriteBatch) {
        if (bounds.x to bounds.y != -100f to -100f) {
            val obj = Sprite(texture)
            obj.setOrigin(width / 2f, height / 2f)
            obj.setSize(width, height)
            obj.setPosition(bounds.x, bounds.y)
            obj.draw(batch)
        }
    }

}
package com.game.pavuk.objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Rectangle

class Card(private var texture: Sprite, val width: Float, val height: Float, var isOpened: Boolean,
           val grade: Int, var column: Int, var line: Int, val indicator: Int) {

    val bounds = Rectangle(0f, 0f, width, height)
    private val screenWidth = Gdx.graphics.width.toFloat()
    private val screenHeight = Gdx.graphics.height.toFloat()

    fun switch() {
        isOpened = !isOpened
        texture = if (isOpened) Sprite(TextureAtlas("pack.atlas").findRegion("$grade"))
        else Sprite(TextureAtlas("pack.atlas").findRegion("back"))
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

    fun upgradeMoving(moving: MutableList<Int>) {
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
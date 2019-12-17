package com.game.pavuk.objects

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Rectangle


class Card(var texture: Sprite, val width: Float, val height: Float, var isOpened: Boolean,
           val grade: Int, val indicator: Int) {


    val bounds = Rectangle(0f, 0f, width, height)

    fun switch() {
        isOpened = !isOpened
        texture = if (isOpened) Sprite(TextureAtlas("pack.atlas").findRegion("$grade"))
        else Sprite(TextureAtlas("pack.atlas").findRegion("back"))
    }

    fun draw(batch: SpriteBatch) {
        val obj = Sprite(texture)
        obj.setOrigin(width / 2f, height / 2f)
        obj.setSize(width, height)
        obj.setPosition(bounds.x, bounds.y)
        obj.draw(batch)
    }
}
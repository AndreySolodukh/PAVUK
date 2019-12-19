package com.game.pavuk

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.game.pavuk.objects.Card

class Resource {

    private val atlas = TextureAtlas("pack.atlas")
    private val skin = Skin()
    val background = Texture("background.png")
    val batch = SpriteBatch()
    val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
    val generator = FreeTypeFontGenerator(Gdx.files.internal("pixel.ttf"))

    val width = Gdx.graphics.width.toFloat()
    val height = Gdx.graphics.height.toFloat()


    init {
        skin.addRegions(atlas)
        parameter.size = (height / 20).toInt()
        parameter.color = Color.WHITE
    }

    val deck = mutableListOf<Card>()
    val columns = setOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    var moving = mutableListOf<Int>()
    var from = -1
    var to = -1
    var start = true
    var victory = false
    var defeat = false
    var backup = 5
    var finished = 0
    var oldColumn = 0

    fun buildDeck() {
            val random = mutableListOf<Int>()
            for (i in 0..103)
                random.add(i % 13)
            for ((i, elem) in random.shuffled().withIndex()) {
                deck.add(Card(Sprite(atlas.findRegion("$elem")), width * 0.074f,
                        height * 0.15f, true, elem, -1, 0, i))
            }
    }

    fun dispose() {
        batch.dispose()
        atlas.dispose()
        generator.dispose()
    }
}
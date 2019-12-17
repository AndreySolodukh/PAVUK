package com.game.pavuk

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.ui.Skin

class Resource {

    val background = Texture("background.png")
    val batch = SpriteBatch()
    val atlas = TextureAtlas("pack.atlas")
    val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
    val generator = FreeTypeFontGenerator(Gdx.files.internal("pixel.ttf"))
    private val skin = Skin()

    init {
        skin.addRegions(atlas)
        parameter.size = 40
        parameter.color = Color.WHITE
    }

    val font: BitmapFont = generator.generateFont(parameter)

    val width = Gdx.graphics.width.toFloat()
    val height = Gdx.graphics.height.toFloat()

    fun dispose() {
        batch.dispose()
        atlas.dispose()
        generator.dispose()
        font.dispose()
    }
}
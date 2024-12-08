package com.simplemobiletools.musicplayer.new_architecture.shared.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.adjustAlpha
import com.simplemobiletools.musicplayer.new_architecture.shared.extensions.value
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.HIGHER_ALPHA
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.MEDIUM_ALPHA

class MyTextInputLayout : TextInputLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    // we need to use reflection to make some colors work well
    fun setColors(textColor: Int, accentColor: Int) {
        try {
            editText!!.setTextColor(textColor)
            editText!!.backgroundTintList = ColorStateList.valueOf(accentColor)

            val hintColor = if (editText!!.value.isEmpty()) textColor.adjustAlpha(HIGHER_ALPHA) else textColor
            val defaultTextColor = TextInputLayout::class.java.getDeclaredField("defaultHintTextColor")
            defaultTextColor.isAccessible = true
            defaultTextColor.set(this, ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(hintColor)))

            val focusedTextColor = TextInputLayout::class.java.getDeclaredField("focusedTextColor")
            focusedTextColor.isAccessible = true
            focusedTextColor.set(this, ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(accentColor)))

            val defaultHintTextColor = textColor.adjustAlpha(MEDIUM_ALPHA)
            val boxColorState = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_active),
                    intArrayOf(android.R.attr.state_focused)
                ),
                intArrayOf(
                    defaultHintTextColor,
                    accentColor
                )
            )

            setEndIconTintList(ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(hintColor)))
            setBoxStrokeColorStateList(boxColorState)
            defaultTextColor.set(this, ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(defaultHintTextColor)))
            setHelperTextColor(ColorStateList.valueOf(textColor))
        } catch (_: Exception) {
        }
    }
}
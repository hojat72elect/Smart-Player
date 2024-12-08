package com.simplemobiletools.musicplayer.new_architecture.shared

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.simplemobiletools.musicplayer.new_architecture.shared.data.models.Track

abstract class MyViewPagerFragment(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {
    abstract fun setupFragment(activity: BaseSimpleActivity)

    abstract fun finishActMode()

    abstract fun onSearchQueryChanged(text: String)

    abstract fun onSearchClosed()

    abstract fun onSortOpen(activity: BaseSimpleActivity)

    abstract fun setupColors(textColor: Int, adjustedPrimaryColor: Int)

    fun prepareAndPlay(tracks: List<Track>, startIndex: Int = 0, startPositionMs: Long = 0, startActivity: Boolean = true) {
        (context as SimpleControllerActivity).prepareAndPlay(tracks, startIndex, startPositionMs, startActivity)
    }
}
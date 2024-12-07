package com.simplemobiletools.musicplayer.views

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.auth.AuthPromptHost
import androidx.viewpager.widget.PagerAdapter
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.PROTECTION_FINGERPRINT
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.PROTECTION_PATTERN
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.PROTECTION_PIN
import com.simplemobiletools.musicplayer.new_architecture.shared.helpers.isRPlus
import com.simplemobiletools.musicplayer.interfaces.HashListener
import com.simplemobiletools.musicplayer.interfaces.SecurityTab
import com.simplemobiletools.musicplayer.R


class PasswordTypesAdapter(
    private val context: Context,
    private val requiredHash: String,
    private val hashListener: HashListener,
    private val scrollView: MyScrollView,
    private val biometricPromptHost: AuthPromptHost,
    private val showBiometricIdTab: Boolean,
    private val showBiometricAuthentication: Boolean
) : PagerAdapter() {
    private val tabs = SparseArray<SecurityTab>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(layoutSelection(position), container, false)
        container.addView(view)
        tabs.put(position, view as SecurityTab)
        (view as SecurityTab).initTab(requiredHash, hashListener, scrollView, biometricPromptHost, showBiometricAuthentication)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        tabs.remove(position)
        container.removeView(item as View)
    }

    override fun getCount() = if (showBiometricIdTab) 3 else 2

    override fun isViewFromObject(view: View, item: Any) = view == item

    private fun layoutSelection(position: Int): Int = when (position) {
        PROTECTION_PATTERN -> R.layout.tab_pattern
        PROTECTION_PIN -> R.layout.tab_pin
        PROTECTION_FINGERPRINT -> if (isRPlus()) R.layout.tab_biometric_id else R.layout.tab_fingerprint
        else -> throw RuntimeException("Only 3 tabs allowed")
    }

    fun isTabVisible(position: Int, isVisible: Boolean) {
        tabs[position]?.visibilityChanged(isVisible)
    }
}

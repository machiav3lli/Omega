/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.iconpack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.annotation.Keep
import androidx.preference.Preference
import com.android.launcher3.R
import com.saggitt.omega.settings.SettingsActivity
import com.saggitt.omega.settings.search.SearchIndex

class IconPackPreference(context: Context, attrs: AttributeSet? = null) : Preference(context, attrs) {

    private val ipm = IconPackManager.getInstance(context)
    private val packList = ipm.packList

    private val onChangeListener = ::updatePreview

    init {
        layoutResource = R.layout.preference_preview_icon
        fragment = IconPackFragment::class.java.name
        updatePreview()
    }

    override fun onAttached() {
        super.onAttached()

        ipm.addListener(onChangeListener)
    }

    override fun onDetached() {
        super.onDetached()

        ipm.removeListener(onChangeListener)
    }

    private fun updatePreview() {
        try {
            summary = if (packList.currentPack() is DefaultPack) {
                packList.currentPack().displayName
            } else {
                packList.appliedPacks
                        .filter { it !is DefaultPack }
                        .joinToString(", ") { it.displayName }
            }
            icon = packList.currentPack().displayIcon
        } catch (ignored: IllegalStateException) {
            //TODO: Fix updating pref when scrolled down
        }
    }

    class IconPackSlice(context: Context, attrs: AttributeSet) : SearchIndex.Slice(context, attrs) {

        override fun createSliceView(): View {
            return (View.inflate(context, R.layout.preview_icon, null) as ImageView).apply {
                IconPackManager.getInstance(context).addListener {
                    setImageDrawable(IconPackManager.getInstance(context).packList.currentPack().displayIcon)
                }
                setOnClickListener {
                    context.startActivity(Intent()
                            .setClass(context, SettingsActivity::class.java)
                            .putExtra(SettingsActivity.EXTRA_FRAGMENT, IconPackFragment::class.java.name)
                            .putExtra(SettingsActivity.EXTRA_FRAGMENT_ARGS, Bundle().apply {
                                putString(SettingsActivity.SubSettingsFragment.TITLE, context.getString(R.string.icon_pack))
                            })
                    )
                }
            }
        }
    }

    companion object {

        @Keep
        @JvmStatic
        val sliceProvider = SearchIndex.SliceProvider.fromLambda(::IconPackSlice)
    }
}

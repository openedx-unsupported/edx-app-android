package org.edx.mobile.extenstion

import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout

fun AppBarLayout.setTitleStateListener(
    collapsingToolbar: CollapsingToolbarLayout,
    listener: CollapsingToolbarStatListener
) {
    // Add an OnOffsetChangedListener to the AppBarLayout to detect the state of the toolbar
    // based on the collapse offset
    this.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
        private var isTitleCollapsed: Boolean = false
        override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
            val minHeight = ViewCompat.getMinimumHeight(collapsingToolbar)
            if (collapsingToolbar.height + verticalOffset <= minHeight) {
                // The toolbar is in collapsed state
                if (!isTitleCollapsed) {
                    listener.onCollapsed()
                    isTitleCollapsed = true
                }
            } else {
                // The toolbar is in expand state
                if (isTitleCollapsed) {
                    listener.onExpanded()
                    isTitleCollapsed = false
                }
            }
        }
    })
}

interface CollapsingToolbarStatListener {
    fun onExpanded()

    fun onCollapsed()
}

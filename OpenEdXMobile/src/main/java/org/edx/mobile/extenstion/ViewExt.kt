package org.edx.mobile.extenstion

import android.view.View

fun View.isVisible(): Boolean = this.visibility == View.VISIBLE

fun View.isInvisible(): Boolean = this.visibility == View.GONE

fun View.visibility(isVisible: Boolean) {
    this.visibility = if (isVisible) View.VISIBLE else View.GONE
}

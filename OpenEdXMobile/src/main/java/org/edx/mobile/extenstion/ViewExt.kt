package org.edx.mobile.extenstion

import android.view.View

fun View.isVisible(): Boolean = this.visibility == View.VISIBLE

fun View.isNotVisible(): Boolean = this.visibility == View.GONE

fun View.setVisibility(isVisible: Boolean) {
    this.visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun View.setInVisible(isInVisible: Boolean) {
    this.visibility = if (isInVisible) View.INVISIBLE else View.VISIBLE
}

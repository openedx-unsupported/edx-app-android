package org.edx.mobile.extenstion

import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat

fun View.isVisible(): Boolean = this.visibility == View.VISIBLE

fun View.isNotVisible(): Boolean = this.visibility == View.GONE

fun View.setVisibility(isVisible: Boolean) {
    this.visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun AppCompatImageView.setImageDrawable(@DrawableRes drawableId: Int) {
    ContextCompat.getDrawable(context, drawableId)
}

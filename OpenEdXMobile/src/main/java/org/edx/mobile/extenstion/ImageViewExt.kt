package org.edx.mobile.extenstion

import android.graphics.PorterDuff
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat

fun AppCompatImageView.setSrcColor(colorRes: Int) {
    this.setColorFilter(ContextCompat.getColor(context, colorRes), PorterDuff.Mode.SRC_IN)
}

fun AppCompatImageView.setImageDrawable(@DrawableRes drawableId: Int) {
    this.setImageDrawable(ContextCompat.getDrawable(context, drawableId))
}

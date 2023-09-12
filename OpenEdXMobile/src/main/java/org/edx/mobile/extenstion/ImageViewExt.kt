package org.edx.mobile.extenstion

import androidx.appcompat.widget.AppCompatImageView
import org.edx.mobile.util.UiUtils

fun AppCompatImageView.setSrcColor(colorRes: Int) {
    UiUtils.setImageViewColor(this.context, this, colorRes)
}
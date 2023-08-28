package org.edx.mobile.view.custom

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * Class to Implement Divider Item Decorator for [RecyclerView](androidx.recyclerview.widget.RecyclerView).
 *
 * This class is intended to facilitate the drawing of divider item decorators within the context of
 * a [RecyclerView](androidx.recyclerview.widget.RecyclerView). Its purpose is to address an issue
 * where [DividerItemDecoration](androidx.recyclerview.widget.DividerItemDecoration) fails to correctly
 * display dividers and show the divider for the final item in the [RecyclerView](androidx.recyclerview.widget.RecyclerView).
 * */
class DividerItemDecorator(private val divider: Drawable) : ItemDecoration() {
    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val dividerLeft = parent.paddingLeft
        val dividerRight = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0..childCount - 2) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val dividerTop = child.bottom + params.bottomMargin
            val dividerBottom = dividerTop + divider.intrinsicHeight
            divider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
            divider.draw(canvas)
        }
    }
}

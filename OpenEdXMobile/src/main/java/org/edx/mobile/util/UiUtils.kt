package org.edx.mobile.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import org.edx.mobile.R
import org.edx.mobile.base.MainApplication
import org.edx.mobile.logger.Logger
import org.edx.mobile.view.custom.SingleScrollDirectionEnforcer

object UiUtils {
    private val TAG = UiUtils::class.java.canonicalName
    private val logger = Logger(UiUtils::class.java)

    /**
     * This function is used to return the passed Value in Display Metrics form
     *
     * @param point width/height as int
     * @return float
     */
    fun getParamsInDP(r: Resources, point: Int): Float {
        try {
            return TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, point.toFloat(), r.displayMetrics
            )
        } catch (e: Exception) {
            logger.error(e)
        }
        return 0F
    }


    fun dpToPx(context: Context, dpValue: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dpValue * scale + 0.5f
    }

    /**
     * Utility method to check the screen direction
     *
     * @return true if direction is LTR false else wise
     */
    fun isDirectionLeftToRight(): Boolean {
        val config = MainApplication.instance().resources.configuration
        return config.layoutDirection == View.LAYOUT_DIRECTION_LTR
    }

    /**
     * CardView adds extra padding on pre-lollipop devices for shadows
     * This function removes that extra padding from its margins
     *
     * @param cardView The CardView that needs adjustments
     * @return float
     */
    fun adjustCardViewMargins(cardView: View) {
        val params = cardView.layoutParams as MarginLayoutParams
        params.topMargin -= cardView.paddingTop
        params.leftMargin -= cardView.paddingStart
        params.rightMargin -= cardView.paddingEnd
        cardView.layoutParams = params
    }

    fun getDrawable(context: Context, @DrawableRes iconResId: Int, resSize: Int) =
            getDrawable(context, iconResId, resSize, 0)

    fun getDrawable(context: Context, @DrawableRes iconResId: Int, resSize: Int, colorRes: Int): Drawable {
        val drawable = getDrawable(context, iconResId).mutate()
        if (colorRes != 0) setDrawableColor(context, drawable, colorRes)
        return if (resSize != 0) setDrawableSize(context, drawable, resSize) else drawable
    }

    private fun setDrawableColor(context: Context, drawable: Drawable, colorRes: Int) = drawable
            .setColorFilter(ContextCompat.getColor(context, colorRes), PorterDuff.Mode.SRC_IN)

    fun setImageViewColor(context: Context, imageView: AppCompatImageView, colorRes: Int) {
        imageView.setColorFilter(ContextCompat.getColor(context, colorRes), PorterDuff.Mode.SRC_IN)
    }

    private fun setDrawableSize(context: Context, drawable: Drawable, resSize: Int): Drawable {
        val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        val size = context.resources.getDimensionPixelSize(resSize)
        val dr = DrawableCompat.wrap(BitmapDrawable(context.resources, bitmap))
        dr.setBounds(0, 0, size, size)
        return dr
    }

    fun setAnimation(imageView: ImageView, animation: Animation) {
        if (animation == Animation.NONE) {
            imageView.animation = null
            return
        }
        val anim = RotateAnimation(
                0.0f,
                360.0f,
                android.view.animation.Animation.RELATIVE_TO_SELF,
                0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF,
                0.5f
        )
        anim.interpolator = LinearInterpolator()
        anim.repeatCount = android.view.animation.Animation.INFINITE
        anim.duration = 1000
        imageView.startAnimation(anim)
    }

    fun setTextViewDrawableStart(context: Context, textView: TextView, @DrawableRes iconResId: Int, resSize: Int) =
            setTextViewDrawableStart(context, textView, iconResId, resSize, 0)

    fun setTextViewDrawableStart(context: Context, textView: TextView, @DrawableRes iconResId: Int, resSize: Int, colorRes: Int) =
            textView.setCompoundDrawables(getDrawable(context, iconResId, resSize, colorRes), null, null, null)

    fun setTextViewDrawableEnd(context: Context, textView: TextView, @DrawableRes iconResId: Int, resSize: Int) =
            setTextViewDrawableEnd(context, textView, iconResId, resSize, 0)

    fun setTextViewDrawableEnd(context: Context, textView: TextView, @DrawableRes iconResId: Int, resSize: Int, colorRes: Int) =
            textView.setCompoundDrawables(null, null, getDrawable(context, iconResId, resSize, colorRes), null)

    fun getDrawable(context: Context, @DrawableRes drawableId: Int): Drawable {
        return ContextCompat.getDrawable(context, drawableId)!!
    }

    @DrawableRes
    fun getDrawable(context: Context, drawableName: String): Int {
        return context.resources.getIdentifier(
                drawableName, "drawable",
                context.packageName
        )
    }

    /**
     * Sets the color scheme of the provided [SwipeRefreshLayout].
     *
     * @param swipeRefreshLayout The SwipeRefreshLayout to set the color scheme of.
     */
    fun setSwipeRefreshLayoutColors(swipeRefreshLayout: SwipeRefreshLayout) {
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primaryDarkColor,
                R.color.neutralBlack
        )
    }

    /**
     * Restarts the fragment without destroying the fragment instance.
     *
     * @param fragment The fragment to restart.
     */
    fun restartFragment(fragment: Fragment?) {
        fragment?.requireFragmentManager()?.beginTransaction()?.detach(fragment)?.attach(fragment)
                ?.commitAllowingStateLoss()
    }

    /**
     * Method to remove the child [Fragment] against the provided tag.
     *
     * @param parentFragment [Fragment] that containing the child [Fragment]
     * @param tag            string to search the fragment.
     */
    fun removeFragmentByTag(parentFragment: Fragment, tag: String) {
        if (parentFragment.isAdded) {
            val fragmentManager = parentFragment.childFragmentManager
            val fragment = fragmentManager.findFragmentByTag(tag)
            fragment?.let {
                fragmentManager.beginTransaction().remove(fragment)
                        .commitAllowingStateLoss()
            }
        }
    }

    /**
     * Util method to enforce the single scrolling direction gesture of the [ViewPager2], as
     * [ViewPager2] supports both vertical & horizontally scrolling gestures that disrupt the
     * the child list scrolling when user gesture not perfectly in single direction (e.g diagonal
     * gesture)
     *
     * @param viewPager2 view to enforce the single scrolling direction
     * @see [
     * Nested scrolling in Opposite Direction using ViewPager2](https://medium.com/@BladeCoder/fixing-recyclerview-nested-scrolling-in-opposite-direction-f587be5c1a04)
     */
    fun enforceSingleScrollDirection(viewPager2: ViewPager2) {
        // ViewPager2 uses a RecyclerView internally.
        val recyclerView = viewPager2.getChildAt(0) as RecyclerView?
        recyclerView?.let {
            val enforcer = SingleScrollDirectionEnforcer()
            recyclerView.addOnItemTouchListener(enforcer)
            recyclerView.addOnScrollListener(enforcer)
        }
    }

    enum class Animation(val value: String) {
        ROTATION("rotation"), NONE("none")
    }
}

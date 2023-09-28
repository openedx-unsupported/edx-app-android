package org.edx.mobile.view.custom

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import kotlin.math.abs

/**
 * A custom WebView implementation that implements the NestedScrollingChild interface
 * to enable nested scrolling behavior. This class allows the WebView to interact with
 * parent views that support nested scrolling.
 *
 * This class provides support for nested scrolling gestures and should be used when
 * you need a WebView within a nested scrolling container, such as a CoordinatorLayout or RecyclerView.
 *
 * Inspiration: [NestedScrollWebView implementation](https://stackoverflow.com/a/57726095)
 */
class NestedScrollWebView(context: Context?, attrs: AttributeSet?) : EdxWebView(context, attrs),
    NestedScrollingChild3 {
    private val scrollOffset = IntArray(2)
    private val scrollConsumed = IntArray(2)
    private var lastMotionY = 0
    private val childHelper: NestedScrollingChildHelper
    private var isBeingDragged = false
    private var velocityTracker: VelocityTracker? = null
    private var touchSlop = 0
    private var activePointerId = INVALID_POINTER
    private var nestedYOffset = 0
    private var scroller: OverScroller? = null
    private var minimumVelocity = 0
    private var maximumVelocity = 0
    private var lastScrollerY = 0

    init {
        overScrollMode = OVER_SCROLL_NEVER
        initScrollView()
        childHelper = NestedScrollingChildHelper(this)
        isNestedScrollingEnabled = true
    }

    private fun initScrollView() {
        scroller = OverScroller(context)
        val configuration = ViewConfiguration.get(
            context
        )
        touchSlop = configuration.scaledTouchSlop
        minimumVelocity = configuration.scaledMinimumFlingVelocity
        maximumVelocity = configuration.scaledMaximumFlingVelocity
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_MOVE && isBeingDragged) { // most common
            return true
        }
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE -> run {
                val activePointerId = activePointerId
                if (activePointerId == INVALID_POINTER) {
                    return@run
                }
                val pointerIndex = event.findPointerIndex(activePointerId)
                if (pointerIndex == -1) {
                    return@run
                }
                val y = event.getY(pointerIndex).toInt()
                val yDiff = abs(y - lastMotionY)
                if (yDiff > touchSlop && nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL == 0) {
                    isBeingDragged = true
                    lastMotionY = y
                    initVelocityTrackerIfNotExists()
                    velocityTracker?.addMovement(event)
                    nestedYOffset = 0
                    val parent = parent
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
            }

            MotionEvent.ACTION_DOWN -> {
                lastMotionY = event.y.toInt()
                activePointerId = event.getPointerId(0)
                initOrResetVelocityTracker()
                velocityTracker?.addMovement(event)
                scroller?.computeScrollOffset()
                isBeingDragged = scroller?.isFinished == false
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                isBeingDragged = false
                activePointerId = INVALID_POINTER
                recycleVelocityTracker()
                if (scroller?.springBack(scrollX, scrollY, 0, 0, 0, scrollRange) == true) {
                    ViewCompat.postInvalidateOnAnimation(this)
                }
                stopNestedScroll()
            }

            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(event)
        }
        return isBeingDragged
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        initVelocityTrackerIfNotExists()
        val motionEvent = MotionEvent.obtain(ev)
        val actionMasked = ev.actionMasked
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            nestedYOffset = 0
        }
        motionEvent.offsetLocation(0f, nestedYOffset.toFloat())
        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (isBeingDragged != scroller?.isFinished) {
                    val parent = parent
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
                if (scroller?.isFinished == false) {
                    abortAnimatedScroll()
                }
                lastMotionY = ev.y.toInt()
                activePointerId = ev.getPointerId(0)
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
            }

            MotionEvent.ACTION_MOVE -> run {
                val activePointerIndex = ev.findPointerIndex(activePointerId)
                if (activePointerIndex == -1) {
                    return@run
                }
                val y = ev.getY(activePointerIndex).toInt()
                var deltaY = lastMotionY - y
                if (dispatchNestedPreScroll(
                        0, deltaY, scrollConsumed, scrollOffset, ViewCompat.TYPE_TOUCH
                    )
                ) {
                    deltaY -= scrollConsumed[1]
                    nestedYOffset += scrollOffset[1]
                }
                if (!isBeingDragged && abs(deltaY) > touchSlop) {
                    val parent = parent
                    parent?.requestDisallowInterceptTouchEvent(true)
                    isBeingDragged = true
                    if (deltaY > 0) {
                        deltaY -= touchSlop
                    } else {
                        deltaY += touchSlop
                    }
                }
                if (isBeingDragged) {
                    lastMotionY = y - scrollOffset[1]
                    val oldY = scrollY
                    val range = scrollRange
                    // Calling overScrollByCompat will call onOverScrolled, which
                    // calls onScrollChanged if applicable.
                    if (overScrollByCompat(
                            0, deltaY, 0, oldY, 0, range, 0, 0
                        ) && !hasNestedScrollingParent(ViewCompat.TYPE_TOUCH)
                    ) {
                        velocityTracker?.clear()
                    }
                    val scrolledDeltaY = scrollY - oldY
                    val unconsumedY = deltaY - scrolledDeltaY
                    scrollConsumed[1] = 0
                    dispatchNestedScroll(
                        0,
                        scrolledDeltaY,
                        0,
                        unconsumedY,
                        scrollOffset,
                        ViewCompat.TYPE_TOUCH,
                        scrollConsumed
                    )
                    lastMotionY -= scrollOffset[1]
                    nestedYOffset += scrollOffset[1]
                }
            }

            MotionEvent.ACTION_UP -> {
                val initialVelocity = velocityTracker?.let { velocityTracker ->
                    velocityTracker.computeCurrentVelocity(1000, maximumVelocity.toFloat())
                    velocityTracker.getYVelocity(activePointerId).toInt()
                } ?: run {
                    0
                }
                if (abs(initialVelocity) > minimumVelocity) {
                    if (!dispatchNestedPreFling(0f, -initialVelocity.toFloat())) {
                        dispatchNestedFling(0f, -initialVelocity.toFloat(), true)
                        fling(-initialVelocity)
                    }
                } else if (scroller?.springBack(scrollX, scrollY, 0, 0, 0, scrollRange) == true) {
                    ViewCompat.postInvalidateOnAnimation(this)
                }
                activePointerId = INVALID_POINTER
                endDrag()
            }

            MotionEvent.ACTION_CANCEL -> {
                if (isBeingDragged) {
                    if (scroller?.springBack(scrollX, scrollY, 0, 0, 0, scrollRange) == true) {
                        ViewCompat.postInvalidateOnAnimation(this)
                    }
                }
                activePointerId = INVALID_POINTER
                endDrag()
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = ev.actionIndex
                lastMotionY = ev.getY(index).toInt()
                activePointerId = ev.getPointerId(index)
            }

            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(ev)
                lastMotionY = ev.getY(ev.findPointerIndex(activePointerId)).toInt()
            }
        }
        if (velocityTracker != null) {
            velocityTracker?.addMovement(motionEvent)
        }
        motionEvent.recycle()
        return super.onTouchEvent(ev)
    }

    private fun abortAnimatedScroll() {
        scroller?.abortAnimation()
        stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
    }

    private fun endDrag() {
        isBeingDragged = false
        recycleVelocityTracker()
        stopNestedScroll()
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex =
            ev.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == activePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            lastMotionY = ev.getY(newPointerIndex).toInt()
            activePointerId = ev.getPointerId(newPointerIndex)
            if (velocityTracker != null) {
                velocityTracker?.clear()
            }
        }
    }

    private fun fling(velocityY: Int) {
        val height = height
        scroller?.fling(
            scrollX, scrollY,  // start
            0, velocityY,  // velocities
            0, 0, Int.MIN_VALUE, Int.MAX_VALUE,  // y
            0, height / 2
        )
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH)
        lastScrollerY = scrollY
        ViewCompat.postInvalidateOnAnimation(this)
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        if (disallowIntercept) {
            recycleVelocityTracker()
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    private fun initOrResetVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        } else {
            velocityTracker?.clear()
        }
    }

    private fun initVelocityTrackerIfNotExists() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
    }

    private fun recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker?.recycle()
            velocityTracker = null
        }
    }

    override fun overScrollBy(
        deltaX: Int,
        deltaY: Int,
        scrollX: Int,
        scrollY: Int,
        scrollRangeX: Int,
        scrollRangeY: Int,
        maxOverScrollX: Int,
        maxOverScrollY: Int,
        isTouchEvent: Boolean
    ): Boolean {
        // this is causing double scroll call (doubled speed), but this WebView isn't over scrollable
        // all over scrolls are passed to appbar, so commenting this out during drag
        if (!isBeingDragged) overScrollByCompat(
            deltaX,
            deltaY,
            scrollX,
            scrollY,
            scrollRangeX,
            scrollRangeY,
            maxOverScrollX,
            maxOverScrollY
        )
        // without this call WebView won't scroll to top when url change or when user pick input
        // (WebView should move a bit making input still in viewport when "adjustResize")
        return true
    }

    //Using scroll range of WebView instead of children as NestedScrollView does.
    private val scrollRange: Int
        get() = computeVerticalScrollRange()

    override fun isNestedScrollingEnabled(): Boolean {
        return childHelper.isNestedScrollingEnabled
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        childHelper.isNestedScrollingEnabled = enabled
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return childHelper.startNestedScroll(axes, type)
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return startNestedScroll(axes, ViewCompat.TYPE_TOUCH)
    }

    override fun stopNestedScroll(type: Int) {
        childHelper.stopNestedScroll(type)
    }

    override fun stopNestedScroll() {
        stopNestedScroll(ViewCompat.TYPE_TOUCH)
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        return childHelper.hasNestedScrollingParent(type)
    }

    override fun hasNestedScrollingParent(): Boolean {
        return hasNestedScrollingParent(ViewCompat.TYPE_TOUCH)
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?
    ): Boolean {
        return dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            ViewCompat.TYPE_TOUCH
        )
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return childHelper.dispatchNestedScroll(
            dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type
        )
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int,
        consumed: IntArray
    ) {
        childHelper.dispatchNestedScroll(
            dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?
    ): Boolean {
        return dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, ViewCompat.TYPE_TOUCH)
    }

    override fun dispatchNestedPreScroll(
        dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?, type: Int
    ): Boolean {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    override fun dispatchNestedFling(
        velocityX: Float, velocityY: Float, consumed: Boolean
    ): Boolean {
        return childHelper.dispatchNestedFling(velocityX, velocityY, false)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun getNestedScrollAxes(): Int {
        return ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun computeScroll() {
        if (scroller?.isFinished == true) {
            return
        }
        scroller?.computeScrollOffset()
        val y = scroller?.currY
        var unconsumed = 0
        y?.run {
            unconsumed = this - lastScrollerY
            lastScrollerY = this
        }

        // Nested Scrolling Pre Pass
        scrollConsumed[1] = 0
        dispatchNestedPreScroll(0, unconsumed, scrollConsumed, null, ViewCompat.TYPE_NON_TOUCH)
        unconsumed -= scrollConsumed[1]
        if (unconsumed != 0) {
            // Internal Scroll
            val oldScrollY = scrollY
            overScrollByCompat(0, unconsumed, scrollX, oldScrollY, 0, scrollRange, 0, 0)
            val scrolledByMe = scrollY - oldScrollY
            unconsumed -= scrolledByMe

            // Nested Scrolling Post Pass
            scrollConsumed[1] = 0
            dispatchNestedScroll(
                0, 0, 0, unconsumed, scrollOffset, ViewCompat.TYPE_NON_TOUCH, scrollConsumed
            )
            unconsumed -= scrollConsumed[1]
        }
        if (unconsumed != 0) {
            abortAnimatedScroll()
        }
        if (scroller?.isFinished == false) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    // copied from NestedScrollView exactly as it looks, leaving overscroll related code, maybe future use
    private fun overScrollByCompat(
        deltaX: Int,
        deltaY: Int,
        scrollX: Int,
        scrollY: Int,
        scrollRangeX: Int,
        scrollRangeY: Int,
        maxOverScrollX: Int,
        maxOverScrollY: Int
    ): Boolean {
        var calculateMaxOverScrollX = maxOverScrollX
        var calculateMaxOverScrollY = maxOverScrollY
        val overScrollMode = overScrollMode
        val canScrollHorizontal = computeHorizontalScrollRange() > computeHorizontalScrollExtent()
        val canScrollVertical = computeVerticalScrollRange() > computeVerticalScrollExtent()
        val overScrollHorizontal =
            overScrollMode == OVER_SCROLL_ALWAYS || overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollHorizontal
        val overScrollVertical =
            overScrollMode == OVER_SCROLL_ALWAYS || overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollVertical
        var newScrollX = scrollX + deltaX
        if (!overScrollHorizontal) {
            calculateMaxOverScrollX = 0
        }
        var newScrollY = scrollY + deltaY
        if (!overScrollVertical) {
            calculateMaxOverScrollY = 0
        }

        // Clamp values if at the limits and record
        val left = -calculateMaxOverScrollX
        val right = calculateMaxOverScrollX + scrollRangeX
        val top = -calculateMaxOverScrollY
        val bottom = calculateMaxOverScrollY + scrollRangeY
        var clampedX = false
        if (newScrollX > right) {
            newScrollX = right
            clampedX = true
        } else if (newScrollX < left) {
            newScrollX = left
            clampedX = true
        }
        var clampedY = false
        if (newScrollY > bottom) {
            newScrollY = bottom
            clampedY = true
        } else if (newScrollY < top) {
            newScrollY = top
            clampedY = true
        }
        if (clampedY && !hasNestedScrollingParent(ViewCompat.TYPE_NON_TOUCH)) {
            scroller?.springBack(newScrollX, newScrollY, 0, 0, 0, scrollRange)
        }
        onOverScrolled(newScrollX, newScrollY, clampedX, clampedY)
        return clampedX || clampedY
    }

    companion object {
        private const val INVALID_POINTER = -1
    }
}

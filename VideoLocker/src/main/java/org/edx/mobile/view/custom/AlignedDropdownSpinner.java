package org.edx.mobile.view.custom;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.CheckResult;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;
import android.widget.Spinner;

import org.edx.mobile.R;
import org.edx.mobile.util.DrawableUtils;

/**
 * A {@link Spinner} which aligns the dropdown to it's entire
 * area, including the indicator area. This is done by having
 * the indicator separately defined as part of the content,
 * instead of it being part of the background with padding
 * to exclude it from the content. Since the dropdown aligns
 * it's content area to the Spinner's content area, this
 * allows it to fully match the Spinner's width, contingent
 * of them defining similar borders and paddings.
 *
 * It also provides an 'active' state for both the background
 * and the indicator, based on whether the dropdown is being
 * displayed or not.
 */
@UiThread
public class AlignedDropdownSpinner extends StatefulSpinner {
    @Nullable
    private Drawable dropdownIndicator;
    @Nullable
    private ColorStateList dropdownIndicatorTintList;
    @Nullable
    private PorterDuff.Mode dropdownIndicatorTintMode;
    private boolean hasDropdownIndicatorTint, hasDropdodwIndicatorTintMode;

    /**
     * Construct a new spinner with the given context's theme.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public AlignedDropdownSpinner(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Construct a new spinner with the given context's theme and the supplied
     * mode of displaying choices. <code>mode</code> may be one of
     * {@link #MODE_DIALOG} or {@link #MODE_DROPDOWN}.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param mode    Constant describing how the user will select choices from the spinner.
     * @see #MODE_DIALOG
     * @see #MODE_DROPDOWN
     */
    public AlignedDropdownSpinner(@NonNull Context context, @Mode int mode) {
        this(context, null, android.R.attr.dropDownSpinnerStyle, mode);
    }

    /**
     * Construct a new spinner with the given context's theme and the supplied attribute set.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public AlignedDropdownSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.dropDownSpinnerStyle);
    }

    /**
     * Construct a new spinner with the given context's theme, the supplied attribute set,
     * and default style attribute.
     *
     * @param context      The Context the view is running in, through which it can
     *                     access the current theme, resources, etc.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     */
    public AlignedDropdownSpinner(@NonNull Context context, @Nullable AttributeSet attrs,
                                  @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, MODE_THEME);
    }

    /**
     * Construct a new spinner with the given context's theme, the supplied attribute set,
     * and default style. <code>mode</code> may be one of {@link #MODE_DIALOG} or
     * {@link #MODE_DROPDOWN} and determines how the user will select choices from the spinner.
     *
     * @param context      The Context the view is running in, through which it can
     *                     access the current theme, resources, etc.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     * @param mode         Constant describing how the user will select choices from the spinner.
     * @see #MODE_DIALOG
     * @see #MODE_DROPDOWN
     */
    public AlignedDropdownSpinner(@NonNull Context context, @Nullable AttributeSet attrs,
                                  @AttrRes int defStyleAttr, @Mode int mode) {
        this(context, attrs, defStyleAttr, mode, null);
    }

    /**
     * Constructs a new spinner with the given context's theme, the supplied
     * attribute set, default styles, popup mode (one of {@link #MODE_DIALOG}
     * or {@link #MODE_DROPDOWN}), and the context against which the popup
     * should be inflated.
     *
     * @param context      The context against which the view is inflated, which
     *                     provides access to the current theme, resources, etc.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default
     *                     values for the view. Can be 0 to not look for
     *                     defaults.
     * @param mode         Constant describing how the user will select choices from
     *                     the spinner.
     * @param popupTheme   The theme against which the dialog or dropdown popup
     *                     should be inflated. May be {@code null} to use the
     *                     view theme. If set, this will override any value
     *                     specified by {@link R.styleable#Spinner_popupTheme}.
     * @see #MODE_DIALOG
     * @see #MODE_DROPDOWN
     */
    public AlignedDropdownSpinner(@NonNull Context context, @Nullable AttributeSet attrs,
                                  @AttrRes int defStyleAttr, @Mode int mode,
                                  @Nullable Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, mode, popupTheme);

        final TintTypedArray a = TintTypedArray.obtainStyledAttributes(
                TintContextWrapper.wrap(context), attrs,
                R.styleable.AlignedDropdownSpinner, defStyleAttr, 0);

        if (a.hasValue(R.styleable.AlignedDropdownSpinner_dropDownIndicatorTintMode)) {
            dropdownIndicatorTintMode = DrawableUtils.parseTintMode(a.getInt(
                    R.styleable.AlignedDropdownSpinner_dropDownIndicatorTintMode, -1), null);
            hasDropdodwIndicatorTintMode = true;
        }

        if (a.hasValue(R.styleable.AlignedDropdownSpinner_dropDownIndicatorTint)) {
            dropdownIndicatorTintList = a.getColorStateList(
                    R.styleable.AlignedDropdownSpinner_dropDownIndicatorTint);
            hasDropdownIndicatorTint = true;
        }

        Drawable indicator = a.getDrawable(R.styleable.AlignedDropdownSpinner_dropDownIndicator);
        if (indicator != null) {
            setDropdownIndicator(indicator);
        }

        a.recycle();
    }

    /**
     * Sets a drawable as the dropdown indicator given its resource
     * identifier.
     *
     * @param indicatorResId The resource identifier of the drawable
     * @attr ref R.styleable#AlignedDropdownSpinner_dropDownIndicator
     */
    public void setDropdownIndicator(@DrawableRes int indicatorResId) {
        setDropdownIndicator(indicatorResId == 0 ? null :
                ContextCompat.getDrawable(getContext(), indicatorResId));
    }

    /**
     * Sets a drawable as the dropdown indicator.
     *
     * @param indicator The drawable to set
     * @attr ref R.styleable#AlignedDropdownSpinner_dropDownIndicator
     */
    public void setDropdownIndicator(@Nullable Drawable indicator) {
        if (dropdownIndicator != indicator) {
            final int oldIndicatorWidth, oldIndicatorHeight;
            if (dropdownIndicator == null) {
                oldIndicatorWidth = 0;
                oldIndicatorHeight = 0;
            } else {
                Rect bounds = dropdownIndicator.getBounds();
                oldIndicatorWidth = bounds.right;
                oldIndicatorHeight = bounds.bottom;
                dropdownIndicator.setCallback(null);
                unscheduleDrawable(dropdownIndicator);
            }

            dropdownIndicator = indicator;

            if (indicator != null) {
                indicator.setCallback(this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    DrawableCompat.setLayoutDirection(indicator, getLayoutDirection());
                }
                if (indicator.isStateful()) {
                    indicator.setState(getDrawableState());
                }
                indicator.setVisible(getVisibility() == VISIBLE, false);
                applyIndicatorTint();
                final int indicatorWidth = indicator.getIntrinsicWidth();
                final int indicatorHeight = indicator.getIntrinsicHeight();
                indicator.setBounds(0, 0, indicatorWidth, indicatorHeight);
                if (indicatorWidth != oldIndicatorWidth || indicatorHeight != oldIndicatorHeight) {
                    requestLayout();
                }
                invalidate();
            }
        }
    }

    /**
     * @return the drawable used as the dropdown indicator.
     * @see #setDropdownIndicator(Drawable)
     * @see #setDropdownIndicator(int)
     */
    @CheckResult
    @Nullable
    public Drawable getDropdownIndicator() {
        return dropdownIndicator == null ? null : DrawableCompat.unwrap(dropdownIndicator);
    }

    /**
     * Applies a tint to the dropdown indicator drawable. Does not modify the
     * current tint mode, which is {@link PorterDuff.Mode#SRC_IN} by default.
     * <p>
     * Subsequent calls to {@link #setDropdownIndicator(Drawable)} will
     * automatically mutate the drawable and apply the specified tint and tint
     * mode using {@link Drawable#setTintList(ColorStateList)}.
     *
     * @param tint The tint to apply, may be {@code null} to clear tint.
     * @attr ref R.styleable#AlignedDropdownSpinner_dropDownIndicatorTint
     * @see #getDropdownIndicatorTintList()
     * @see Drawable#setTintList(ColorStateList)
     */
    public void setDropdownIndicatorTintList(@Nullable ColorStateList tint) {
        dropdownIndicatorTintList = tint;
        hasDropdownIndicatorTint = true;

        applyIndicatorTint();
    }

    /**
     * @return The tint applied to the dropdown indicator drawable.
     * @attr ref R.styleable#AlignedDropdownSpinner_dropDownIndicatorTint
     * @see #setDropdownIndicatorTintList(ColorStateList)
     */
    @CheckResult
    @Nullable
    public ColorStateList getDropdownIndicatorTintList() {
        return dropdownIndicatorTintList;
    }

    /**
     * Specifies the blending mode used to apply the tint specified by
     * {@link #setDropdownIndicatorTintList(ColorStateList)}} to the dropdown
     * indicator drawable. The default mode is {@link PorterDuff.Mode#SRC_IN}.
     *
     * @param tintMode The blending mode used to apply the tint, may be
     *                 {@code null} to clear tint.
     * @attr ref R.styleable#AlignedDropdownSpinner_dropDownIndicatorTintMode
     * @see #getDropdownIndicatorTintMode()
     * @see Drawable#setTintMode(PorterDuff.Mode)
     */
    public void setDropdownIndicatorTintMode(@Nullable PorterDuff.Mode tintMode) {
        dropdownIndicatorTintMode = tintMode;
        hasDropdodwIndicatorTintMode = true;

        applyIndicatorTint();
    }

    /**
     * @return The blending mode used to apply the tint to the dropdown
     *         indicator drawable.
     * @attr ref R.styleable#AlignedDropdownSpinner_dropDownIndicatorTintMode
     * @see #setDropdownIndicatorTintMode(PorterDuff.Mode)
     */
    @CheckResult
    @Nullable
    public PorterDuff.Mode getDropdownIndicatorTintMode() {
        return dropdownIndicatorTintMode;
    }

    private void applyIndicatorTint() {
        if (dropdownIndicator != null &&
                (hasDropdownIndicatorTint || hasDropdodwIndicatorTintMode)) {
            dropdownIndicator = DrawableCompat.wrap(dropdownIndicator).mutate();
            if (hasDropdownIndicatorTint) {
                DrawableCompat.setTintList(dropdownIndicator, dropdownIndicatorTintList);
            }
            if (hasDropdodwIndicatorTintMode) {
                DrawableCompat.setTintMode(dropdownIndicator, dropdownIndicatorTintMode);
            }
            // The drawable (or one of its children) may not have been
            // stateful before applying the tint, so let's try again.
            if (dropdownIndicator.isStateful()) {
                dropdownIndicator.setState(getDrawableState());
            }
        }
    }

    @Override
    @CheckResult
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == dropdownIndicator;
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (dropdownIndicator != null) {
            dropdownIndicator.jumpToCurrentState();
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (dropdownIndicator != null && dropdownIndicator.isStateful()) {
            dropdownIndicator.setState(getDrawableState());
            invalidate();
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void drawableHotspotChanged(@FloatRange(from = 0) float x,
                                       @FloatRange(from = 0) float y) {
        super.drawableHotspotChanged(x, y);
        if (dropdownIndicator != null && dropdownIndicator.isStateful()) {
            dropdownIndicator.setHotspot(x, y);
        }
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        if (dropdownIndicator != null) {
            DrawableCompat.setLayoutDirection(dropdownIndicator, layoutDirection);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dropdownIndicator != null) {
            final Rect bounds = dropdownIndicator.getBounds();
            final int paddingTop = getPaddingTop();
            final int paddingBottom = getPaddingBottom();
            final int paddingEnd = ViewCompat.getPaddingEnd(this);
            final int top = paddingTop + ((getHeight() -
                    paddingBottom - paddingTop - bounds.bottom) / 2);
            final int left = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
                    ? paddingEnd : getWidth() - paddingEnd - bounds.right;
            canvas.save();
            canvas.translate(left, top);
            dropdownIndicator.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (dropdownIndicator == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            /*
             * Provide modified width measure specs to the super implementation,
             * leaving space for the dropdown indicator. This will also limit the
             * layout to this scope, since the Spinner implementation caches these
             * specs and passes them on to the children during layout. This relies
             * on the current implementation, but there doesn't seem to be any
             * other straightforward way to get the Spinner implementation to leave
             * space for the indicator other than using padding, and avoiding that
             * is the whole point of this class.
             */
            final int dropdownIndicatorWidth = dropdownIndicator.getBounds().right;
            final int widthMeasureSpecMod;
            final int widthMeasureSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            final int widthMeasureSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            if (widthMeasureSpecMode == MeasureSpec.UNSPECIFIED) {
                widthMeasureSpecMod = widthMeasureSpec;
            } else {
                widthMeasureSpecMod = MeasureSpec.makeMeasureSpec(
                        Math.max(0, widthMeasureSpecSize - dropdownIndicatorWidth),
                        widthMeasureSpecMode);
            }
            super.onMeasure(widthMeasureSpecMod, heightMeasureSpec);
            int measuredWidthAndState = getMeasuredWidthAndState();
            int measuredWidth = measuredWidthAndState & MEASURED_SIZE_MASK;
            switch (widthMeasureSpecMode) {
                case MeasureSpec.UNSPECIFIED:
                    measuredWidth += dropdownIndicatorWidth;
                    break;
                case MeasureSpec.EXACTLY:
                    measuredWidth = widthMeasureSpecSize;
                    break;
                case MeasureSpec.AT_MOST:
                    measuredWidth = Math.min(widthMeasureSpecSize,
                            measuredWidth + dropdownIndicatorWidth);
                    break;
            }
            measuredWidthAndState = measuredWidth | (measuredWidthAndState & MEASURED_STATE_MASK);

            int measuredHeightAndState = getMeasuredHeightAndState();
            final int indicatorHeightWithPadding = getPaddingTop() +
                    getPaddingBottom() + dropdownIndicator.getBounds().bottom;
            if (indicatorHeightWithPadding > (measuredHeightAndState & MEASURED_SIZE_MASK)) {
                measuredHeightAndState = resolveSizeAndState(
                        indicatorHeightWithPadding, heightMeasureSpec, 0);
            }

            setMeasuredDimension(measuredWidthAndState, measuredHeightAndState);
        }
    }
}

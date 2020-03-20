package com.joanzapata.iconify;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextPaint;
import android.util.LayoutDirection;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.View;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.view.View.LAYOUT_DIRECTION_RTL;

/**
 * Embed an icon into a Drawable that can be used as TextView icons, or ActionBar icons.
 * <pre>
 *     new IconDrawable(context, IconValue.icon_star)
 *           .colorRes(R.color.white)
 *           .actionBarSize();
 * </pre>
 * If you don't set the size of the drawable, it will use the size
 * that is given to him. Note that in an ActionBar, if you don't
 * set the size explicitly it uses 0, so please use actionBarSize().
 */
public final class IconDrawable extends Drawable implements Animatable {
    private static final int DEFAULT_COLOR = Color.BLACK;
    // Set the default tint to make it half translucent on disabled state.
    private static final PorterDuff.Mode DEFAULT_TINT_MODE = PorterDuff.Mode.MULTIPLY;
    private static final ColorStateList DEFAULT_TINT = new ColorStateList(
            new int[][] { { -android.R.attr.state_enabled }, StateSet.WILD_CARD },
            new int[] { 0x80FFFFFF, 0xFFFFFFFF }
    );
    private static final int ROTATION_DURATION = 600;
    // Font Awesome uses 8-step rotation for pulse, and
    // it seems to have the only pulsing spinner. If
    // spinners with different pulses are introduced at
    // some point, then a pulse property can be
    // implemented for the icons.
    private static final int ROTATION_PULSES = 8;
    private static final int ROTATION_PULSE_DURATION = ROTATION_DURATION / ROTATION_PULSES;
    private static final int ANDROID_ACTIONBAR_ICON_SIZE_DP = 24;
    private static final Rect TEMP_DRAW_BOUNDS = new Rect();

    @NonNull
    private IconState iconState;
    private final TextPaint paint;
    @ColorInt
    private int color;
    @Nullable
    private ColorFilter tintFilter;
    @ColorInt
    private int tintColor;
    @IntRange(from = -1)
    private long spinStartTime = -1;
    private boolean mMutated;
    @NonNull
    private final String text;
    @NonNull
    private final Rect drawBounds = new Rect();
    private float centerX, centerY;
    @Nullable
    private Runnable invalidateRunnable;

    @CheckResult
    @NonNull
    private static Icon findValidIconForKey(@NonNull String iconKey) {
        Icon icon = Iconify.findIconForKey(iconKey);
        if (icon == null) {
            throw new IllegalArgumentException("No icon found with key \"" + iconKey + "\".");
        }
        return icon;
    }

    @NonNull
    private static Icon validateIcon(@NonNull Icon icon) {
        if (Iconify.findTypefaceOf(icon) == null) {
            throw new IllegalStateException("Unable to find the module associated " +
                    "with icon " + icon.key() + ", have you registered the module " +
                    "you are trying to use with Iconify.with(...) in your Application?");
        }
        return icon;
    }

    /**
     * Create an IconDrawable.
     * @param context Your activity or application context.
     * @param iconKey The icon key you want this drawable to display.
     * @throws IllegalArgumentException if the key doesn't match any icon.
     */
    public IconDrawable(@NonNull Context context, @NonNull String iconKey) {
        this(context, new IconState(findValidIconForKey(iconKey)));
    }

    /**
     * Create an IconDrawable.
     * @param context Your activity or application context.
     * @param icon    The icon you want this drawable to display.
     */
    public IconDrawable(@NonNull Context context, @NonNull Icon icon) {
        this(context, new IconState(validateIcon(icon)));
    }

    private IconDrawable(@NonNull IconState state) {
        this(null, state);
    }

    private IconDrawable(Context context, @NonNull IconState state) {
        iconState = state;
        paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        // We have already confirmed that a typeface exists for this icon during
        // validation, so we can ignore the null pointer warning.
        //noinspection ConstantConditions
        paint.setTypeface(Iconify.findTypefaceOf(state.icon).getTypeface(context));
        paint.setStyle(state.style);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setUnderlineText(false);
        color = state.colorStateList.getColorForState(StateSet.WILD_CARD, DEFAULT_COLOR);
        paint.setColor(color);
        updateTintFilter();
        setModulatedAlpha();
        paint.setDither(iconState.dither);
        text = String.valueOf(iconState.icon.character());
        if (SDK_INT < LOLLIPOP && iconState.bounds != null) {
            setBounds(iconState.bounds);
        }
    }

    /**
     * Set the size of this icon to the standard Android ActionBar.
     * @return The current IconDrawable for chaining.
     */
    @NonNull
    public IconDrawable actionBarSize(@NonNull Context context) {
        return sizeDp(context, ANDROID_ACTIONBAR_ICON_SIZE_DP);
    }

    /**
     * Set the size of the drawable.
     * @param dimenRes The dimension resource.
     * @return The current IconDrawable for chaining.
     */
    @NonNull
    public IconDrawable sizeRes(@NonNull Context context, @DimenRes int dimenRes) {
        return sizePx(context.getResources().getDimensionPixelSize(dimenRes));
    }

    /**
     * Set the size of the drawable.
     * @param size The size in density-independent pixels (dp).
     * @return The current IconDrawable for chaining.
     */
    @NonNull
    public IconDrawable sizeDp(@NonNull Context context, @IntRange(from = 0) int size) {
        return sizePx((int) TypedValue.applyDimension(
                COMPLEX_UNIT_DIP, size,
                context.getResources().getDisplayMetrics()));
    }

    /**
     * Set the size of the drawable.
     * @param size The size in pixels (px).
     * @return The current IconDrawable for chaining.
     */
    @NonNull
    public IconDrawable sizePx(@IntRange(from = -1) int size) {
        iconState.height = size;
        if (size == -1) {
            iconState.width = -1;
        } else {
            paint.setTextSize(size);
            paint.getTextBounds(text, 0, 1, TEMP_DRAW_BOUNDS);
            iconState.width = TEMP_DRAW_BOUNDS.width();
        }
        return this;
    }

    /**
     * Set the color of the drawable.
     * @param color The color, usually from android.graphics.Color or 0xFF012345.
     * @return The current IconDrawable for chaining.
     */
    @NonNull
    public IconDrawable color(@ColorInt int color) {
        return color(ColorStateList.valueOf(color));
    }

    /**
     * Set the color of the drawable.
     * @param colorStateList The color state list.
     * @return The current IconDrawable for chaining.
     */
    @NonNull
    public IconDrawable color(@NonNull ColorStateList colorStateList) {
        if (colorStateList != iconState.colorStateList) {
            iconState.colorStateList = colorStateList;
            color = iconState.colorStateList.getColorForState(StateSet.WILD_CARD, DEFAULT_COLOR);
            paint.setColor(color);
            invalidateSelf();
        }
        return this;
    }

    /**
     * Set the color of the drawable.
     * @param colorRes The color resource, from your R file.
     * @return The current IconDrawable for chaining.
     */
    @NonNull
    public IconDrawable colorRes(@NonNull Context context, @ColorRes int colorRes) {
        // Since we have an @ColorRes annotation on the colorRes parameter,
        // we can be sure that we will get a non-null ColorStateList.
        //noinspection ConstantConditions
        return color(context.getResources().getColorStateList(colorRes));
    }

    /**
     * Set the alpha of this drawable.
     * @param alpha The alpha, between 0 (transparent) and 255 (opaque).
     * @return The current IconDrawable for chaining.
     */
    @NonNull
    public IconDrawable alpha(@ColorInt int alpha) {
        setAlpha(alpha);
        return this;
    }

    /**
     * Start a spinning animation on this drawable. Call {@link #stop()}
     * to stop it.
     * @return The current IconDrawable for chaining.
     */
    @NonNull
    public IconDrawable spin() {
        start();
        return this;
    }

    /**
     * Start a pulse animation on this drawable. Call {@link #stop()}
     * to stop it.
     * @return The current IconDrawable for chaining.
     */
    @NonNull
    public IconDrawable pulse() {
        iconState.pulse = true;
        start();
        return this;
    }

    /**
     * Returns the icon to be displayed
     * @return The icon
     */
    @CheckResult
    @NonNull
    public final Icon getIcon() {
        return iconState.icon;
    }

    @Override
    @CheckResult
    @IntRange(from = -1)
    public int getIntrinsicHeight() {
        return iconState.height;
    }

    @Override
    @CheckResult
    @IntRange(from = -1)
    public int getIntrinsicWidth() {
        return iconState.width;
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        final int width = bounds.width();
        final int height = bounds.height();
        paint.setTextSize(height);
        paint.getTextBounds(text, 0, 1, drawBounds);
        paint.setTextSize(Math.min(height, (int) Math.ceil(
                width * (height / (float) drawBounds.width()))));
        paint.getTextBounds(text, 0, 1, drawBounds);
        drawBounds.offsetTo(bounds.left + (width - drawBounds.width()) / 2,
                bounds.top + (height - drawBounds.height()) / 2 - drawBounds.bottom);
        centerX = bounds.exactCenterX();
        centerY = bounds.exactCenterY();
    }

    @Override
    @CheckResult
    @NonNull
    public Rect getDirtyBounds() {
        return drawBounds;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void getOutline(@NonNull Outline outline) {
        outline.setRect(drawBounds);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.save();
        final boolean needMirroring = needMirroring();
        if (needMirroring) {
            canvas.translate(getBounds().width(), 0);
            canvas.scale(-1.0f, 1.0f);
        }
        if (iconState.spinning) {
            long currentTime = SystemClock.uptimeMillis();
            if (spinStartTime < 0) {
                spinStartTime = currentTime;
                if (isVisible()) {
                    if (iconState.pulse) {
                        scheduleSelf(invalidateRunnable, currentTime + ROTATION_PULSE_DURATION);
                    } else {
                        invalidateSelf();
                    }
                }
            } else {
                boolean isVisible = isVisible();
                long timeElapsed = currentTime - spinStartTime;
                float rotation;
                if (iconState.pulse) {
                    rotation = timeElapsed / (float) ROTATION_PULSE_DURATION;
                    if (isVisible) {
                        scheduleSelf(invalidateRunnable, currentTime +
                                (timeElapsed * (int) (rotation + 1)));
                    }
                    rotation = ((int) Math.floor(rotation)) * 360f / ROTATION_PULSES;
                } else {
                    rotation = timeElapsed / (float) ROTATION_DURATION * 360f;
                    if (isVisible) {
                        invalidateSelf();
                    }
                }
                canvas.rotate(rotation, centerX, centerY);
            }
            if (isVisible()) {
                invalidateSelf();
            }
        }
        canvas.drawText(text, centerX, drawBounds.bottom, paint);
        canvas.restore();
    }

    @Override
    @CheckResult
    public boolean isStateful() {
        return iconState.colorStateList.isStateful() ||
                (iconState.tint != null && iconState.tint.isStateful());
    }

    @Override
    protected boolean onStateChange(@NonNull int[] state) {
        boolean changed = false;

        int newColor = iconState.colorStateList.getColorForState(state, DEFAULT_COLOR);
        if (newColor != color) {
            color = newColor;
            paint.setColor(color);
            setModulatedAlpha();
            changed = true;
        }

        if (tintFilter != null) {
            // If tintFilter is not null, then it's guaranteed that tint and tintMode
            // are not null as well, so suppress any warnings otherwise.
            //noinspection ConstantConditions
            int newTintColor = iconState.tint.getColorForState(state, Color.TRANSPARENT);
            if (newTintColor != tintColor) {
                tintColor = newTintColor;
                //noinspection ConstantConditions
                tintFilter = new PorterDuffColorFilter(tintColor, iconState.tintMode);
                if (iconState.colorFilter == null) {
                    paint.setColorFilter(tintFilter);
                    changed = true;
                }
            }
        }

        return changed;
    }

    @Override
    public void setAlpha(@ColorInt int alpha) {
        if (alpha != iconState.alpha) {
            iconState.alpha = alpha;
            setModulatedAlpha();
            invalidateSelf();
        }
    }

    private void setModulatedAlpha() {
        /* color >>> 24 Returns the alpha component of a color int
         * For example we have a color #8800ff00, its integer value will be -2013200640
         * -2013200640 >>> 24 will give us 136 which is the decimal value of hex 88
         * Ref: https://developer.android.com/reference/android/graphics/Color#alpha(int)
         */
        paint.setAlpha(((color >>> 24) * iconState.alpha) / 255);
    }

    @Override
    @CheckResult
    @ColorInt
    public int getAlpha() {
        return iconState.alpha;
    }

    @Override
    @CheckResult
    public int getOpacity() {
        int baseAlpha = color >>> 24;
        if (baseAlpha == 255 && iconState.alpha == 255) return PixelFormat.OPAQUE;
        if (baseAlpha == 0 || iconState.alpha == 0) return PixelFormat.TRANSPARENT;
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setDither(boolean dither) {
        if (dither != iconState.dither) {
            iconState.dither = dither;
            paint.setDither(dither);
            invalidateSelf();
        }
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter cf) {
        if (cf != iconState.colorFilter) {
            iconState.colorFilter = cf;
            paint.setColorFilter(cf);
            invalidateSelf();
        }
    }

    @Override
    @CheckResult
    @Nullable
    public ColorFilter getColorFilter() {
        return iconState.colorFilter;
    }

    @NonNull
    public IconDrawable tint(@Nullable ColorStateList tint) {
        if (tint != iconState.tint) {
            iconState.tint = tint;
            updateTintFilter();
            invalidateSelf();
        }
        return this;
    }

    @Override
    public void setTintList(@Nullable ColorStateList tint) {
        tint(tint);
    }

    @Override
    public void setTintMode(@NonNull PorterDuff.Mode tintMode) {
        if (tintMode != iconState.tintMode) {
            iconState.tintMode = tintMode;
            updateTintFilter();
            invalidateSelf();
        }
    }

    private void updateTintFilter() {
        if (iconState.tint == null || iconState.tintMode == null) {
            if (tintFilter == null) {
                return;
            }
            tintColor = 0;
            tintFilter = null;
        } else {
            tintColor = iconState.tint.getColorForState(getState(), Color.TRANSPARENT);
            tintFilter = new PorterDuffColorFilter(tintColor, iconState.tintMode);
        }
        if (iconState.colorFilter == null) {
            paint.setColorFilter(tintFilter);
            invalidateSelf();
        }
    }

    /**
     * Sets paint style.
     * @param style to be applied
     */
    public void setStyle(@NonNull Paint.Style style) {
        if (style != iconState.style) {
            iconState.style = style;
            paint.setStyle(style);
            invalidateSelf();
        }
    }

    @Override
    public void setAutoMirrored(boolean mirrored) {
        if (SDK_INT >= JELLY_BEAN_MR1 && iconState.icon.supportsRtl() &&
                iconState.autoMirrored != mirrored) {
            iconState.autoMirrored = mirrored;
            invalidateSelf();
        }
    }

    @Override
    @CheckResult
    public final boolean isAutoMirrored() {
        return iconState.autoMirrored;
    }

    // Since the auto-mirrored state is only set to true if the SDK
    // version supports it, we don't need an explicit check for that
    // before calling getLayoutDirection().
    @TargetApi(JELLY_BEAN_MR1)
    @CheckResult
    private boolean needMirroring() {
        if (isAutoMirrored()) {
            if (SDK_INT >= M) {
                return getLayoutDirection() == LayoutDirection.RTL;
            }
            // Since getLayoutDirection() is hidden prior to Marshmallow, we
            // will try to get the layout direction from the View, which we will
            // assume is set as the callback. As the setLayoutDirection() method
            // is also hidden, we can safely rely on the behaviour of the
            // platform Views to provide a correct replacement for the hidden
            // method.
            Callback callback = getCallback();
            if (callback instanceof View) {
                return ((View) callback).getLayoutDirection() == LAYOUT_DIRECTION_RTL;
            }
        }
        return false;
    }

    @Override
    public void start() {
        if (!iconState.spinning) {
            iconState.spinning = true;
            if (iconState.pulse && invalidateRunnable == null) {
                invalidateRunnable = new InvalidateRunnable();
            }
            invalidateSelf();
        }
    }

    @Override
    public void stop() {
        if (iconState.spinning) {
            iconState.spinning = false;
            if (invalidateRunnable != null) {
                unscheduleSelf(invalidateRunnable);
                invalidateRunnable = null;
            }
        }
    }

    @Override
    @CheckResult
    public boolean isRunning() {
        return iconState.spinning;
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        final boolean changed = super.setVisible(visible, restart);
        if (iconState.spinning) {
            if (changed) {
                if (visible) {
                    invalidateSelf();
                } else if (invalidateRunnable != null) {
                    unscheduleSelf(invalidateRunnable);
                }
            } else {
                if (restart && visible) {
                    spinStartTime = -1;
                }
            }
        }
        return changed;
    }

    private class InvalidateRunnable implements Runnable {
        @Override
        public void run() {
            invalidateSelf();
        }
    }

    @Override
    @CheckResult
    public int getChangingConfigurations() {
        return iconState.changingConfigurations;
    }

    @Override
    public void setChangingConfigurations(int configs) {
        iconState.changingConfigurations = configs;
    }

    // Implementing shared state despite being a third-party implementation
    // in order to work around bugs in the framework and support library:
    // http://b.android.com/191754
    // https://github.com/JoanZapata/android-iconify/issues/93
    @Override
    @CheckResult
    @Nullable
    public ConstantState getConstantState() {
        // The bounds level need to be copied here to work around a bug in
        // LayerDrawable where it doesn't copy the bounds and level in it's
        // children when mutated or cloned. This bug has been fixed in
        // Lollipop. The layout direction was not copied as well in Jelly
        // Bean MR 1, but we're ignoring it for the moment as it's not
        // exposed in the SDK prior to Marshmallow. If it ever becomes an
        // issue though, then we'll need to handle that as well.
        iconState.bounds = getBounds();
        return iconState;
    }

    @Override
    @NonNull
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            iconState = new IconState(iconState);
            mMutated = true;
        }
        return this;
    }

    private static class IconState extends ConstantState {
        @NonNull
        final Icon icon;
        @IntRange(from = -1)
        int height = -1;
        @IntRange(from = -1)
        int width = -1;
        @NonNull
        ColorStateList colorStateList = ColorStateList.valueOf(DEFAULT_COLOR);
        @ColorInt
        int alpha = 255;
        boolean dither;
        @Nullable
        ColorFilter colorFilter;
        @Nullable
        ColorStateList tint = DEFAULT_TINT;
        @Nullable
        PorterDuff.Mode tintMode = DEFAULT_TINT_MODE;
        @NonNull
        Paint.Style style = Paint.Style.FILL;
        boolean spinning;
        boolean pulse;
        boolean autoMirrored;
        int changingConfigurations;
        @Nullable
        Rect bounds;

        IconState(@NonNull Icon icon) {
            this.icon = icon;
            autoMirrored = SDK_INT >= JELLY_BEAN_MR1 && icon.supportsRtl();
        }

        IconState(@NonNull IconState state) {
            icon = state.icon;
            height = state.height;
            width = state.width;
            colorStateList = state.colorStateList;
            alpha = state.alpha;
            dither = state.dither;
            colorFilter = state.colorFilter;
            tint = state.tint;
            tintMode = state.tintMode;
            style = state.style;
            spinning = state.spinning;
            pulse = state.pulse;
            autoMirrored = state.autoMirrored;
            changingConfigurations = state.changingConfigurations;
        }

        @Override
        @CheckResult
        @NonNull
        public Drawable newDrawable() {
            return new IconDrawable(this);
        }

        @Override
        @CheckResult
        public int getChangingConfigurations() {
            return changingConfigurations;
        }
    }
}

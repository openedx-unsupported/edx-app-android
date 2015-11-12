package org.edx.mobile.third_party.iconify;

/**
 * Copyright 2013 Joan Zapata
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * It uses FontAwesome font, licensed under OFL 1.1, which is compatible
 * with this library's license.
 *
 *     http://scripts.sil.org/cms/scripts/render_download.php?format=file&media_id=OFL_plaintext&filename=OFL.txt
 */

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
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.StateSet;
import android.view.View;

import org.edx.mobile.third_party.iconify.Iconify.IconValue;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.edx.mobile.third_party.iconify.Utils.convertDpToPx;

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
    static final int DEFAULT_COLOR = Color.BLACK;
    // Set the default tint to make it half translucent on disabled state.
    private static final PorterDuff.Mode DEFAULT_TINT_MODE = PorterDuff.Mode.MULTIPLY;
    private static final ColorStateList DEFAULT_TINT = new ColorStateList(
            new int[][] { { -android.R.attr.state_enabled }, StateSet.WILD_CARD },
            new int[] { 0x80FFFFFF, 0xFFFFFFFF }
    );
    private static final int ROTATION_DURATION = 2000;
    private static final int ANDROID_ACTIONBAR_ICON_SIZE_DP = 24;
    private static final Rect TEMP_DRAW_BOUNDS = new Rect();

    @NonNull
    private IconState iconState;
    private final TextPaint paint;
    private int color;
    private ColorFilter tintFilter;
    private int tintColor;
    private long rotationStartTime = -1;
    private boolean mMutated;
    private final String text;
    private final Rect drawBounds = new Rect();
    private float centerX, centerY;

    /**
     * Create an IconDrawable.
     * @param context Your activity or application context.
     * @param icon    The icon you want this drawable to display.
     */
    public IconDrawable(@NonNull Context context, @NonNull IconValue icon) {
        this(context, new IconState(icon));
    }

    private IconDrawable(@NonNull IconState state) {
        this(null, state);
    }

    private IconDrawable(Context context, @NonNull IconState state) {
        iconState = state;
        paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setTypeface(Iconify.getTypeface(context));
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
    public IconDrawable sizeDp(@NonNull Context context, int size) {
        return sizePx(convertDpToPx(context, size));
    }

    /**
     * Set the size of the drawable.
     * @param size The size in pixels (px).
     * @return The current IconDrawable for chaining.
     */
    @NonNull
    public IconDrawable sizePx(int size) {
        iconState.height = size;
        paint.setTextSize(size);
        paint.getTextBounds(text, 0, 1, TEMP_DRAW_BOUNDS);
        iconState.width = TEMP_DRAW_BOUNDS.width();
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
    public IconDrawable alpha(int alpha) {
        setAlpha(alpha);
        return this;
    }

    /**
     * Start a spinning animation on this drawable. Call {@link #stop()}
     * to stop it.
     * @return The current IconDrawable for chaining.
     */
    @NonNull
    public IconDrawable rotate() {
        start();
        return this;
    }

    /**
     * Returns the icon to be displayed
     * @return The icon
     */
    public final IconValue getIcon() {
        return iconState.icon;
    }

    @Override
    public int getIntrinsicHeight() {
        return iconState.height;
    }

    @Override
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
        if (iconState.rotating) {
            long currentTime = SystemClock.uptimeMillis();
            if (rotationStartTime < 0) {
                rotationStartTime = currentTime;
            } else {
                float rotation = (currentTime - rotationStartTime) /
                        (float) ROTATION_DURATION * 360f;
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
    public void setAlpha(int alpha) {
        if (alpha != iconState.alpha) {
            iconState.alpha = alpha;
            setModulatedAlpha();
            invalidateSelf();
        }
    }

    private void setModulatedAlpha() {
        paint.setAlpha(((color >> 24) * iconState.alpha) / 255);
    }

    @Override
    public int getAlpha() {
        return iconState.alpha;
    }

    @Override
    public int getOpacity() {
        int baseAlpha = color >> 24;
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
    @Nullable
    public ColorFilter getColorFilter() {
        return iconState.colorFilter;
    }

    @Override
    public void setTintList(@Nullable ColorStateList tint) {
        if (tint != iconState.tint) {
            iconState.tint = tint;
            updateTintFilter();
            invalidateSelf();
        }
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
        if (SDK_INT >= JELLY_BEAN_MR1 && iconState.icon.supportsRtl &&
                iconState.autoMirrored != mirrored) {
            iconState.autoMirrored = mirrored;
            invalidateSelf();
        }
    }

    @Override
    public final boolean isAutoMirrored() {
        return iconState.autoMirrored;
    }

    // Since the auto-mirrored state is only set to true the SDK version
    // supports it, we don't need an explicit check for it before calling
    // the getLayoutDirection() methods.
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private boolean needMirroring() {
        if (isAutoMirrored()) {
            // TODO: Uncomment this one we start compiling against Marshmallow
            /*if (SDK_INT >= M) {
                return getLayoutDirection() == LayoutDirection.RTL;
            }*/
            // Since getLayoutDirection() is hidden prior to Marshmallow, we will
            // try to get the layout direction from the View, which we will assume
            // is set as the callback. As the setLayoutDirection() method is also
            // hidden, we can safely rely on the behaviour of the platform Views to
            // provide a correct replacement for the hidden method.
            Callback callback = getCallback();
            if (callback instanceof View) {
                return ((View) callback).getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
            }
        }
        return false;
    }

    @Override
    public void start() {
        if (!iconState.rotating) {
            iconState.rotating = true;
            invalidateSelf();
        }
    }

    @Override
    public void stop() {
        if (iconState.rotating) {
            iconState.rotating = false;
        }
    }

    @Override
    public boolean isRunning() {
        return iconState.rotating;
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        final boolean changed = super.setVisible(visible, restart);
        if (iconState.rotating) {
            if (changed) {
                if (visible) {
                    invalidateSelf();
                }
            } else {
                if (restart && visible) {
                    rotationStartTime = -1;
                }
            }
        }
        return changed;
    }

    @Override
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
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            iconState = new IconState(iconState);
            mMutated = true;
        }
        return this;
    }

    private static class IconState extends ConstantState {
        @NonNull
        final IconValue icon;
        int height = -1, width = -1;
        @NonNull
        ColorStateList colorStateList = ColorStateList.valueOf(DEFAULT_COLOR);
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
        boolean rotating;
        boolean autoMirrored;
        int changingConfigurations;
        @Nullable
        Rect bounds;

        IconState(@NonNull IconValue icon) {
            this.icon = icon;
            autoMirrored = SDK_INT >= JELLY_BEAN_MR1 && icon.supportsRtl;
        }

        IconState(IconState state) {
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
            rotating = state.rotating;
            autoMirrored = state.autoMirrored;
            changingConfigurations = state.changingConfigurations;
        }

        @Override
        public Drawable newDrawable() {
            return new IconDrawable(this);
        }

        @Override
        public int getChangingConfigurations() {
            return changingConfigurations;
        }
    }
}
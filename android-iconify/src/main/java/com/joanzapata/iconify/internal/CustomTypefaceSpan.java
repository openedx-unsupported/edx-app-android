package com.joanzapata.iconify.internal;

import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ReplacementSpan;
import android.widget.TextView;
import com.joanzapata.iconify.Icon;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.view.View.LAYOUT_DIRECTION_RTL;
import static android.view.View.TEXT_DIRECTION_ANY_RTL;
import static android.view.View.TEXT_DIRECTION_FIRST_STRONG;
import static android.view.View.TEXT_DIRECTION_FIRST_STRONG_LTR;
import static android.view.View.TEXT_DIRECTION_FIRST_STRONG_RTL;
import static android.view.View.TEXT_DIRECTION_LOCALE;
import static android.view.View.TEXT_DIRECTION_LTR;
import static android.view.View.TEXT_DIRECTION_RTL;

public class CustomTypefaceSpan extends ReplacementSpan {
    private static final int ROTATION_DURATION = 600;
    // Font Awesome uses 8-step rotation for pulse, and
    // it seems to have the only pulsing spinner. If
    // spinners with different pulses are introduced at
    // some point, then a pulse property can be
    // implemented for the icons.
    private static final int ROTATION_PULSES = 8;
    private static final int ROTATION_PULSE_DURATION = ROTATION_DURATION / ROTATION_PULSES;
    private static final Rect TEXT_BOUNDS = new Rect();
    private static final Rect DIRTY_REGION = new Rect();
    private static final RectF DIRTY_REGION_FLOAT = new RectF();
    private static final Matrix ROTATION_MATRIX = new Matrix();
    private static final Paint LOCAL_PAINT = new Paint();
    private static final float BASELINE_RATIO = 1 / 7f;

    @NonNull
    private final TextView view;
    @NonNull
    private final String icon;
    @NonNull
    private final Typeface type;
    @Size
    private final float iconSizePx;
    @FloatRange(from = -1f, to = 1f)
    private final float iconSizeRatio;
    @ColorInt
    private final int iconColor;
    private final boolean mirrorable;
    @NonNull
    private final Animation animation;
    private final boolean baselineAligned;
    @IntRange(from = -1)
    private long spinStartTime;

    public CustomTypefaceSpan(@NonNull TextView view, @NonNull Icon icon,
            @NonNull Typeface type, @Size float iconSizePx,
            @FloatRange(from = -1f, to = 1f) float iconSizeRatio,
            @ColorInt int iconColor, @NonNull Animation animation,
            boolean baselineAligned) {
        this.view = view;
        this.animation = animation;
        this.baselineAligned = baselineAligned;
        this.icon = String.valueOf(icon.character());
        this.type = type;
        this.iconSizePx = iconSizePx;
        this.iconSizeRatio = iconSizeRatio;
        this.iconColor = iconColor;
        this.mirrorable = SDK_INT >= JELLY_BEAN_MR1 && icon.supportsRtl();
    }

    @Override
    @CheckResult
    public int getSize(@NonNull Paint paint, @NonNull CharSequence text,
            @IntRange(from = 0) int start, @IntRange(from = 0) int end,
            @Nullable Paint.FontMetricsInt fm) {
        LOCAL_PAINT.set(paint);
        applyCustomTypeFace(LOCAL_PAINT, type);
        LOCAL_PAINT.getTextBounds(icon, 0, 1, TEXT_BOUNDS);
        if (fm != null) {
            float baselineRatio = baselineAligned ? 0 : BASELINE_RATIO;
            fm.descent = (int) (TEXT_BOUNDS.height() * baselineRatio);
            fm.ascent = -(TEXT_BOUNDS.height() - fm.descent);
            fm.top = fm.ascent;
            fm.bottom = fm.descent;
        }
        return TEXT_BOUNDS.width();
    }

    @Override
    @TargetApi(JELLY_BEAN_MR1)
    public void draw(@NonNull Canvas canvas, @NonNull CharSequence text,
            @IntRange(from = 0) int start, @IntRange(from = 0) int end,
            float x, int top, int y, int bottom, @NonNull Paint paint) {
        applyCustomTypeFace(paint, type);
        paint.getTextBounds(icon, 0, 1, TEXT_BOUNDS);
        int width = TEXT_BOUNDS.width();
        int height = TEXT_BOUNDS.height();
        float baselineRatio = baselineAligned ? 0f : BASELINE_RATIO;
        canvas.save();
        float baselineOffset = height * baselineRatio;
        float offsetY = y - TEXT_BOUNDS.bottom + baselineOffset;
        if (!needMirroring()) {
            canvas.translate(x - TEXT_BOUNDS.left, offsetY);
        } else {
            canvas.translate(x + width + TEXT_BOUNDS.left, offsetY);
            canvas.scale(-1.0f, 1.0f);
        }
        if (animation != Animation.NONE) {
            DIRTY_REGION.set(TEXT_BOUNDS);
            DIRTY_REGION.offsetTo((int) x, y - height + Math.round(baselineOffset));
            long currentTime = SystemClock.uptimeMillis();
            if (spinStartTime < 0) {
                spinStartTime = currentTime;
                switch (animation) {
                    case PULSE:
                        view.postInvalidateDelayed(ROTATION_PULSE_DURATION,
                                DIRTY_REGION.left, DIRTY_REGION.top,
                                DIRTY_REGION.right, DIRTY_REGION.bottom);
                        break;
                    case SPIN:
                        view.invalidate(DIRTY_REGION);
                        break;
                }
            } else {
                long timeElapsed = currentTime - spinStartTime;
                float rotation;
                switch (animation) {
                    case PULSE:
                        rotation = timeElapsed / (float) ROTATION_PULSE_DURATION;
                        long invalidationDelay = ROTATION_PULSE_DURATION -
                                (timeElapsed % ROTATION_PULSE_DURATION);
                        rotation = ((int) Math.floor(rotation)) * 360f / ROTATION_PULSES;
                        rotateDirtyRegion(rotation);
                        view.postInvalidateDelayed(invalidationDelay,
                                DIRTY_REGION.left, DIRTY_REGION.top,
                                DIRTY_REGION.right, DIRTY_REGION.bottom);
                        break;
                    case SPIN:
                        rotation = timeElapsed / (float) ROTATION_DURATION * 360f;
                        rotateDirtyRegion(rotation);
                        view.invalidate(DIRTY_REGION);
                        break;
                    default:
                        throw new IllegalStateException();
                }
                float centerX = TEXT_BOUNDS.left + width / 2f;
                float centerY = TEXT_BOUNDS.bottom - height / 2f;
                canvas.rotate(rotation, centerX, centerY);
            }
        }

        canvas.drawText(icon, 0, 0, paint);
        canvas.restore();
    }

    // Rotate the dirty rectangle and set it to the new
    // bounds containing the rotated rectangle.
    private static void rotateDirtyRegion(@FloatRange(from = 0) float rotation) {
        DIRTY_REGION_FLOAT.set(DIRTY_REGION);
        ROTATION_MATRIX.postRotate(rotation,
                DIRTY_REGION_FLOAT.centerX(), DIRTY_REGION_FLOAT.centerY());
        ROTATION_MATRIX.mapRect(DIRTY_REGION_FLOAT);
        DIRTY_REGION_FLOAT.round(DIRTY_REGION);
    }

    private void applyCustomTypeFace(@NonNull Paint paint, @NonNull Typeface tf) {
        paint.setFakeBoldText(false);
        paint.setTextSkewX(0f);
        paint.setTypeface(tf);
        if (animation != Animation.NONE) paint.clearShadowLayer();
        if (iconSizeRatio > 0) paint.setTextSize(paint.getTextSize() * iconSizeRatio);
        else if (iconSizePx > 0) paint.setTextSize(iconSizePx);
        if (iconColor < Integer.MAX_VALUE) paint.setColor(iconColor);
    }

    // Since the 'mirrorable' flag is only set to true if the SDK
    // version supports it, we don't need an explicit check for that
    // before calling getLayoutDirection().
    @TargetApi(JELLY_BEAN_MR1)
    @CheckResult
    private boolean needMirroring() {
        if (!mirrorable) return false;

        // Passwords fields should be LTR
        if (view.getTransformationMethod() instanceof PasswordTransformationMethod) {
            return false;
        }

        // Always need to resolve layout direction first
        final boolean defaultIsRtl = view.getLayoutDirection() == LAYOUT_DIRECTION_RTL;

        if (SDK_INT < JELLY_BEAN_MR2) {
            return defaultIsRtl;
        }

        // Select the text direction heuristic according to the
        // package-private getTextDirectionHeuristic() method in TextView
        TextDirectionHeuristic textDirectionHeuristic;
        switch (view.getTextDirection()) {
            default:
            case TEXT_DIRECTION_FIRST_STRONG:
                textDirectionHeuristic = defaultIsRtl ?
                        TextDirectionHeuristics.FIRSTSTRONG_RTL :
                        TextDirectionHeuristics.FIRSTSTRONG_LTR;
                break;
            case TEXT_DIRECTION_ANY_RTL:
                textDirectionHeuristic = TextDirectionHeuristics.ANYRTL_LTR;
                break;
            case TEXT_DIRECTION_LTR:
                textDirectionHeuristic = TextDirectionHeuristics.LTR;
                break;
            case TEXT_DIRECTION_RTL:
                textDirectionHeuristic = TextDirectionHeuristics.RTL;
                break;
            case TEXT_DIRECTION_LOCALE:
                textDirectionHeuristic = TextDirectionHeuristics.LOCALE;
                break;
            case TEXT_DIRECTION_FIRST_STRONG_LTR:
                textDirectionHeuristic = TextDirectionHeuristics.FIRSTSTRONG_LTR;
                break;
            case TEXT_DIRECTION_FIRST_STRONG_RTL:
                textDirectionHeuristic = TextDirectionHeuristics.FIRSTSTRONG_RTL;
                break;
        }
        CharSequence text = view.getText();
        return textDirectionHeuristic.isRtl(text, 0, text.length());
    }
}

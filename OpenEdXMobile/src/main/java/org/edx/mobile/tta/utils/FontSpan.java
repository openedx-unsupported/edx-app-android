package org.edx.mobile.tta.utils;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import androidx.annotation.Nullable;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

/**
 * Span that changes the typeface of the text used to the one provided. The style set before will
 * be kept.
 */
public class FontSpan extends MetricAffectingSpan {

    @Nullable
    private final Typeface font;

    public FontSpan(@Nullable final Typeface font) {
        this.font = font;
    }

    @Override
    public void updateMeasureState(TextPaint textPaint) {
        update(textPaint);
    }

    @Override
    public void updateDrawState(TextPaint textPaint) {
        update(textPaint);
    }

    @SuppressLint("WrongConstant")
    private void update(TextPaint textPaint) {
        Typeface old = textPaint.getTypeface();
        int oldStyle = old != null ? old.getStyle() : 0;

        // Typeface is already cached at the system level
        // keep the style set before
        Typeface font = Typeface.create(this.font, oldStyle);
        textPaint.setTypeface(font);
    }
}

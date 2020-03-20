package com.joanzapata.iconify.internal;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import androidx.annotation.CheckResult;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Size;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.TypedValue;
import android.widget.TextView;
import com.joanzapata.iconify.Icon;

import java.util.List;

public final class ParsingUtil {

    private static final String ANDROID_PACKAGE_NAME = "android";

    // Prevents instantiation
    private ParsingUtil() {}

    @CheckResult
    @NonNull
    public static CharSequence parse(
            @NonNull
            TextView targetView,
            @NonNull @Size(min = 1)
            List<IconFontDescriptorWrapper> iconFontDescriptors,
            @NonNull
            CharSequence text) {
        final SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(text);
        recursivePrepareSpannableIndexes(targetView,
                text.toString(), spannableBuilder,
                iconFontDescriptors, 0);
        return spannableBuilder;
    }

    // Analyse the text and replace {} blocks with the appropriate character
    // Retain all transformations in the accumulator
    private static void recursivePrepareSpannableIndexes(
            @NonNull
            TextView targetView,
            @NonNull
            String fullText,
            @NonNull
            SpannableStringBuilder text,
            @NonNull @Size(min = 1)
            List<IconFontDescriptorWrapper> iconFontDescriptors,
            @IntRange(from = 0)
            int start) {

        // Try to find a {...} in the string and extract expression from it
        String stringText = text.toString();
        int startIndex = stringText.indexOf("{", start);
        if (startIndex == -1) return;
        int endIndex = stringText.indexOf("}", startIndex) + 1;
        String expression = stringText.substring(startIndex + 1, endIndex - 1);

        // Split the expression and retrieve the icon key
        String[] strokes = expression.split(" ");
        String key = strokes[0];

        // Loop through the descriptors to find a key match
        IconFontDescriptorWrapper iconFontDescriptor = null;
        Icon icon = null;
        for (int i = 0; i < iconFontDescriptors.size(); i++) {
            iconFontDescriptor = iconFontDescriptors.get(i);
            icon = iconFontDescriptor.getIcon(key);
            if (icon != null) break;
        }

        // If no match, ignore and continue
        if (icon == null) {
            recursivePrepareSpannableIndexes(targetView, fullText, text, iconFontDescriptors, endIndex);
            return;
        }

        // See if any more stroke within {} should be applied
        Context context = targetView.getContext();
        float iconSizePx = -1;
        int iconColor = Integer.MAX_VALUE;
        float iconSizeRatio = -1;
        Animation animation = Animation.NONE;
        boolean baselineAligned = false;
        for (int i = 1; i < strokes.length; i++) {
            String stroke = strokes[i];

            // Look for "spin"
            if (stroke.equalsIgnoreCase("spin")) {
                animation = Animation.SPIN;
            }

            // Look for "pulse"
            else if (stroke.equalsIgnoreCase("pulse")) {
                animation = Animation.PULSE;
            }

            // Look for "baseline"
            else if (stroke.equalsIgnoreCase("baseline")) {
                baselineAligned = true;
            }

            // Look for an icon size
            else if (stroke.matches("([0-9]*(\\.[0-9]*)?)dp")) {
                iconSizePx = dpToPx(context, Float.valueOf(stroke.substring(0, stroke.length() - 2)));
            } else if (stroke.matches("([0-9]*(\\.[0-9]*)?)sp")) {
                iconSizePx = spToPx(context, Float.valueOf(stroke.substring(0, stroke.length() - 2)));
            } else if (stroke.matches("([0-9]*)px")) {
                iconSizePx = Integer.valueOf(stroke.substring(0, stroke.length() - 2));
            } else if (stroke.matches("@dimen/(.*)")) {
                iconSizePx = getPxFromDimen(context, context.getPackageName(), stroke.substring(7));
                if (iconSizePx < 0)
                    throw new IllegalArgumentException("Unknown resource " + stroke + " in \"" + fullText + "\"");
            } else if (stroke.matches("@android:dimen/(.*)")) {
                iconSizePx = getPxFromDimen(context, ANDROID_PACKAGE_NAME, stroke.substring(15));
                if (iconSizePx < 0)
                    throw new IllegalArgumentException("Unknown resource " + stroke + " in \"" + fullText + "\"");
            } else if (stroke.matches("([0-9]*(\\.[0-9]*)?)%")) {
                iconSizeRatio = Float.valueOf(stroke.substring(0, stroke.length() - 1)) / 100f;
            }

            // Look for an icon color
            else if (stroke.matches("#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})")) {
                iconColor = Color.parseColor(stroke);
            } else if (stroke.matches("@color/(.*)")) {
                iconColor = getColorFromResource(context, context.getPackageName(), stroke.substring(7));
                if (iconColor == Integer.MAX_VALUE)
                    throw new IllegalArgumentException("Unknown resource " + stroke + " in \"" + fullText + "\"");
            } else if (stroke.matches("@android:color/(.*)")) {
                iconColor = getColorFromResource(context, ANDROID_PACKAGE_NAME, stroke.substring(15));
                if (iconColor == Integer.MAX_VALUE)
                    throw new IllegalArgumentException("Unknown resource " + stroke + " in \"" + fullText + "\"");
            } else {
                throw new IllegalArgumentException("Unknown expression " + stroke + " in \"" + fullText + "\"");
            }
        }

        // Replace the character and apply the typeface
        text = text.replace(startIndex, endIndex, "" + icon.character());
        text.setSpan(new CustomTypefaceSpan(targetView, icon,
                        iconFontDescriptor.getTypeface(context),
                        iconSizePx, iconSizeRatio, iconColor, animation, baselineAligned),
                startIndex, startIndex + 1,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        recursivePrepareSpannableIndexes(targetView, fullText, text, iconFontDescriptors, startIndex);
    }

    @CheckResult
    public static float getPxFromDimen(@NonNull Context context,
            @NonNull String packageName, @NonNull String resName) {
        Resources resources = context.getResources();
        int resId = resources.getIdentifier(
                resName, "dimen",
                packageName);
        if (resId <= 0) return -1;
        return resources.getDimension(resId);
    }

    @CheckResult
    public static int getColorFromResource(@NonNull Context context,
            @NonNull String packageName, @NonNull String resName) {
        Resources resources = context.getResources();
        int resId = resources.getIdentifier(
                resName, "color",
                packageName);
        if (resId <= 0) return Integer.MAX_VALUE;
        return resources.getColor(resId);
    }

    @CheckResult
    public static float dpToPx(@NonNull Context context,
            @FloatRange(from = 0f) float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    @CheckResult
    public static float spToPx(@NonNull Context context,
            @FloatRange(from = 0f) float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                context.getResources().getDisplayMetrics());
    }

}

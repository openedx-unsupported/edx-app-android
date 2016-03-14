package org.edx.mobile.discussion;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.ResourceUtil;

import java.util.HashMap;

public abstract class DiscussionTextUtils {
    @Inject
    private static Config config;

    private DiscussionTextUtils() {
    }

    public static void setAuthorAttributionText(@NonNull TextView textView,
                                                @StringRes final int stringRes,
                                                @NonNull final IAuthorData authorData,
                                                @NonNull final Runnable onAuthorClickListener) {
        setAuthorAttributionText(textView, stringRes, authorData,
                System.currentTimeMillis(), onAuthorClickListener);
    }


    public static void setAuthorAttributionText(@NonNull TextView textView,
                                                @StringRes final int stringRes,
                                                @NonNull final IAuthorData authorData,
                                                long initialTimeStampMs,
                                                @NonNull final Runnable onAuthorClickListener) {
        final CharSequence text;
        {
            final Context context = textView.getContext();
            final CharSequence formattedTime = getRelativeTimeSpanString(context,
                    initialTimeStampMs, authorData.getCreatedAt().getTime());
            final String authorLabel = authorData.getAuthorLabel();

            final SpannableString authorSpan = new SpannableString(authorData.getAuthor());
            if (config.isUserProfilesEnabled() && !authorData.isAuthorAnonymous()) {
                authorSpan.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        onAuthorClickListener.run();
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                    }
                }, 0, authorSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                authorSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, authorSpan.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            text = trim(ResourceUtil.getFormattedString(context.getResources(),
                    stringRes, new HashMap<String, CharSequence>() {{
                        put("time", formattedTime);
                        put("author", authorSpan);
                        put("author_label", null == authorLabel ? "" : authorLabel.toUpperCase(
                                context.getResources().getConfiguration().locale));
                    }}));
        }

        textView.setText(text);
        // Allow ClickableSpan to trigger clicks
        textView.setMovementMethod(new LinkMovementMethod());
    }

    private static CharSequence getRelativeTimeSpanString(@NonNull Context context, long nowMs,
                                                          long timeMs) {
        if (nowMs - timeMs < DateUtils.SECOND_IN_MILLIS) {
            return context.getString(R.string.just_now);
        } else {
            return DateUtils.getRelativeTimeSpanString(
                    timeMs,
                    nowMs,
                    DateUtils.SECOND_IN_MILLIS,
                    DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR);
        }
    }

    public static CharSequence parseHtml(@NonNull String html) {
        // If the HTML contains a paragraph at the end, there will be blank lines following the text
        // Therefore, we need to trim the resulting CharSequence to remove those extra lines
        return trim(Html.fromHtml(html));
    }

    public static CharSequence trim(CharSequence s) {
        int start = 0, end = s.length();

        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return s.subSequence(start, end);
    }
}

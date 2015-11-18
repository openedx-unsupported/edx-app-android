package org.edx.mobile.discussion;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
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
                                                @NonNull final IAuthorData authorData,
                                                @NonNull final Runnable onAuthorClickListener) {
        final CharSequence text;
        {
            final Context context = textView.getContext();
            final CharSequence formattedTime;
            {
                if (System.currentTimeMillis() - authorData.getCreatedAt().getTime()
                        < DateUtils.SECOND_IN_MILLIS) {
                    formattedTime = context.getString(R.string.just_now);
                } else {
                    formattedTime = DateUtils.getRelativeTimeSpanString(
                            authorData.getCreatedAt().getTime(),
                            System.currentTimeMillis(),
                            DateUtils.SECOND_IN_MILLIS,
                            DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR);
                }
            }

            final String authorLabel = authorData.getAuthorLabel();

            final SpannableString authorSpan = new SpannableString(authorData.getAuthor());
            if (config.isUserProfilesEnabled()) {
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
                    R.string.post_attribution, new HashMap<String, CharSequence>() {{
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

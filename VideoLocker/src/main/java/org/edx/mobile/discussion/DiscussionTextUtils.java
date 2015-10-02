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

import org.edx.mobile.R;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.Router;

import java.util.HashMap;

public abstract class DiscussionTextUtils {
    private DiscussionTextUtils() {
    }

    public static void setAuthorAttributionText(@NonNull TextView textView, @NonNull final IAuthorData authorData, @NonNull final Router router) {
        final CharSequence text;
        {
            final CharSequence formattedTime = DateUtils.getRelativeTimeSpanString(
                    authorData.getCreatedAt().getTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR);

            final String authorLabel = authorData.getAuthorLabel();

            final Context context = textView.getContext();
            final SpannableString authorSpan = new SpannableString(authorData.getAuthor());
            authorSpan.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    router.showUserProfile(context, authorData.getAuthor());
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setColor(context.getResources().getColor(R.color.edx_brand_primary_base));
                }
            }, 0, authorSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            authorSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, authorSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            text = trim(ResourceUtil.getFormattedString(context.getResources(), R.string.post_attribution, new HashMap<String, CharSequence>() {{
                put("time", formattedTime);
                put("author", authorSpan);
                put("author_label", null == authorLabel ? "" : authorLabel.toUpperCase(context.getResources().getConfiguration().locale));
            }}));
        }

        textView.setText(text);
        textView.setMovementMethod(new LinkMovementMethod()); // Allows ClickableSpan to trigger clicks
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

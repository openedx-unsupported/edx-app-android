package org.edx.mobile.discussion;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.widget.TextViewCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.ResourceUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

public abstract class DiscussionTextUtils {
    @Inject
    private static Config config;

    private DiscussionTextUtils() {
    }

    /**
     * Encapsulates the two variants of author attribution strings, i.e. with and without authorLabel.
     */
    public enum AuthorAttributionLabel {
        POST(R.string.post_attribution, R.string.post_attribution_without_label),
        ANSWER(R.string.answer_author_attribution, R.string.answer_author_attribution_without_label),
        ENDORSEMENT(R.string.endorser_attribution, R.string.endorser_attribution_without_label);

        @StringRes
        private final int stringRes, noLabelStringRes;

        AuthorAttributionLabel(@StringRes int stringRes, @StringRes int noLabelStringRes) {
            this.stringRes = stringRes;
            this.noLabelStringRes = noLabelStringRes;
        }

        /**
         * @return The string resource with authorLabel included.
         */
        @StringRes
        public int getStringRes() {
            return stringRes;
        }

        /**
         * @return The string resource without the authorLabel.
         */
        @StringRes
        public int getNoLabelStringRes() {
            return noLabelStringRes;
        }
    }

    public static void setAuthorAttributionText(@NonNull TextView textView,
                                                @NonNull AuthorAttributionLabel authorAttributionLabel,
                                                @NonNull final IAuthorData authorData,
                                                @NonNull final Runnable onAuthorClickListener) {
        setAuthorAttributionText(textView, authorAttributionLabel, authorData,
                System.currentTimeMillis(), onAuthorClickListener);
    }


    public static void setAuthorAttributionText(@NonNull TextView textView,
                                                @NonNull AuthorAttributionLabel authorAttributionLabel,
                                                @NonNull final IAuthorData authorData,
                                                long initialTimeStampMs,
                                                @NonNull final Runnable onAuthorClickListener) {
        final CharSequence text;
        {
            final Context context = textView.getContext();
            final Date dateCreated = authorData.getCreatedAt();
            final CharSequence formattedTime = dateCreated == null ? null :
                    getRelativeTimeSpanString(context, initialTimeStampMs, dateCreated.getTime());

            final String author = authorData.getAuthor();
            if (TextUtils.isEmpty(author)) {
                switch (authorAttributionLabel) {
                    case POST:
                        text = formattedTime;
                        break;
                    default:
                        text = null;
                        break;
                }
            } else {
                final String authorLabel = authorData.getAuthorLabel();

                final SpannableString authorSpan = new SpannableString(author);
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

                @StringRes int finalStringRes;
                HashMap<String, CharSequence> valuesMap = new HashMap<>();
                valuesMap.put("time", formattedTime);
                valuesMap.put("author", authorSpan);
                if (authorLabel != null) {
                    finalStringRes = authorAttributionLabel.getStringRes();
                    valuesMap.put("author_label", authorLabel);
                } else {
                    finalStringRes = authorAttributionLabel.getNoLabelStringRes();
                }
                CharSequence formattedText = trim(ResourceUtil.getFormattedString(
                        context.getResources(), finalStringRes, valuesMap));
                // If time is not available, then reduce the whitespaces
                // surrounding it's placeholder in the template to one.
                if (TextUtils.isEmpty(formattedTime)) {
                    formattedText = removeDuplicateWhitespace(formattedText);
                }
                text = formattedText;
            }
        }

        textView.setText(text);
        if (TextUtils.isEmpty(text)) {
            textView.setVisibility(View.GONE);
        } else {
            // Allow ClickableSpan to trigger clicks
            textView.setMovementMethod(new LinkMovementMethod());
        }
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

    public static CharSequence removeDuplicateWhitespace(CharSequence text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        for (int size = text.length(), i = 0; i < size; i++) {
            if (Character.isWhitespace(builder.charAt(i))) {
                int nextIndex = i + 1;
                if (nextIndex < size && Character.isWhitespace(builder.charAt(nextIndex))) {
                    builder.replace(i, nextIndex, " ");
                    break;
                }
            }
        }
        return builder;
    }

    public static void setEndorsedState(@NonNull TextView target,
                                        @NonNull DiscussionThread thread,
                                        @NonNull DiscussionComment response) {
        if (response.isEndorsed()) {
            Context context = target.getContext();
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    target,
                    new IconDrawable(context, FontAwesomeIcons.fa_check_square_o)
                            .sizeRes(context, R.dimen.edx_xxx_small)
                            .colorRes(context, R.color.edx_utility_success),
                    null, null, null);
            switch (thread.getType()) {
                case QUESTION:
                    target.setText(R.string.discussion_responses_answer);
                    break;
                case DISCUSSION:
                default:
                    target.setText(R.string.discussion_responses_endorsed);
                    break;
            }
            target.setVisibility(View.VISIBLE);
        }

    }
}

package org.edx.mobile.discussion;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.widget.TextViewCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.ResourceUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class DiscussionTextUtils {
    @Inject
    private static Config config;

    private DiscussionTextUtils() {
    }

    public enum AuthorAttributionLabel {POST, ANSWER, ENDORSEMENT}

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
            List<CharSequence> joinableStrings = new ArrayList<>();
            boolean isEndorsed = false;
            switch (authorAttributionLabel) {
                case ANSWER:
                    isEndorsed = true;
                    joinableStrings.add(context.getString(R.string.discussion_post_marked_as_answer));
                    break;
                case ENDORSEMENT:
                    isEndorsed = true;
                    joinableStrings.add(context.getString(R.string.discussion_post_endorsed));
                    break;
            }

            final Date dateCreated = authorData.getCreatedAt();
            if (dateCreated != null) {
                joinableStrings.add(getRelativeTimeSpanString(context, initialTimeStampMs,
                        dateCreated.getTime()));
            }

            final String author = authorData.getAuthor();
            if (!TextUtils.isEmpty(author)) {
                final SpannableString authorSpan = new SpannableString(author);
                if (config.isUserProfilesEnabled() && !authorData.isAuthorAnonymous()) {
                    // Change the author text color and style
                    authorSpan.setSpan(new ForegroundColorSpan(
                                    context.getResources().getColor(R.color.edx_brand_primary_base)),
                            0, authorSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    authorSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, authorSpan.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // Set the click listener on the whole textView
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onAuthorClickListener.run();
                        }
                    });
                } else {
                    textView.setOnClickListener(null);
                    textView.setClickable(false);
                }
                joinableStrings.add(ResourceUtil.getFormattedString(context.getResources(),
                        R.string.discussion_post_author_attribution, "author", authorSpan));
            }

            final String authorLabel = authorData.getAuthorLabel();
            if (!TextUtils.isEmpty(authorLabel)) {
                joinableStrings.add(ResourceUtil.getFormattedString(context.getResources(),
                        R.string.discussion_post_author_label_attribution, "text", authorLabel));
            }

            int joinableStringsSize = joinableStrings.size();
            if (joinableStringsSize == 0 || (isEndorsed && joinableStringsSize == 1)) {
                text = null;
            } else {
                text = org.edx.mobile.util.TextUtils.join(" ", joinableStrings);
            }
        }
        if (TextUtils.isEmpty(text)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(text);
        }
    }

    public static CharSequence getRelativeTimeSpanString(@NonNull Context context, long nowMs,
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

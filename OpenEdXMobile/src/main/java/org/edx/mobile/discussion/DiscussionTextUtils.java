package org.edx.mobile.discussion;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.model.discussion.DiscussionComment;
import org.edx.mobile.model.discussion.DiscussionThread;
import org.edx.mobile.model.discussion.IAuthorData;
import org.edx.mobile.util.ResourceUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class DiscussionTextUtils {

    private DiscussionTextUtils() {
    }

    public enum AuthorAttributionLabel {ANSWER, ENDORSEMENT}

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
                if (MainApplication.getEnvironment(context).getConfig().isUserProfilesEnabled() && !authorData.isAuthorAnonymous()) {
                    // Change the author text color and style
                    authorSpan.setSpan(new ForegroundColorSpan(context.getResources().
                                    getColor(R.color.primaryBaseColor)), 0,
                            authorSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    public static void setAuthorText(@NonNull TextView textView, @NonNull IAuthorData authorData) {
        final CharSequence authorText;
        {
            final Context context = textView.getContext();
            List<CharSequence> joinableStrings = new ArrayList<>();
            final String author = authorData.getAuthor();
            if (!TextUtils.isEmpty(author)) {
                final SpannableString authorSpan = new SpannableString(author);
                // Change the author text color and style
                if (!authorData.isAuthorAnonymous()) {
                    authorSpan.setSpan(new ForegroundColorSpan(context.getResources().
                                    getColor(R.color.primaryBaseColor)), 0,
                            authorSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                authorSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, authorSpan.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                joinableStrings.add(authorSpan);
            }

            final String authorLabel = authorData.getAuthorLabel();
            if (!TextUtils.isEmpty(authorLabel)) {
                joinableStrings.add(ResourceUtil.getFormattedString(context.getResources(),
                        R.string.discussion_post_author_label_attribution, "text", authorLabel));
            }

            authorText = org.edx.mobile.util.TextUtils.join(" ", joinableStrings);
        }
        if (TextUtils.isEmpty(authorText)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(authorText);
        }
    }

    public static CharSequence getRelativeTimeSpanString(@NonNull Context context, long nowMs,
                                                         long timeMs) {
        return getRelativeTimeSpanString(context, nowMs, timeMs,
                DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR);
    }

    public static CharSequence getRelativeTimeSpanString(@NonNull Context context, long nowMs,
                                                         long timeMs, int flags) {
        if (nowMs - timeMs < DateUtils.SECOND_IN_MILLIS) {
            return context.getString(R.string.just_now);
        } else {
            return DateUtils.getRelativeTimeSpanString(timeMs, nowMs,
                    DateUtils.SECOND_IN_MILLIS, flags);
        }
    }

    public static void setEndorsedState(@NonNull TextView target,
                                        @NonNull DiscussionThread thread,
                                        @NonNull DiscussionComment response) {
        if (response.isEndorsed()) {
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

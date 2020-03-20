package org.edx.mobile.discussion;

import android.content.Context;
import androidx.annotation.StringRes;
import androidx.core.widget.TextViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;

public abstract class DiscussionUtils {
    /**
     * Sets the state, text and icon of the new item creation button on discussion screens
     *
     * @param isTopicClosed     Boolean if the topic is closed or not
     * @param textView          The TextView whose text has to be updated
     * @param positiveTextResId The text resource to be applied when topic IS NOT closed
     * @param negativeTextResId The text resource to be applied when topic IS closed
     * @param creationLayout    The layout which should be enabled/disabled and applied listener to
     * @param listener          The listener to apply to creationLayout
     */
    public static void setStateOnTopicClosed(boolean isTopicClosed, TextView textView,
                                             @StringRes int positiveTextResId,
                                             @StringRes int negativeTextResId,
                                             ViewGroup creationLayout,
                                             View.OnClickListener listener) {
        Context context = textView.getContext();
        if (isTopicClosed) {
            textView.setText(negativeTextResId);
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(textView,
                    new IconDrawable(context, FontAwesomeIcons.fa_lock)
                            .sizeRes(context, R.dimen.small_icon_size)
                            .colorRes(context, R.color.white),
                    null, null, null
            );
            creationLayout.setOnClickListener(null);
        } else {
            textView.setText(positiveTextResId);
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(textView,
                    new IconDrawable(context, FontAwesomeIcons.fa_plus_circle)
                            .sizeRes(context, R.dimen.small_icon_size)
                            .colorRes(context, R.color.white),
                    null, null, null
            );
            creationLayout.setOnClickListener(listener);
        }
        creationLayout.setEnabled(!isTopicClosed);
    }
}

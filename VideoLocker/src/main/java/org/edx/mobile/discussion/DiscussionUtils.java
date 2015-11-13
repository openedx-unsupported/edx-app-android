package org.edx.mobile.discussion;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v4.widget.TextViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.third_party.iconify.IconDrawable;
import org.edx.mobile.third_party.iconify.Iconify;

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
            TextViewCompat.setCompoundDrawablesRelative(textView,
                    new IconDrawable(context, Iconify.IconValue.fa_lock)
                            .sizeRes(context, R.dimen.icon_view_standard_width_height)
                            .colorRes(context, R.color.edx_grayscale_neutral_white_t),
                    null, null, null
            );
            creationLayout.setOnClickListener(null);
        } else {
            textView.setText(positiveTextResId);
            TextViewCompat.setCompoundDrawablesRelative(textView,
                    new IconDrawable(textView.getContext(), Iconify.IconValue.fa_plus_circle)
                            .sizeRes(context, R.dimen.icon_view_standard_width_height)
                            .colorRes(context, R.color.edx_grayscale_neutral_white_t),
                    null, null, null
            );
            creationLayout.setOnClickListener(listener);
        }
        creationLayout.setEnabled(!isTopicClosed);
    }
}

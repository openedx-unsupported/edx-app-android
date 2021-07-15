package org.edx.mobile.discussion;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.StringRes;

import org.edx.mobile.R;
import org.edx.mobile.util.UiUtils;

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
            UiUtils.INSTANCE.setTextViewDrawableStart(context, textView, R.drawable.ic_lock,
                    R.dimen.small_icon_size);
            creationLayout.setOnClickListener(null);
        } else {
            textView.setText(positiveTextResId);
            UiUtils.INSTANCE.setTextViewDrawableStart(context, textView, R.drawable.ic_add_comment,
                    R.dimen.small_icon_size);
            creationLayout.setOnClickListener(listener);
        }
        creationLayout.setEnabled(!isTopicClosed);
    }
}

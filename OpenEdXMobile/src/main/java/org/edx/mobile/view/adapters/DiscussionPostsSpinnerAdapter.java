package org.edx.mobile.view.adapters;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;

import org.edx.mobile.R;
import org.edx.mobile.interfaces.TextResourceProvider;
import org.edx.mobile.util.UiUtils;
import org.edx.mobile.view.CourseDiscussionPostsThreadFragment;

/**
 * Adapter for the filter and sort spinners in {@link CourseDiscussionPostsThreadFragment}
 */
public class DiscussionPostsSpinnerAdapter extends ArrayAdapter<TextResourceProvider> {

    private static final int[] ACTIVATED_STATE_SET = new int[]{android.R.attr.state_activated};

    @DrawableRes
    private final int iconResId;

    // We need a reference to the Spinner that this adapter is set to, in
    // order to generate dropdown items that match it's content height. Since
    // the content height is the result of subtracting the popup background's
    // vertical padding from it's parent's height (this margin is set up on
    // the Spinner in order to properly align the popup), it's best to
    // calculate it dynamically instead of defining it in another dimen
    // resource, which would be subject to desynchronization from it's
    // sources.
    @NonNull
    private final Spinner spinner;

    public DiscussionPostsSpinnerAdapter(@NonNull Spinner spinner,
                                         @NonNull TextResourceProvider[] textResourceProviders,
                                         @DrawableRes int iconResId) {
        super(spinner.getContext(), R.layout.row_discussion_thread_dropdown, textResourceProviders);
        this.iconResId = iconResId;
        this.spinner = spinner;
    }

    @Override
    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView textView = initTextView(position, convertView, parent);
        if (textView != convertView) {
            UiUtils.INSTANCE.setTextViewDrawableStart(textView.getContext(), textView, iconResId,
                    R.dimen.small_icon_size);
        }
        return textView;
    }

    @Override
    @NonNull
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        TextView textView = initTextView(position, convertView, parent);
        if (textView != convertView) {
            Drawable icon = UiUtils.INSTANCE.getDrawable(getContext(), iconResId,
                    R.dimen.small_icon_size);
            StateListDrawable statefulIcon = new StateListDrawable();
            statefulIcon.setBounds(icon.getBounds());
            statefulIcon.addState(ACTIVATED_STATE_SET, icon);
            TextViewCompat.setCompoundDrawablesRelative(
                    textView, statefulIcon, null, null, null);
        }
        ViewGroup.LayoutParams layoutParams = textView.getLayoutParams();
        if (layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
            layoutParams.height = spinner.getHeight() -
                    spinner.getPaddingTop() - spinner.getPaddingBottom();
        }
        return textView;
    }

    // Initialize the TextView, pending the assignment of the (stateful or
    // stateless) icon.
    @NonNull
    private TextView initTextView(int position, @Nullable View convertView,
                                  @NonNull ViewGroup parent) {
        TextView textView = (TextView) (convertView != null ? convertView :
                LayoutInflater.from(getContext()).inflate(
                        R.layout.row_discussion_thread_dropdown, parent, false));
        textView.setText(getItem(position).getTextResource());
        return textView;
    }
}

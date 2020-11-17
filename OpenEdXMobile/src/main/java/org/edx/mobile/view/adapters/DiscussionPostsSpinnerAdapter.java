package org.edx.mobile.view.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;

import org.edx.mobile.R;
import org.edx.mobile.interfaces.TextResourceProvider;
import org.edx.mobile.view.CourseDiscussionPostsThreadFragment;

/**
 * Adapter for the filter and sort spinners in {@link CourseDiscussionPostsThreadFragment}
 */
public class DiscussionPostsSpinnerAdapter extends ArrayAdapter<TextResourceProvider> {
    // Since the sort icon is constructed by combining two IconDrawable items,
    // we need to accept a DrawableFactory for the icon instead of just an
    // Iconify.IconValue item.
    public interface IconDrawableFactory {
        @NonNull
        Drawable createIcon();
    }

    private static class IconDrawableFactoryImpl implements IconDrawableFactory {
        @NonNull
        private final Context context;
        @NonNull
        private final Icon icon;

        IconDrawableFactoryImpl(@NonNull Context context, @NonNull Icon icon) {
            this.context = context;
            this.icon = icon;
        }

        @Override
        @NonNull
        public IconDrawable createIcon() {
            return new IconDrawable(context, icon)
                    .sizeRes(context, R.dimen.small_icon_size)
                    .colorRes(context, R.color.primaryBaseColor);
        }
    }

    private static final int[] ACTIVATED_STATE_SET =
            new int[] { android.R.attr.state_activated };

    @NonNull
    private final IconDrawableFactory iconDrawableFactory;

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
            @NonNull Icon icon) {
        this(spinner, textResourceProviders,
                new IconDrawableFactoryImpl(spinner.getContext(), icon));
    }

    public DiscussionPostsSpinnerAdapter(@NonNull Spinner spinner,
            @NonNull TextResourceProvider[] textResourceProviders,
            @NonNull IconDrawableFactory iconDrawableFactory) {
        super(spinner.getContext(), R.layout.row_discussion_thread_dropdown, textResourceProviders);
        this.iconDrawableFactory = iconDrawableFactory;
        this.spinner = spinner;
    }

    // The bounds are being set up manually instead of depending on the intrinsic
    // dimensions because the constructed sort icon has non-square dimensions,
    // which is not possible to set in IconDrawable.
    @NonNull
    private Drawable createIcon() {
        Drawable icon = iconDrawableFactory.createIcon();
        if (icon.getIntrinsicWidth() >= 0 && icon.getIntrinsicHeight() >= 0 &&
                icon.getBounds().isEmpty()) {
            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        }
        return icon;
    }

    @Override
    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView textView = initTextView(position, convertView, parent);
        if (textView != convertView) {
            TextViewCompat.setCompoundDrawablesRelative(
                    textView, createIcon(), null, null, null);
        }
        return textView;
    }

    @Override
    @NonNull
    public View getDropDownView(int position, @Nullable View convertView,
            @NonNull ViewGroup parent) {
        TextView textView = initTextView(position, convertView, parent);
        if (textView != convertView) {
            Drawable icon = createIcon();
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

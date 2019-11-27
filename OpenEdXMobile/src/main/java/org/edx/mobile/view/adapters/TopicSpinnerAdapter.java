package org.edx.mobile.view.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.discussion.DiscussionTopicDepth;
import org.edx.mobile.util.ResourceUtil;

import java.util.ArrayList;
import java.util.List;

public class TopicSpinnerAdapter extends ArrayAdapter<DiscussionTopicDepth> {

    private final int basePadding = getContext().getResources().getDimensionPixelSize(R.dimen.widget_margin);
    private final int extraPaddingPerLevel = getContext().getResources().getDimensionPixelSize(R.dimen.edx_margin);

    public TopicSpinnerAdapter(@NonNull Context context, @NonNull List<DiscussionTopicDepth> objects) {
        super(context, 0, new ArrayList<DiscussionTopicDepth>());
        addAll(objects);
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final TextView view;
        if (convertView == null) {
            view = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.edx_spinner_item, parent, false);
        } else {
            view = (TextView) convertView;
        }
        final DiscussionTopicDepth topic = getItem(position);
        view.setText(ResourceUtil.getFormattedString(getContext().getResources(),
                R.string.discussion_add_post_topic_selection, "topic",
                topic.getDiscussionTopic().getName()));
        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final TextView view;
        if (convertView == null) {
            view = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.edx_spinner_dropdown_item, parent, false);
        } else {
            view = (TextView) convertView;
        }
        final DiscussionTopicDepth topic = getItem(position);
        final int extraLeftPadding = topic.getDepth() * extraPaddingPerLevel;
        ViewCompat.setPaddingRelative(view, basePadding + extraLeftPadding, view.getPaddingTop(),
                basePadding, view.getPaddingBottom());
        view.setText(topic.getDiscussionTopic().getName());
        view.setEnabled(isEnabled(position));
        return view;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        final DiscussionTopicDepth item = getItem(position);
        return null != item && item.isPostable();
    }

    /**
     * @return The position of the specified topic, or -1 if not found
     */
    public int getPosition(@NonNull DiscussionTopic discussionTopic) {
        for (int i = 0; i < getCount(); ++i) {
            final DiscussionTopicDepth item = getItem(i);
            if (item.isPostable() && discussionTopic.hasSameId(item.getDiscussionTopic())) {
                return i;
            }
        }
        return -1;
    }
}

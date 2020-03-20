package org.edx.mobile.view.adapters;

import android.content.Context;
import androidx.core.view.ViewCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.discussion.DiscussionTopicDepth;

public class DiscussionTopicsAdapter extends BaseListAdapter<DiscussionTopicDepth> {

    private final int childPadding;

    @Inject
    public DiscussionTopicsAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_discussion_topic, environment);
        childPadding = context.getResources().getDimensionPixelOffset(R.dimen.edx_margin);
    }

    @Override
    public void render(BaseViewHolder tag, DiscussionTopicDepth discussionTopic) {
        ViewHolder holder = (ViewHolder) tag;
        holder.discussionTopicNameTextView.setText(discussionTopic.getDiscussionTopic().getName());
        ViewCompat.setPaddingRelative(holder.discussionTopicNameTextView, childPadding * (1 + discussionTopic.getDepth()), childPadding, childPadding, childPadding);
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        return new ViewHolder(convertView);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    private static class ViewHolder extends BaseViewHolder {
        final TextView discussionTopicNameTextView;

        private ViewHolder(View view) {
            this.discussionTopicNameTextView = (TextView) view.findViewById(R.id.discussion_topic_name_text_view);
        }
    }

}

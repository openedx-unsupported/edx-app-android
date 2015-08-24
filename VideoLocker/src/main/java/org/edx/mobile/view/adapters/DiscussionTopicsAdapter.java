package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.inject.Inject;
import org.edx.mobile.discussion.DiscussionTopicDepth;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;

public class DiscussionTopicsAdapter extends BaseListAdapter<DiscussionTopicDepth> {

    private static final int CHILD_INDENT_LEVEL = 100;

    @Inject
    public DiscussionTopicsAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_discussion_topic, environment);
    }

    @Override
    public void render(BaseViewHolder tag, DiscussionTopicDepth discussionTopic) {
        ViewHolder holder = (ViewHolder) tag;

        String topicName = discussionTopic.getDiscussionTopic().getName();
        holder.discussionTopicNameTextView.setText(topicName);

        int childIndentLevel = discussionTopic.getDepth() * CHILD_INDENT_LEVEL;
        holder.discussionTopicNameTextView.setPadding(childIndentLevel == 0 ? 30 : childIndentLevel, 0, 0, 0);
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.discussionTopicNameTextView = (TextView) convertView
                .findViewById(R.id.discussion_topic_name_text_view);
        return holder;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {}

    private static class ViewHolder extends BaseViewHolder {
        TextView discussionTopicNameTextView;
    }

}
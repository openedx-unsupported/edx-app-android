package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.DiscussionTopic;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.view.custom.SocialFacePileView;

public class DiscussionTopicsAdapter extends BaseListAdapter<DiscussionTopic> {

    @Inject
    public DiscussionTopicsAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_discussion_topic, environment);
    }

    @Override
    public void render(BaseViewHolder tag, DiscussionTopic discussionTopic) {
        ViewHolder holder = (ViewHolder) tag;

        String topicName = discussionTopic.getName();
        holder.discussionTopicNameTextView.setText(topicName);
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.discussionTopicNameTextView = (TextView) convertView
                .findViewById(R.id.discussion_topic_name_text_view);
        return holder;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    private static class ViewHolder extends BaseViewHolder {
        TextView discussionTopicNameTextView;
    }


}

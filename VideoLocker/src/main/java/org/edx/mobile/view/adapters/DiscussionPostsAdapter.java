package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.DiscussionThread;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;

public class DiscussionPostsAdapter extends BaseListAdapter<DiscussionThread> {

    @Inject
    public DiscussionPostsAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_discussion_thread, environment);
    }

    @Override
    public void render(BaseViewHolder tag, DiscussionThread discussionThread) {
        ViewHolder holder = (ViewHolder) tag;

        String threadTitle = discussionThread.getTitle();
        holder.discussionThreadTitle.setText(threadTitle);
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.discussionThreadTitle = (TextView) convertView
                .findViewById(R.id.discussion_thread_title);
        return holder;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {}

    private static class ViewHolder extends BaseViewHolder {
        TextView discussionThreadTitle;
    }
}

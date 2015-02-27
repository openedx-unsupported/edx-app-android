package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.social.SocialGroup;
import org.edx.mobile.view.custom.SocialFacePileView;

import java.util.List;

public class GroupListAdapter extends SimpleAdapter<SocialGroup>  {

    private GroupFriendsListener groupFriendsListener;

    public GroupListAdapter(Context context, GroupFriendsListener groupFriendsListener) {
        super(context);
        this.groupFriendsListener = groupFriendsListener;
    }

    public GroupListAdapter(Context context, GroupFriendsListener groupFriendsListener, List<SocialGroup> data) {
        super(context, data);
        this.groupFriendsListener = groupFriendsListener;
    }

    public interface GroupFriendsListener {
        public void fetchGroupFriends(SocialGroup socialGroup);
    }

    @Override
    protected int getRowLayout() {
        return R.layout.group_list_item;
    }

    protected void setUpView(View view, final SocialGroup item) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (viewHolder == null) {

            viewHolder = new ViewHolder();
            viewHolder.txtName = (TextView) view.findViewById(R.id.group_name);
            viewHolder.txtDetails = (TextView) view.findViewById(R.id.group_details);
            viewHolder.facePileView = (SocialFacePileView) view.findViewById(R.id.group_list_face_pile);
            viewHolder.txtUnreadDisplay = (TextView) view.findViewById(R.id.group_unread);
            view.setTag(viewHolder);

        }

        if (item != null) {

            viewHolder.txtName.setText(item.getName());
            viewHolder.txtDetails.setText("");
            viewHolder.facePileView.clearAvatars();

            if (item.getUnread() > 0) {
                viewHolder.txtUnreadDisplay.setText(Integer.toString(item.getUnread()));
                viewHolder.txtUnreadDisplay.setVisibility(View.VISIBLE);
            } else {
                viewHolder.txtUnreadDisplay.setVisibility(View.GONE);
            }

            if (item.getMembers() == null) {
                if (groupFriendsListener != null){
                    groupFriendsListener.fetchGroupFriends(item);
                }
            } else {
                String strDetails = context.getResources().getString(R.string.group_list_members, item.getMembers().size());
                viewHolder.txtDetails.setText(strDetails);
                viewHolder.facePileView.setMemberList(item.getMembers());
            }

        }

    }

    private class ViewHolder {
        private TextView txtName;
        private TextView txtDetails;
        private SocialFacePileView facePileView;
        private TextView txtUnreadDisplay;
    }

}

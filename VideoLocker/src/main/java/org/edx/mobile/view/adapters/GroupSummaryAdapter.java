package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.social.SocialMember;

import java.util.List;
import java.util.Set;

/**
 * Created by marcashman on 2014-12-15.
 */
public class GroupSummaryAdapter extends FriendListAdapter {

    public GroupSummaryAdapter(Context context) {
        super(context);
    }

    public GroupSummaryAdapter(Context context, List<SocialMember> data) {
        super(context, data);
    }

    public GroupSummaryAdapter(Context context, List<SocialMember> data, Set<Long> alreadyInGroupList) {
        super(context, data, alreadyInGroupList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflate(R.layout.friend_list_item_nosselect, parent);
        }
        setUpView(convertView, getItem(position));
        return convertView;

    }

}

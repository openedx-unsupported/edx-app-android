package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.social.SocialMember;

import java.util.List;

public class FriendsInCourseAdapter extends FriendListAdapter {

    public FriendsInCourseAdapter(Context context) {
        super(context);
    }

    public FriendsInCourseAdapter(Context context, List<SocialMember> data) {
        super(context, data);
    }

    @Override
    protected void setUpView(View view, SocialMember item) {
        super.setUpView(view, item);

        view.setOnClickListener(null);
        view.findViewById(R.id.friend_selected_check).setVisibility(View.INVISIBLE);
    }

}

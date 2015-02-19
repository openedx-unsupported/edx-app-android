package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.social.SocialGroup;
import org.edx.mobile.social.SocialMember;

import java.util.ArrayList;

public class SocialFriendPickerActivity extends BaseSingleFragmentActivity {

    private static final String TAG = SocialFriendPickerActivity.class.getCanonicalName();
    public static final String EXTRA_OPT_LIST_OF_FRIENDS = TAG + ".friends";
    public static final int PICK_FRIENDS_CREATE_GROUP_REQUEST = 1;

    public static final String ARG_FILTER_GROUP = TAG + ".filterGroup";
    public static final String ADD_TO_GROUP = TAG + ".addToGroup";

    private boolean addState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        addState = getIntent().getBooleanExtra(ADD_TO_GROUP, false);

        if (addState) {
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.stay_put);
        }

        try{
            String analyticsID = addState ? "Social Friend Picker - existing group" : "Social Friend Picker - new group";
            segIO.screenViewsTracking(analyticsID);
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (addState) {
            setTitle(getString(R.string.label_add_new_group_friends));
        } else {
            setTitle(getString(R.string.label_add_group_friends));
        }
    }

    @Override
    public Fragment getFirstFragment() {

        Bundle bundle = new Bundle();

        Fragment fragment = new SocialFriendPickerFragment();
        ArrayList<SocialMember> friends = getIntent().getParcelableArrayListExtra(EXTRA_OPT_LIST_OF_FRIENDS);

        if (friends != null) {
            bundle.putParcelableArrayList(SocialFriendPickerFragment.EXTRA_OPT_FRIENDS_LIST, friends);
        }

        SocialGroup filterGroup = getIntent().getParcelableExtra(ARG_FILTER_GROUP);
        if (filterGroup != null && filterGroup.getMembers() != null) {

            int memberCount = filterGroup.getMembers().size();
            long[] alreadyInGroup = new long[memberCount];
            for (int i = 0; i < memberCount; i++) {

                alreadyInGroup[i] = filterGroup.getMembers().get(i).getId();

            }
            bundle.putLongArray(SocialFriendPickerFragment.EXTRA_OPT_IN_GROUP, alreadyInGroup);

        }

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void finish() {
        super.finish();
        if (addState) {
            overridePendingTransition(R.anim.stay_put, R.anim.slide_out_bottom);
        }

    }
}

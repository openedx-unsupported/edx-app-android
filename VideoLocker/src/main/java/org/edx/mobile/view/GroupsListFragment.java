package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.widget.LoginButton;
import com.getbase.floatingactionbutton.AddFloatingActionButton;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.facebook.IUiLifecycleHelper;
import org.edx.mobile.social.SocialGroup;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.social.SocialProvider;
import org.edx.mobile.social.facebook.FacebookProvider;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.adapters.GroupListAdapter;

import java.util.ArrayList;
import java.util.List;

public class GroupsListFragment extends Fragment implements SocialProvider.Callback<List<SocialGroup>>, GroupListAdapter.GroupFriendsListener {

    private enum State {
        LOADING,
        FACEBOOK_DISCONNECTED,
        NO_GROUPS,
        GROUPS_LIST
    }

    private static final String TAG = GroupsListFragment.class.getCanonicalName();
    private static final String GROUP_LIST_MODELS = TAG + ".GroupListModels";
    private static final String GROUP_BRING_INVITED = TAG + ".invited_group";
    private static final int REQUEST_CREATE_GROUP = 0x0000dede;
    private static final int REQUEST_ADD_FRIENDS = 0x0000eaea;

    private static final int REQUEST_GROUP_SUMMARY = 0x000efeb;

    private final Logger logger = new Logger(GroupsListFragment.class);

    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeLayout;
    private AddFloatingActionButton addGroupFab;
    private View facebookConnectLayout;
    private View errorLayout;

    private IUiLifecycleHelper uiLifecycleHelper;
    private GroupListAdapter groupListAdapter;
    private SocialProvider groupProvider;

    private SocialGroup lastGroupToInvite;

    private ISegment segIO;

    private void launchGroup(SocialGroup group){
        Intent intent = new Intent(getActivity(), GroupSummaryActivity.class);
        intent.putExtra(GroupSummaryActivity.EXTRA_GROUP, group);
        startActivityForResult(intent, REQUEST_GROUP_SUMMARY);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupProvider = new FacebookProvider();
        groupListAdapter = new GroupListAdapter(getActivity(), this);
        if (savedInstanceState != null) {
            ArrayList<SocialGroup> groups = savedInstanceState.getParcelableArrayList(GROUP_LIST_MODELS);
            if (groups != null && !groups.isEmpty()) {
                groupListAdapter.setItems(groups);
            }
            lastGroupToInvite = savedInstanceState.getParcelable(GROUP_BRING_INVITED);
        }

        uiLifecycleHelper = IUiLifecycleHelper.Factory.getInstance(getActivity(), null);
        uiLifecycleHelper.onCreate(savedInstanceState);

        segIO = SegmentFactory.getInstance();

        try{
            segIO.screenViewsTracking("Group List");
        }catch(Exception e){
            logger.error(e);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_groups_list, container, false);

        ListView listView = (ListView) view.findViewById(R.id.my_groups_list);
        listView.setAdapter(groupListAdapter);
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SocialGroup item = groupListAdapter.getItem(i);
                launchGroup(item);
            }
        });

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setColorSchemeColors(getResources().getIntArray(R.array.swipeRefreshColors));
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                groupListAdapter.clearAll();
                groupProvider.getMyGroups(getActivity(), GroupsListFragment.this);
                showState(State.LOADING);
            }
        });

        LoginButton loginButton = (LoginButton) view.findViewById(R.id.authButton);
        loginButton.setFragment(this);

        addGroupFab = (AddFloatingActionButton) view.findViewById(R.id.group_fab);
        addGroupFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateGroupActivity.class);
                startActivityForResult(intent, REQUEST_CREATE_GROUP);
            }
        });

        addGroupFab.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if(addGroupFab.getViewTreeObserver().isAlive() && addGroupFab.getHeight() > 0 && getView() != null) {
                    addGroupFab.setY(getView().getHeight() + 40);
                    addGroupFab.animate().translationY(0).setStartDelay(200).setDuration(520).setInterpolator(new AnticipateOvershootInterpolator()).start();
                    addGroupFab.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

            }
        });

        progressBar = (ProgressBar) view.findViewById(R.id.api_spinner);
        errorLayout = view.findViewById(R.id.error_layout);
        facebookConnectLayout = view.findViewById(R.id.layout_connect_facebook);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiLifecycleHelper.onActivityResult(requestCode, resultCode, data);

        if (resultCode == GroupSummaryFragment.RESULT_FRIEND_ADDED) {

            SocialGroup updatedGroup = data.getParcelableExtra(GroupSummaryFragment.ARG_GROUP);

            List<SocialGroup> groupItems = groupListAdapter.getItems();
            int groupIndex = groupItems.indexOf(updatedGroup);
            if (groupIndex >= 0){
                groupItems.remove(groupIndex);
                groupItems.add(groupIndex, updatedGroup);
                groupListAdapter.notifyDataSetChanged();
            }

        } else if (requestCode == REQUEST_ADD_FRIENDS && resultCode == Activity.RESULT_OK) {

            List<SocialMember> toInvite = data.getParcelableArrayListExtra(SocialFriendPickerFragment.RESULT_FRIEND_LIST);
            inviteFriendsToGroup(lastGroupToInvite.getId(), toInvite);

        } else if (requestCode == REQUEST_CREATE_GROUP && resultCode == Activity.RESULT_OK) {

            List<SocialMember> friends = data.getParcelableArrayListExtra(CreateGroupFragment.RESULT_FRIENDS);
            String desc = data.getStringExtra(CreateGroupFragment.RESULT_DESC);
            String name = data.getStringExtra(CreateGroupFragment.RESULT_NAME);

            makeNewGroup(name, desc, friends);

        } else if (requestCode != REQUEST_CREATE_GROUP &&
            Session.getActiveSession().isOpened()) {
            groupProvider.getMyGroups(getActivity(), this);
            showState(State.LOADING);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        uiLifecycleHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiLifecycleHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiLifecycleHelper.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        uiLifecycleHelper.onStop();
    }

    private void inviteFriendsToGroup(final long groupID, final List<SocialMember> friends) {
        if (friends != null && !friends.isEmpty()) {


            FacebookProvider fbProvider  = new FacebookProvider();
            fbProvider.inviteFriendsListToGroup(getActivity(),
                    groupID,
                    friends,
                    new SocialProvider.Callback<Void>() {
                        @Override
                        public void onSuccess(Void response) {

                            try{
                                segIO.groupCreated(groupID, friends.size());
                            }catch(Exception e){
                                logger.error(e);
                            }

                            refreshAfterGroupCreate(getString(R.string.group_created));
                        }

                        @Override
                        public void onError(SocialProvider.SocialError err) {
                            //
                        }
                    });
        }
    }

    private void refreshAfterGroupCreate(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        groupProvider.getMyGroups(getActivity(), GroupsListFragment.this);
        showState(State.LOADING);
    }

    private void makeNewGroup(final String groupName, final String description, final List<SocialMember> membersToInvite) {

        final SocialProvider.Callback<Long> createGroupCallback = new SocialProvider.Callback<Long>() {
            @Override
            public void onSuccess(Long groupID) {
                if (getActivity() == null) {
                    return;
                }

                if (membersToInvite != null && membersToInvite.size() > 0){
                    inviteFriendsToGroup(groupID, membersToInvite);
                } else {
                    refreshAfterGroupCreate(getString(R.string.group_created));
                }

            }

            @Override
            public void onError(SocialProvider.SocialError err) {
                showError(err);
            }
        };

        FacebookProvider fbProvider = new FacebookProvider();
        fbProvider.getUser(getActivity(), new SocialProvider.Callback<SocialMember>() {
            @Override
            public void onSuccess(SocialMember response) {

                String groupAdmin = String.valueOf(response.getId());
                FacebookProvider fbProvider = new FacebookProvider();

                //TODO This line needs to be removed when/if API implements adding admin on group create
                membersToInvite.add(new SocialMember(response.getId(), "admin"));

                fbProvider.createNewGroup(getActivity(),
                        groupName,
                        description,
                        groupAdmin,
                        createGroupCallback);
            }

            @Override
            public void onError(SocialProvider.SocialError err) {
                showError(err);
            }
        });

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (groupProvider.isLoggedIn() && NetworkUtil.isConnected(getActivity())) {
            groupProvider.getMyGroups(getActivity(), this);
            showState(State.LOADING);
        } else {
            showState(State.FACEBOOK_DISCONNECTED);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiLifecycleHelper.onSaveInstanceState(outState);
        if (!groupListAdapter.isEmpty()) {
            outState.putParcelableArrayList(GROUP_LIST_MODELS, new ArrayList<Parcelable>(groupListAdapter.getItems()));
        }
        if (lastGroupToInvite != null) {
            outState.putParcelable(GROUP_BRING_INVITED, lastGroupToInvite);
        }
    }

    private void showState(State state) {
        swipeLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        facebookConnectLayout.setVisibility(View.GONE);
        addGroupFab.setVisibility(View.GONE);

        switch (state) {
            case LOADING:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case FACEBOOK_DISCONNECTED:
                facebookConnectLayout.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.VISIBLE);
                break;
            case NO_GROUPS:
                errorLayout.setVisibility(View.VISIBLE);
                addGroupFab.setVisibility(View.VISIBLE);
                break;
            case GROUPS_LIST:
                addGroupFab.setVisibility(View.VISIBLE);
                swipeLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onSuccess(List<SocialGroup> response) {
        groupListAdapter.setItems(response);
        swipeLayout.setRefreshing(false);
        if (groupListAdapter.isEmpty()) {
            showState(State.NO_GROUPS);
        } else {
            showState(State.GROUPS_LIST);
        }
    }

    @Override
    public void onError(SocialProvider.SocialError err) {
        showError(err);
    }

    private void showError(SocialProvider.SocialError error) {
        Toast.makeText(getActivity(), R.string.error_contact_FB, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void fetchGroupFriends(final SocialGroup socialGroup) {

        FacebookProvider fbProvider = new FacebookProvider();
        fbProvider.getGroupMembers(getActivity(), socialGroup, new SocialProvider.Callback<List<SocialMember>>() {

            @Override
            public void onSuccess(List<SocialMember> response) {
                socialGroup.setMembers(response);
                groupListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(SocialProvider.SocialError err) {
                showError(err);
            }
        });

    }

}

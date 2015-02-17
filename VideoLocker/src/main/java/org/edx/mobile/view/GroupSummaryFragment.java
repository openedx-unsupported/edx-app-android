package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.AddFloatingActionButton;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.social.SocialGroup;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.social.SocialProvider;
import org.edx.mobile.social.facebook.FacebookProvider;
import org.edx.mobile.util.SocialUtils;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.view.adapters.GroupSummaryAdapter;
import org.edx.mobile.view.adapters.SimpleAdapter;

import java.util.List;

/**
 * Created by marcashman on 2014-12-15.
 */
public class GroupSummaryFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = GroupSummaryFragment.class.getCanonicalName();
    private static final int REQUEST_ADD_FRIENDS = 0x0000dead;
    public static final String ARG_GROUP = TAG + ".group";

    public static final int RESULT_FRIEND_ADDED = 0X000ADDD;

    private final Logger logger = new Logger(GroupSummaryFragment.class);

    private AddFloatingActionButton addButton;
    private ProgressBar progressBar;
    private TextView memberCountLabel;
    private TextView errorLabel;
    private SwipeRefreshLayout listContainer;

    private SimpleAdapter<SocialMember> adapter;
    private SocialGroup group;
    private int unread;

    private ISegment segIO;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args == null || !args.containsKey(ARG_GROUP)) {
            throw new IllegalArgumentException("missing args");
        }

        group = args.getParcelable(ARG_GROUP);

        if (adapter == null) {
            adapter = new GroupSummaryAdapter(getActivity());
        }

        segIO = SegmentFactory.getInstance();

        try{
            segIO.screenViewsTracking("Group Summary - " + Long.toString(group.getId()));
        }catch(Exception e){
            logger.error(e);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_summary, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.group_summary_list);
        listView.setAdapter(adapter);

        listContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        listContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        memberCountLabel = (TextView) rootView.findViewById(R.id.member_count);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress);
        errorLabel = (TextView) rootView.findViewById(R.id.label_error);
        addButton = (AddFloatingActionButton) rootView.findViewById(R.id.btn_add_to_group);
        addButton.setOnClickListener(this);

        addButton.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

            if(addButton.getViewTreeObserver().isAlive() && addButton.getHeight() > 0 && getView() != null) {
                    addButton.setY(getView().getHeight() + 40);
                    addButton.animate().translationY(0).setStartDelay(200).setDuration(520).setInterpolator(new AnticipateOvershootInterpolator()).start();
                    addButton.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

            }
        });

        rootView.findViewById(R.id.btn_open_group).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refresh();
    }

    private void refresh() {

        errorLabel.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);

        SocialProvider facebookProvider = new FacebookProvider();
        facebookProvider.getGroupMembers(getActivity(), group, new SocialProvider.Callback<List<SocialMember>>() {
            @Override
            public void onSuccess(List<SocialMember> response) {

                if (!isAdded()) {
                    return;
                }
                group.setMembers(response);
                onRefreshed(response);
            }

            @Override
            public void onError(SocialProvider.SocialError err) {
                if (!isAdded()) {
                    return;
                }
                UiUtil.showMessage(getView(), getString(R.string.error_friends_list));
            }
        });

    }

    private void onRefreshed(List<SocialMember> members) {

        addButton.setVisibility(View.VISIBLE);

        progressBar.setVisibility(View.GONE);
        listContainer.setRefreshing(false);

        adapter.setItems(members);
        if (adapter.isEmpty()) {
            memberCountLabel.setVisibility(View.GONE);
            listContainer.setVisibility(View.GONE);
            errorLabel.setVisibility(View.VISIBLE);
        } else {
            memberCountLabel.setVisibility(View.VISIBLE);
            memberCountLabel.setText(getString(R.string.group_summary_count, adapter.getCount()));
            listContainer.setVisibility(View.VISIBLE);
            errorLabel.setVisibility(View.GONE);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        listContainer.setRefreshing(true);
        if (requestCode == REQUEST_ADD_FRIENDS && resultCode == Activity.RESULT_OK){

            errorLabel.setVisibility(View.GONE);
            addButton.setVisibility(View.GONE);

            final List<SocialMember> toInvite = data.getParcelableArrayListExtra(SocialFriendPickerFragment.RESULT_FRIEND_LIST);
            final long groupId = group.getId();

            FacebookProvider fbProvider  = new FacebookProvider();

            fbProvider.inviteFriendsListToGroup(getActivity(),
                groupId,
                toInvite,
                new SocialProvider.Callback<Void>() {
                    @Override
                    public void onSuccess(Void response) {

                        Intent intent = new Intent();
                        intent.putExtra(ARG_GROUP, group);
                        getActivity().setResult(RESULT_FRIEND_ADDED, intent);
                        //
                        try{
                            segIO.groupInvited(groupId, toInvite.size());
                        }catch(Exception e){
                            logger.error(e);
                        }
                        //
                        refresh();
                        Toast.makeText(getActivity(), getString(R.string.friends_invited), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(SocialProvider.SocialError err) {
                        //
                    }
                });

        } else {

            refresh();

        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_to_group:
                Intent intent = new Intent(getActivity(), SocialFriendPickerActivity.class);
                intent.putExtra(SocialFriendPickerActivity.ARG_FILTER_GROUP, group);
                intent.putExtra(SocialFriendPickerActivity.ADD_TO_GROUP, true);
                startActivityForResult(intent, REQUEST_ADD_FRIENDS);

                break;
            case R.id.btn_open_group:

                try{
                    segIO.gameGroupAccessed(group.getId(), group.getMembers().size());
                }catch(Exception e){
                    logger.error(e);
                }

                startActivity(SocialUtils.makeGroupLaunchIntent(getActivity(), Long.toString(group.getId()), SocialUtils.SocialType.FACEBOOK));
                break;
        }
    }

}

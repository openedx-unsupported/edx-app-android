package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.social.SocialProvider;
import org.edx.mobile.social.facebook.FacebookProvider;
import org.edx.mobile.util.JavaUtil;
import org.edx.mobile.view.adapters.FriendListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SocialFriendPickerFragment extends Fragment implements SocialProvider.Callback<List<SocialMember>> {

    private enum State {
        LOADING,
        LOADED,
        ERROR
    }

    private static String TAG = SocialFriendPickerFragment.class.getCanonicalName();
    public static final String EXTRA_OPT_FRIENDS_LIST = TAG + ".input_friends";
    public static final String EXTRA_OPT_IN_GROUP = TAG + ".input_in_group";
    public static final String RESULT_FRIEND_LIST = TAG + ".result";
    private static final String FRIEND_LIST_MODELS = TAG + ".models";
    private static final String FRIEND_LIST_STATES = TAG + ".states";
    private static final String FRIEND_LIST_ALREADY_IN_GROUP = TAG + ".alreadyInGroup";

    private final Logger logger = new Logger(SocialFriendPickerFragment.class);

    private SocialProvider friendProvider;
    private FriendListAdapter friendListAdapter;

    private ListView listView;
    private EditText searchText;
    private ProgressBar progressBar;
    private TextView errorMessage;
    private MenuItem doneMenuItem;
    private ImageButton cancelBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        friendProvider = new FacebookProvider();
        friendListAdapter = new FriendListAdapter(getActivity());
        friendListAdapter.setOnSelectChangeListener(new FriendListAdapter.OnSelectedItemCountChangeListener() {

            @Override
            public void onSelectionItemCountChange(int itemSelectCount) {
                refreshMenu();
            }

        });

        ISegment segIO = SegmentFactory.getInstance();

        try{
            segIO.screenViewsTracking("Social Friend Picker");
        }catch(Exception e){
            logger.error(e);
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        doneMenuItem = menu.findItem(R.id.done_btn);
        doneMenuItem.setVisible(true);

        doneMenuItem.getActionView().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onDone();
            }

        });
        refreshMenu();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            ArrayList<SocialMember> friends = savedInstanceState.getParcelableArrayList(FRIEND_LIST_MODELS);

            friendListAdapter.setItems(friends);

            //Restore the saved selected set
            long[] selected = savedInstanceState.getLongArray(FRIEND_LIST_STATES);
            if (selected != null) {
                friendListAdapter.setSelectedList(JavaUtil.primitiveLongToSet(selected));
            }
            //Restore the saved already in group set
            long[] alreadyInGroup = savedInstanceState.getLongArray(FRIEND_LIST_ALREADY_IN_GROUP);
            if (alreadyInGroup != null) {
                friendListAdapter.setAlreadyInGroupList(JavaUtil.primitiveLongToSet(alreadyInGroup));
            }
            refreshPage(State.LOADED);
        } else if (getArguments() != null && getArguments().containsKey(EXTRA_OPT_FRIENDS_LIST)) {
            List<SocialMember> friends = getArguments().getParcelableArrayList(EXTRA_OPT_FRIENDS_LIST);
            friendListAdapter.setItems(friends);
            refreshPage(State.LOADED);
        } else {
            friendProvider.getMyFriends(getActivity(), this);
            refreshPage(State.LOADING);
        }

        //Retrieve the already in group list
        if (getArguments() != null && getArguments().containsKey(EXTRA_OPT_IN_GROUP)){

            long[] alreadyInGroup = getArguments().getLongArray(EXTRA_OPT_IN_GROUP);
            friendListAdapter.setAlreadyInGroupList(JavaUtil.primitiveLongToSet(alreadyInGroup));

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_social_friend_list, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.api_spinner);
        errorMessage = (TextView) view.findViewById(R.id.social_friend_picker_message);

        listView = (ListView) view.findViewById(R.id.social_friend_picker_list_view);
        listView.setAdapter(friendListAdapter);

        final TextView searchHeader = (TextView) view.findViewById(R.id.search_list_header);

        searchText = (EditText) view.findViewById(R.id.search_friends_edit_text);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                Filter filter = friendListAdapter.getFilter();
                if (TextUtils.isEmpty(s)) {
                    filter.filter("");
                    cancelBtn.setVisibility(View.INVISIBLE);
                    searchHeader.setText(R.string.friends_with_edx_accounts);

                } else {
                    filter.filter(s);
                    cancelBtn.setVisibility(View.VISIBLE);
                    searchHeader.setText(R.string.search_results);
                }

            }
        });

        cancelBtn = (ImageButton) view.findViewById(R.id.cancel_search_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchText.setText("");
            }
        });

        return view;
    }

    private void refreshPage(State state) {
        progressBar.setVisibility(View.GONE);
        errorMessage.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        switch (state) {
            case LOADING:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case LOADED:
                if (friendListAdapter.isEmpty()) {
                    errorMessage.setVisibility(View.VISIBLE);

                    errorMessage.setText(R.string.error_no_friends_connected);
                    searchText.setEnabled(false);
                } else {
                    listView.setVisibility(View.VISIBLE);

                    searchText.setEnabled(true);
                }
                break;
            case ERROR:
                errorMessage.setVisibility(View.VISIBLE);
                break;
        }
        refreshMenu();
    }

    private void onDone() {
        ArrayList<SocialMember> result = (ArrayList<SocialMember>) friendListAdapter.getSelectedFriends();

        Intent returnIntent = new Intent();
        returnIntent.putParcelableArrayListExtra(RESULT_FRIEND_LIST, result);
        getActivity().setResult(Activity.RESULT_OK, returnIntent);
        getActivity().finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        searchText.clearFocus();
    }

    private void refreshMenu(){
        if (doneMenuItem != null) {
            TextView doneText = (TextView) doneMenuItem.getActionView();
            doneText.setEnabled(!friendListAdapter.getSelectedFriends().isEmpty());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        Set<Long> toInvite = friendListAdapter.getSelectedList();
        Long inviteLongArray[] = toInvite.toArray(new Long[toInvite.size()]);

        Set<Long> alreadyGrouped = friendListAdapter.getAlreadyInGroupList();
        Long alreadyGroupedLongArray[] = alreadyGrouped.toArray(new Long[alreadyGrouped.size()]);

        outState.putParcelableArrayList(FRIEND_LIST_MODELS, new ArrayList<Parcelable>(friendListAdapter.getItems()));
        outState.putLongArray(FRIEND_LIST_STATES, JavaUtil.toPrimitive(inviteLongArray));
        outState.putLongArray(FRIEND_LIST_ALREADY_IN_GROUP, JavaUtil.toPrimitive(alreadyGroupedLongArray));

        super.onSaveInstanceState(outState);
    }

    //On friend list fetch success
    @Override
    public void onSuccess(List<SocialMember> response) {

        if (response != null && response.size() > 0){

            friendListAdapter.setItems(response);

        }
        refreshPage(State.LOADED);
    }

    @Override
    public void onError(SocialProvider.SocialError err) {
        errorMessage.setText(getResources().getString(R.string.error_friends_list));
        refreshPage(State.ERROR);
    }

}
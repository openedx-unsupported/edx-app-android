package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.facebook.IUiLifecycleHelper;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.social.SocialProvider;
import org.edx.mobile.social.facebook.FacebookProvider;
import org.edx.mobile.view.dialog.InstallFacebookDialog;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupFragment extends Fragment implements View.OnClickListener {

    public static String TAG = CreateGroupFragment.class.getCanonicalName();
    public static String ALL_FRIENDS = TAG + ".all_friends";

    public static final String RESULT_FRIENDS = TAG + "result.friends";
    public static final String RESULT_DESC = TAG + "result.desc";
    public static final String RESULT_NAME = TAG + "result.name";

    private final Logger logger = new Logger(CreateGroupFragment.class);

    private EditText editTextName;
    private EditText editTextDesc;
    private TextView connectedText;
    private Button addButton;
    private View errorLayout;
    private View progress;
    private View formLayout;

    private SocialMember currentUser;

    private List<SocialMember> friendsConnectedToEdx;

    private IUiLifecycleHelper uiHelper;

    private SocialProvider.Callback<List<SocialMember>> getFriendsCallback = new SocialProvider.Callback<List<SocialMember>>() {
        @Override
        public void onSuccess(List<SocialMember> response) {
            friendsConnectedToEdx = response;
            refreshVisibility();
        }

        @Override
        public void onError(SocialProvider.SocialError err) {
            Toast.makeText(getActivity(), R.string.error_contact_FB, Toast.LENGTH_SHORT).show();
        }
    };

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            //empty on purpose
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            //empty on purpose
        }

        @Override
        public void afterTextChanged(Editable editable) {
            updateButtonValidity();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {

            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {

            }
        });

        if (requestCode == SocialFriendPickerActivity.PICK_FRIENDS_CREATE_GROUP_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {

                //If a result was received from the Friends list we create the group
                ArrayList<SocialMember> friendsToInvite = data.getParcelableArrayListExtra(SocialFriendPickerFragment.RESULT_FRIEND_LIST);

                Intent resultIntent = new Intent();
                resultIntent.putExtra(RESULT_FRIENDS, friendsToInvite);
                resultIntent.putExtra(RESULT_NAME, editTextName.getText().toString());
                resultIntent.putExtra(RESULT_DESC, editTextDesc.getText().toString());

                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (friendsConnectedToEdx != null) {
            outState.putParcelableArrayList(ALL_FRIENDS, new ArrayList<Parcelable>(friendsConnectedToEdx));
        }
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (friendsConnectedToEdx == null) {
            new FacebookProvider().getMyFriends(getActivity(), getFriendsCallback);
        }

        ISegment segIO = SegmentFactory.getInstance();

        try{
            segIO.screenViewsTracking("Create Games Group");
        }catch(Exception e){
            logger.error(e);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_group, container, false);

        editTextName = (EditText) rootView.findViewById(R.id.text_group_name);
        editTextName.addTextChangedListener(textWatcher);

        editTextDesc = (EditText) rootView.findViewById(R.id.text_group_desc);
        editTextDesc.addTextChangedListener(textWatcher);

        addButton = (Button) rootView.findViewById(R.id.next_create_group);
        addButton.setOnClickListener(this);

        View shareAppButton = rootView.findViewById(R.id.btn_share_app);
        shareAppButton.setOnClickListener(this);

        errorLayout = rootView.findViewById(R.id.error_layout);
        progress = rootView.findViewById(R.id.progress);
        formLayout = rootView.findViewById(R.id.create_group_form);

        connectedText = (TextView) rootView.findViewById(R.id.group_create_facebook_text);
        connectedText.setOnClickListener(this);
        SocialProvider socialProvider = new FacebookProvider();
        socialProvider.getUser(getActivity(), new SocialProvider.Callback<SocialMember>() {
            @Override
            public void onSuccess(SocialMember response) {

                currentUser = response;
                insertUserName(response.getFullName());
                connectedText.setVisibility(View.VISIBLE);

            }

            @Override
            public void onError(SocialProvider.SocialError err) {
                connectedText.setVisibility(View.GONE);
            }
        });

        refreshVisibility();

        uiHelper = IUiLifecycleHelper.Factory.getInstance(getActivity(), null);
        uiHelper.onCreate(savedInstanceState);

        return rootView;
    }

    private void refreshVisibility() {
        if (getView() == null) {
            //view not inflated
            return;
        }

        errorLayout.setVisibility(View.GONE);
        formLayout.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);

        if (friendsConnectedToEdx != null) {
            if (friendsConnectedToEdx.size() == 0) {
                errorLayout.setVisibility(View.VISIBLE);
            } else {
                formLayout.findViewById(R.id.create_group_form).setVisibility(View.VISIBLE);
                updateButtonValidity();
            }
        } else {
            progress.setVisibility(View.VISIBLE);
        }
    }

    private void updateButtonValidity() {
        addButton.setEnabled(!TextUtils.isEmpty(editTextName.getText()));
    }

    private void insertUserName(String name){

        String display = getString(R.string.connected_as, name);
        connectedText.setText(display, TextView.BufferType.SPANNABLE);

        int start = display.indexOf(name);
        int end = start + name.length();

        Spannable s = (Spannable) connectedText.getText();
        int colour = getActivity().getResources().getColor(R.color.facebook_blue);
        s.setSpan(new ForegroundColorSpan(colour), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.next_create_group:
                Intent friendsIntent = new Intent(getActivity(), SocialFriendPickerActivity.class);
                friendsIntent.putExtra(SocialFriendPickerActivity.EXTRA_OPT_LIST_OF_FRIENDS, new ArrayList<SocialMember>(friendsConnectedToEdx));
                startActivityForResult(friendsIntent, SocialFriendPickerActivity.PICK_FRIENDS_CREATE_GROUP_REQUEST);
                break;
            case R.id.group_create_facebook_text:
                if (currentUser != null) {
                    SocialProvider fbProvider = new FacebookProvider();
                    fbProvider.launchUserProfile(getActivity(), "me");
                }
                break;
            case R.id.btn_share_app:
                FacebookProvider fbProvider = new FacebookProvider();
                FacebookDialog dialog = (FacebookDialog) fbProvider.shareApplication(getActivity());
                if (dialog != null) {
                    uiHelper.trackPendingDialogCall(dialog.present());
                } else {
                    new InstallFacebookDialog().show(getFragmentManager(), null);
                }

                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

}
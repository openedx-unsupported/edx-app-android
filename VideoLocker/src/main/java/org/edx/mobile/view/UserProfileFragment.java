package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.third_party.iconify.IconDrawable;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.user.Account;
import org.edx.mobile.user.GetAccountTask;
import org.edx.mobile.user.LanguageProficiency;
import org.edx.mobile.util.ResourceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;

public class UserProfileFragment extends RoboFragment {

    @InjectExtra(UserProfileActivity.EXTRA_USERNAME)
    private String username;

    @Nullable
    private GetAccountTask getAccountTask;

    @Nullable
    private Account account;

    @Nullable
    private ViewHolder viewHolder;

    @Nullable
    MenuItem editProfileMenuItem;

    @Inject
    private Router router;

    private boolean isViewingOwnProfile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        final ProfileModel model = new PrefManager(getActivity(), PrefManager.Pref.LOGIN).getCurrentUserProfile();
        isViewingOwnProfile = null != model && model.username.equalsIgnoreCase(username);

        getAccountTask = new GetAccountTask(getActivity(), username) {
            @Override
            protected void onSuccess(Account account) throws Exception {
                UserProfileFragment.this.account = account;
                if (null != viewHolder) {
                    viewHolder.setAccount(account);
                }
            }
        };
        getAccountTask.setTaskProcessCallback(null); // Disable default loading indicator, we have our own
        getAccountTask.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (isViewingOwnProfile) {
            inflater.inflate(R.menu.edit_profile, menu);
            editProfileMenuItem = menu.findItem(R.id.edit_profile).setIcon(
                    new IconDrawable(getActivity(), Iconify.IconValue.fa_pencil)
                            .actionBarSize().colorRes(R.color.edx_white));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.edit_profile) {
            router.showUserProfileEditor(getActivity(), username);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        editProfileMenuItem = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewHolder = new ViewHolder(view);
        viewHolder.setUsername(username);
        viewHolder.setAccount(account);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != getAccountTask) {
            getAccountTask.cancel(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewHolder = null;
    }

    public class ViewHolder {
        public final ImageView profileImage;
        public final TextView usernameText;
        public final View profileHeaderContent;
        public final View profileBodyContent;
        public final View loadingIndicator;
        public final View parentalConsentRequired;
        public final View limitedView;
        public final TextView bioText;
        public final View languageContainer;
        public final View locationContainer;
        public final TextView languageText;
        public final TextView locationText;
        public final View incompleteContainer;
        public final TextView incompleteGreeting;
        public final Button editProfileButton;

        public ViewHolder(View parent) {
            this.profileImage = (ImageView) parent.findViewById(R.id.profile_image);
            this.usernameText = (TextView) parent.findViewById(R.id.username_text);
            this.profileHeaderContent = parent.findViewById(R.id.profile_header_content);
            this.profileBodyContent = parent.findViewById(R.id.profile_body_content);
            this.loadingIndicator = parent.findViewById(R.id.loading_indicator);
            this.parentalConsentRequired = parent.findViewById(R.id.parental_consent_required);
            this.limitedView = parent.findViewById(R.id.sharing_limited);
            this.bioText = (TextView) parent.findViewById(R.id.bio_text);
            this.languageContainer = parent.findViewById(R.id.language_container);
            this.languageText = (TextView) parent.findViewById(R.id.language_text);
            this.locationContainer = parent.findViewById(R.id.location_container);
            this.locationText = (TextView) parent.findViewById(R.id.location_text);
            this.incompleteContainer = parent.findViewById(R.id.incomplete_container);
            this.incompleteGreeting = (TextView) parent.findViewById(R.id.incomplete_greeting);
            this.editProfileButton = (Button) parent.findViewById(R.id.edit_profile_button);
            editProfileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    router.showUserProfileEditor(getActivity(), username);
                }
            });
        }

        public void setUsername(String username) {
            usernameText.setText(username);
        }

        public void setAccount(@Nullable final Account account) {
            if (null == account) {
                profileHeaderContent.setVisibility(View.GONE);
                profileBodyContent.setVisibility(View.GONE);
                loadingIndicator.setVisibility(View.VISIBLE);

            } else {
                profileHeaderContent.setVisibility(View.VISIBLE);
                profileBodyContent.setVisibility(View.VISIBLE);
                loadingIndicator.setVisibility(View.GONE);

                final RequestManager requestManager = Glide.with(profileImage.getContext());
                requestManager
                        .load(account.getProfileImage().getImageUrlFull())
                        .thumbnail(requestManager.load(account.getProfileImage().getImageUrlSmall()))
                        .into(profileImage);

                if (account.requiresParentalConsent() || account.getAccountPrivacy() == Account.Privacy.PRIVATE) {
                    limitedView.setVisibility(View.VISIBLE);
                    languageContainer.setVisibility(View.GONE);
                    locationContainer.setVisibility(View.GONE);
                } else {
                    limitedView.setVisibility(View.GONE);
                    if (account.getLanguageProficiencies().isEmpty()) {
                        languageContainer.setVisibility(View.GONE);
                    } else {
                        languageContainer.setVisibility(View.VISIBLE);
                        final List<String> languageNames = new ArrayList<>(account.getLanguageProficiencies().size());
                        for (LanguageProficiency proficiency : account.getLanguageProficiencies()) {
                            languageNames.add(new Locale.Builder().setLanguage(proficiency.getCode()).build().getDisplayName());
                        }
                        languageText.setText(TextUtils.join(", ", languageNames));
                    }

                    if (TextUtils.isEmpty(account.getCountry())) {
                        locationContainer.setVisibility(View.GONE);
                    } else {
                        locationContainer.setVisibility(View.VISIBLE);
                        locationText.setText(new Locale.Builder().setRegion(account.getCountry()).build().getDisplayCountry());
                    }
                }

                incompleteContainer.setVisibility(View.GONE);
                parentalConsentRequired.setVisibility(View.GONE);
                bioText.setVisibility(View.GONE);
                editProfileButton.setVisibility(View.GONE);
                if (isViewingOwnProfile && account.requiresParentalConsent()) {
                    parentalConsentRequired.setVisibility(View.VISIBLE);
                    editProfileButton.setVisibility(View.VISIBLE);
                    editProfileButton.setText(getString(R.string.profile_consent_needed_edit_button));

                } else if (isViewingOwnProfile && TextUtils.isEmpty(account.getBio())) {
                    incompleteContainer.setVisibility(View.VISIBLE);
                    incompleteGreeting.setText(ResourceUtil.getFormattedString(getResources(), R.string.profile_incomplete_greeting, "username", account.getUsername()));
                    editProfileButton.setVisibility(View.VISIBLE);
                    editProfileButton.setText(getString(R.string.profile_incomplete_edit_button));

                } else {
                    bioText.setVisibility(View.VISIBLE);
                    bioText.setText(account.getBio());
                }
            }
        }
    }
}
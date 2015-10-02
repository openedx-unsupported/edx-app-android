package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ProgressBar;
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
import org.edx.mobile.util.ResourceUtil;

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
    private UserProfileViewHolder viewHolder;

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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (isViewingOwnProfile) {
            inflater.inflate(R.menu.edit_profile, menu);
            menu.findItem(R.id.edit_profile).setIcon(
                    new IconDrawable(getActivity(), Iconify.IconValue.fa_pencil)
                            .actionBarSize().colorRes(R.color.edx_white));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_profile: {
                router.showUserProfileEditor(getActivity(), username);
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewHolder = new UserProfileViewHolder(view);
        viewHolder.editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                router.showUserProfileEditor(getActivity(), username);
            }
        });
        viewHolder.usernameText.setText(username);
        setAccount(account);

        if (null == getAccountTask) {
            getAccountTask = new GetAccountTask(getActivity(), username) {
                @Override
                protected void onSuccess(Account account) throws Exception {
                    setAccount(account);
                }

                @Override
                protected void onException(Exception e) throws RuntimeException {
                    logger.error(e);
                    showErrorMessage(e);
                }
            };
            getAccountTask.setProgressDialog(viewHolder.loadingIndicator); // So that our indicator is hidden after task completes
            getAccountTask.execute();
        }
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


    private void setAccount(@Nullable final Account account) {
        this.account = account;
        if (null == viewHolder) {
            return;
        }
        if (null == account) {
            viewHolder.profileHeaderContent.setVisibility(View.GONE);
            viewHolder.profileBodyContent.setVisibility(View.GONE);
            viewHolder.loadingIndicator.setVisibility(View.VISIBLE);

        } else {
            viewHolder.profileHeaderContent.setVisibility(View.VISIBLE);
            viewHolder.profileBodyContent.setVisibility(View.VISIBLE);
            viewHolder.loadingIndicator.setVisibility(View.GONE);

            final RequestManager requestManager = Glide.with(viewHolder.profileImage.getContext());
            requestManager
                    .load(account.getProfileImage().getImageUrlFull())
                    .thumbnail(requestManager.load(account.getProfileImage().getImageUrlSmall()))
                    .into(viewHolder.profileImage);

            if (account.requiresParentalConsent() || account.getAccountPrivacy() == Account.Privacy.PRIVATE) {
                viewHolder.limitedView.setVisibility(View.VISIBLE);
                viewHolder.limitedView.setText(isViewingOwnProfile
                        ? R.string.profile_sharing_limited_by_you
                        : R.string.profile_sharing_limited_by_other_user);
                viewHolder.languageContainer.setVisibility(View.GONE);
                viewHolder.locationContainer.setVisibility(View.GONE);
            } else {
                viewHolder.limitedView.setVisibility(View.GONE);
                if (account.getLanguageProficiencies().isEmpty()) {
                    viewHolder.languageContainer.setVisibility(View.GONE);
                } else {
                    viewHolder.languageContainer.setVisibility(View.VISIBLE);
                    viewHolder.languageText.setText(
                            new Locale.Builder()
                                    .setLanguage(account.getLanguageProficiencies().get(0).getCode())
                                    .build()
                                    .getDisplayName());
                }

                if (TextUtils.isEmpty(account.getCountry())) {
                    viewHolder.locationContainer.setVisibility(View.GONE);
                } else {
                    viewHolder.locationContainer.setVisibility(View.VISIBLE);
                    viewHolder.locationText.setText(
                            new Locale.Builder()
                                    .setRegion(account.getCountry())
                                    .build()
                                    .getDisplayCountry());
                }
            }

            viewHolder.incompleteContainer.setVisibility(View.GONE);
            viewHolder.parentalConsentRequired.setVisibility(View.GONE);
            viewHolder.bioText.setVisibility(View.GONE);
            viewHolder.editProfileButton.setVisibility(View.GONE);
            if (isViewingOwnProfile && account.requiresParentalConsent()) {
                viewHolder.parentalConsentRequired.setVisibility(View.VISIBLE);
                viewHolder.editProfileButton.setVisibility(View.VISIBLE);
                viewHolder.editProfileButton.setText(viewHolder.editProfileButton.getResources().getString(R.string.profile_consent_needed_edit_button));

            } else if (isViewingOwnProfile && TextUtils.isEmpty(account.getBio())) {
                viewHolder.incompleteContainer.setVisibility(View.VISIBLE);
                viewHolder.incompleteGreeting.setText(ResourceUtil.getFormattedString(
                        viewHolder.incompleteGreeting.getResources(), R.string.profile_incomplete_greeting, "username", account.getUsername()));
                viewHolder.editProfileButton.setVisibility(View.VISIBLE);
                viewHolder.editProfileButton.setText(viewHolder.editProfileButton.getResources().getString(R.string.profile_incomplete_edit_button));

            } else {
                viewHolder.bioText.setVisibility(View.VISIBLE);
                viewHolder.bioText.setText(account.getBio());
            }
        }
    }

    public static class UserProfileViewHolder {
        public final ImageView profileImage;
        public final TextView usernameText;
        public final View profileHeaderContent;
        public final View profileBodyContent;
        public final ProgressBar loadingIndicator;
        public final View parentalConsentRequired;
        public final TextView limitedView;
        public final TextView bioText;
        public final View languageContainer;
        public final View locationContainer;
        public final TextView languageText;
        public final TextView locationText;
        public final View incompleteContainer;
        public final TextView incompleteGreeting;
        public final Button editProfileButton;

        public UserProfileViewHolder(@NonNull View parent) {
            this.profileImage = (ImageView) parent.findViewById(R.id.profile_image);
            this.usernameText = (TextView) parent.findViewById(R.id.username_text);
            this.profileHeaderContent = parent.findViewById(R.id.profile_header_content);
            this.profileBodyContent = parent.findViewById(R.id.profile_body_content);
            this.loadingIndicator = (ProgressBar) parent.findViewById(R.id.loading_indicator);
            this.parentalConsentRequired = parent.findViewById(R.id.parental_consent_required);
            this.limitedView = (TextView) parent.findViewById(R.id.sharing_limited);
            this.bioText = (TextView) parent.findViewById(R.id.bio_text);
            this.languageContainer = parent.findViewById(R.id.language_container);
            this.languageText = (TextView) parent.findViewById(R.id.language_text);
            this.locationContainer = parent.findViewById(R.id.location_container);
            this.locationText = (TextView) parent.findViewById(R.id.location_text);
            this.incompleteContainer = parent.findViewById(R.id.incomplete_container);
            this.incompleteGreeting = (TextView) parent.findViewById(R.id.incomplete_greeting);
            this.editProfileButton = (Button) parent.findViewById(R.id.edit_profile_button);
        }
    }
}
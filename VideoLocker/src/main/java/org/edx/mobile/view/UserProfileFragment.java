package org.edx.mobile.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorRes;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.event.AccountUpdatedEvent;
import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.user.Account;
import org.edx.mobile.user.GetAccountTask;
import org.edx.mobile.util.InvalidLocaleException;
import org.edx.mobile.util.LocaleUtils;

import de.greenrobot.event.EventBus;
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

    @Inject
    private ISegment segment;

    private boolean isViewingOwnProfile;

    protected final Logger logger = new Logger(getClass().getName());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        EventBus.getDefault().register(this);

        final ProfileModel model = new PrefManager(getActivity(), PrefManager.Pref.LOGIN).getCurrentUserProfile();
        isViewingOwnProfile = null != model && model.username.equalsIgnoreCase(username);

        if (null == savedInstanceState) {
            segment.trackProfileViewed(username);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (isViewingOwnProfile) {
            inflater.inflate(R.menu.edit_profile, menu);
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
        {
            final View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    router.showUserProfileEditor(getActivity(), username);
                }
            };
            viewHolder.parentalConsentRequired.setOnClickListener(listener);
            viewHolder.incompleteEditProfileButton.setOnClickListener(listener);
        }
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
                    viewHolder.loadingIndicator.setVisibility(View.GONE);
                    logger.error(e);
                    showErrorMessage(e);
                }
            };
            getAccountTask.setProgressCallback(null); // Disable built-in loading indicator
            getAccountTask.execute();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != getAccountTask) {
            getAccountTask.cancel(true);
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewHolder = null;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull AccountUpdatedEvent event) {
        if (event.getAccount().getUsername().equalsIgnoreCase(username)) {
            setAccount(event.getAccount());
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull ProfilePhotoUpdatedEvent event) {
        if (event.getUsername().equalsIgnoreCase(username)) {
            if (null == event.getUri()) {
                Glide.with(UserProfileFragment.this)
                        .load(R.drawable.xsie)
                        .into(viewHolder.profileImage);
            } else {
                Glide.with(UserProfileFragment.this)
                        .load(event.getUri())
                        .skipMemoryCache(true) // URI is re-used in subsequent events; disable caching
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(viewHolder.profileImage);
            }
        }
    }

    private void setAccount(@Nullable final Account account) {
        this.account = account;
        if (null == viewHolder) {
            return;
        }

        // These views are all mutually exclusive; hide them all here, then show only the correct one
        viewHolder.incompleteContainer.setVisibility(View.GONE);
        viewHolder.parentalConsentRequired.setVisibility(View.GONE);
        viewHolder.bioText.setVisibility(View.GONE);
        viewHolder.noAboutMe.setVisibility(View.GONE);
        viewHolder.loadingIndicator.setVisibility(View.GONE);

        if (null == account) {
            viewHolder.profileHeaderContent.setVisibility(View.GONE);
            viewHolder.loadingIndicator.setVisibility(View.VISIBLE);

        } else {
            viewHolder.profileHeaderContent.setVisibility(View.VISIBLE);

            if (account.getProfileImage().hasImage()) {
                Glide.with(UserProfileFragment.this)
                        .load(account.getProfileImage().getImageUrlFull())
                        .into(viewHolder.profileImage);
            } else {
                Glide.with(UserProfileFragment.this)
                        .load(R.drawable.xsie)
                        .into(viewHolder.profileImage);
            }

            if (account.requiresParentalConsent() || account.getAccountPrivacy() == Account.Privacy.PRIVATE) {
                viewHolder.limitedView.setVisibility(View.VISIBLE);
                viewHolder.limitedView.setText(isViewingOwnProfile
                        ? R.string.profile_sharing_limited_by_you
                        : R.string.profile_sharing_limited_by_other_user);
                viewHolder.languageContainer.setVisibility(View.GONE);
                viewHolder.locationContainer.setVisibility(View.GONE);
            } else {
                viewHolder.limitedView.setVisibility(View.GONE);
                viewHolder.languageContainer.setVisibility(View.GONE);
                if (!account.getLanguageProficiencies().isEmpty()) {
                    try {
                        viewHolder.languageText.setText(
                                LocaleUtils.getLanguageNameFromCode(account.getLanguageProficiencies().get(0).getCode()));
                        viewHolder.languageContainer.setVisibility(View.VISIBLE);
                    } catch (InvalidLocaleException e) {
                        logger.error(e, true);
                    }
                }

                viewHolder.locationContainer.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(account.getCountry())) {
                    try {
                        viewHolder.locationText.setText(LocaleUtils.getCountryNameFromCode(account.getCountry()));
                        viewHolder.locationContainer.setVisibility(View.VISIBLE);
                    } catch (InvalidLocaleException e) {
                        logger.error(e, true);
                    }
                }
            }

            @ColorRes int bodyBackgroundColor = R.color.edx_grayscale_neutral_xx_light;
            if (isViewingOwnProfile && account.requiresParentalConsent()) {
                viewHolder.parentalConsentRequired.setVisibility(View.VISIBLE);

            } else if (isViewingOwnProfile && TextUtils.isEmpty(account.getBio()) && account.getAccountPrivacy() != Account.Privacy.ALL_USERS) {
                viewHolder.incompleteContainer.setVisibility(View.VISIBLE);

            } else if (account.getAccountPrivacy() != Account.Privacy.PRIVATE) {
                if (TextUtils.isEmpty(account.getBio())) {
                    viewHolder.noAboutMe.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.bioText.setVisibility(View.VISIBLE);
                    viewHolder.bioText.setText(account.getBio());
                    bodyBackgroundColor = R.color.white;
                }
            }
            viewHolder.profileBodyContent.setBackgroundColor(getResources().getColor(bodyBackgroundColor));
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
        public final Button consentEditProfileButton;
        public final Button incompleteEditProfileButton;
        public final View noAboutMe;

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
            this.consentEditProfileButton = (Button) parent.findViewById(R.id.parental_consent_edit_profile_button);
            this.incompleteEditProfileButton = (Button) parent.findViewById(R.id.incomplete_edit_profile_button);
            this.noAboutMe = parent.findViewById(R.id.no_about_me);
        }
    }
}

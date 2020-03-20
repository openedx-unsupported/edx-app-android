package org.edx.mobile.profiles;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentUserProfileBioBinding;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.PresenterFragment;
import org.edx.mobile.view.Router;

import javax.inject.Inject;

public class UserProfileBioFragment extends PresenterFragment<UserProfileBioPresenter, UserProfileBioPresenter.ViewInterface> implements ScrollingPreferenceChild {

    private final Logger logger = new Logger(getClass().getName());

    @Inject
    Router router;

    private boolean prefersScrollingHeader = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return DataBindingUtil.inflate(inflater, R.layout.fragment_user_profile_bio, container, false).getRoot();
    }

    public static UserProfileBioFragment newInstance() {
        UserProfileBioFragment fragment = new UserProfileBioFragment();
        return fragment;
    }

    @NonNull
    @Override
    protected UserProfileBioPresenter createPresenter() {
        Fragment parent = getParentFragment();
        UserProfileBioTabParent owner = (UserProfileBioTabParent)parent;

        return new UserProfileBioPresenter(owner.getBioInteractor());
    }

    @NonNull
    @Override
    protected UserProfileBioPresenter.ViewInterface createView() {
        final FragmentUserProfileBioBinding viewHolder = DataBindingUtil.getBinding(getView());

        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onEditProfile();
            }
        };
        viewHolder.parentalConsentEditProfileButton.setOnClickListener(listener);
        viewHolder.incompleteEditProfileButton.setOnClickListener(listener);

        return new UserProfileBioPresenter.ViewInterface() {

            @Override
            public void showBio(UserProfileBioModel bio) {
                viewHolder.profileBioContent.setVisibility(View.VISIBLE);
                viewHolder.parentalConsentRequired.setVisibility(bio.contentType == UserProfileBioModel.ContentType.PARENTAL_CONSENT_REQUIRED ? View.VISIBLE : View.GONE);
                viewHolder.incompleteContainer.setVisibility(bio.contentType == UserProfileBioModel.ContentType.INCOMPLETE ? View.VISIBLE : View.GONE);
                viewHolder.noAboutMe.setVisibility(bio.contentType == UserProfileBioModel.ContentType.NO_ABOUT_ME ? View.VISIBLE : View.GONE);
                viewHolder.bioText.setVisibility(bio.contentType == UserProfileBioModel.ContentType.ABOUT_ME ? View.VISIBLE : View.GONE);
                viewHolder.bioText.setText(bio.bioText);
                viewHolder.bioText.setContentDescription(ResourceUtil.getFormattedString(getResources(), R.string.profile_about_me_description, "about_me", bio.bioText));
                prefersScrollingHeader = bio.contentType == UserProfileBioModel.ContentType.ABOUT_ME;
                ((ScrollingPreferenceParent)getParentFragment()).onChildScrollingPreferenceChanged();
            }

            @Override
            public void navigateToProfileEditor(String username) {
                router.showUserProfileEditor(getActivity(), username);
            }
        };
    }

    @Override
    public boolean prefersScrollingHeader() {
        return prefersScrollingHeader;
    }
}

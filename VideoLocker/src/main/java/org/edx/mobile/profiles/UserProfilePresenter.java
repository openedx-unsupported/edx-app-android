package org.edx.mobile.profiles;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.util.observer.Func1;
import org.edx.mobile.util.observer.Observables;
import org.edx.mobile.util.observer.Observer;
import org.edx.mobile.view.ViewHoldingPresenter;

import java.util.Collections;
import java.util.List;

public class UserProfilePresenter extends ViewHoldingPresenter<UserProfilePresenter.ViewInterface> {

    private static final String BIO_TAB = "PROFILE_TAB_BIO";

    @NonNull
    private final UserProfileInteractor userProfileInteractor;

    public UserProfilePresenter(@NonNull ISegment segment, @NonNull UserProfileInteractor userProfileInteractor) {
        this.userProfileInteractor = userProfileInteractor;
        segment.trackProfileViewed(userProfileInteractor.getUsername());
    }

    public UserProfileBioInteractor getBioInteractor() {
        return new UserProfileBioInteractor(userProfileInteractor.getUsername(), Observables.map(userProfileInteractor.observeProfile(), new Func1<UserProfileViewModel, UserProfileBioModel>() {
            @Override
            public UserProfileBioModel call(UserProfileViewModel arg) {
                return arg.bio;
            }
        }));
    }

    private List<UserProfileTab> builtInTabs() {
        return Collections.singletonList(new UserProfileTab(BIO_TAB, "Bio", UserProfileBioFragment.class));
    }

    @Override
    public void attachView(@NonNull final ViewInterface view) {
        super.attachView(view);
        view.setName(userProfileInteractor.getUsername());
        view.setEditProfileMenuButtonVisible(userProfileInteractor.isViewingOwnProfile());
        view.showLoading();
        observeOnView(userProfileInteractor.observeProfile()).subscribe(new Observer<UserProfileViewModel>() {
            @Override
            public void onData(@NonNull UserProfileViewModel account) {
                view.showProfile(account);
                view.showTabs(builtInTabs());
            }

            @Override
            public void onError(@NonNull Throwable error) {
                view.showError(error);
            }
        });
        observeOnView(userProfileInteractor.observeProfileImage()).subscribe(new Observer<UserProfileImageViewModel>() {
            @Override
            public void onData(@NonNull UserProfileImageViewModel data) {
                view.setPhotoImage(data);
            }

            @Override
            public void onError(@NonNull Throwable error) {
                // Do nothing; leave whatever image/placeholder is already displayed
            }
        });
    }

    @Override
    public void destroy() {
        super.destroy();
        userProfileInteractor.destroy();
    }

    public void onEditProfile() {
        assert getView() != null;
        getView().navigateToProfileEditor(userProfileInteractor.getUsername());
    }

    public interface ViewInterface {
        void setEditProfileMenuButtonVisible(boolean visible);

        void showProfile(@NonNull UserProfileViewModel profile);

        void showLoading();

        void showTabs(@NonNull List<UserProfileTab> tab);

        void showError(@NonNull Throwable error);

        void setPhotoImage(@NonNull UserProfileImageViewModel model);

        void setName(@NonNull String name);

        void navigateToProfileEditor(@NonNull String username);
    }

}

package org.edx.mobile.profiles;

import androidx.annotation.NonNull;

import org.edx.mobile.util.observer.CachingObservable;
import org.edx.mobile.util.observer.Func1;
import org.edx.mobile.util.observer.Observable;
import org.edx.mobile.util.observer.Observables;
import org.edx.mobile.util.observer.Observer;
import org.edx.mobile.view.ViewHoldingPresenter;

class UserProfileBioInteractor {

    @NonNull
    private final Observable<UserProfileBioModel> bio;

    @NonNull
    private final String username;

    UserProfileBioInteractor(String username, Observable<UserProfileBioModel> bio) {
        this.username = username;
        this.bio = bio;
    }

    public Observable<UserProfileBioModel> observeBio() {
        return bio;
    }

    @NonNull
    public String getUsername() {
        return username;
    }
}

public class UserProfileBioPresenter extends ViewHoldingPresenter<UserProfileBioPresenter.ViewInterface> {

    private final UserProfileBioInteractor interactor;

    public UserProfileBioPresenter(UserProfileBioInteractor interactor) {
        this.interactor = interactor;
    }


    public void onEditProfile() {
        assert getView() != null;
        getView().navigateToProfileEditor(interactor.getUsername());
    }

    @Override
    public void attachView(@NonNull final ViewInterface view) {
        super.attachView(view);
        observeOnView(this.interactor.observeBio()).subscribe(new Observer<UserProfileBioModel>() {
            @Override
            public void onData(@NonNull UserProfileBioModel bio) {
                view.showBio(bio);
            }

            @Override
            public void onError(@NonNull Throwable error) {
                // do nothing. Handled at a higher level
            }
        });
    }

    public interface ViewInterface {
        void showBio(UserProfileBioModel bio);
        void navigateToProfileEditor(String username);
    }

}

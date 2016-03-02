package org.edx.mobile.profiles;

import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.profiles.UserProfileImageViewModel;
import org.edx.mobile.profiles.UserProfileInteractor;
import org.edx.mobile.profiles.UserProfilePresenter;
import org.edx.mobile.profiles.UserProfileViewModel;
import org.edx.mobile.test.PresenterTest;
import org.edx.mobile.util.observer.CachingObservable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class UserProfilePresenterTest extends PresenterTest<UserProfilePresenter, UserProfilePresenter.ViewInterface> {

    static final String PROFILE_USERNAME = "john_doe";

    @Mock
    private UserProfileInteractor userProfileInteractor;
    @Mock
    private UserPrefs userPrefs;
    @Mock
    private ISegment segment;

    private CachingObservable<UserProfileViewModel> accountObservable;

    private CachingObservable<UserProfileImageViewModel> photoObservable;

    @Before
    public void before() {
        accountObservable = new CachingObservable<>();
        photoObservable = new CachingObservable<>();
        when(userProfileInteractor.getUsername()).thenReturn(PROFILE_USERNAME);
        when(userProfileInteractor.observeProfile()).thenReturn(accountObservable);
        when(userProfileInteractor.observeProfileImage()).thenReturn(photoObservable);
        startPresenter(new UserProfilePresenter(
                segment,
                userProfileInteractor
        ));
    }

    @Test
    public void whenPresenterIsCreated_tracksProfileView() {
        verify(segment).trackProfileViewed(PROFILE_USERNAME);
    }

    @Test
    public void whenInteractorEmitsProfileData_setsProfileOnView() {
        final UserProfileViewModel model = mock(UserProfileViewModel.class);
        accountObservable.onData(model);
        verify(view).showProfile(model);
    }

    @Test
    public void whenInteractorEmitsProfileError_showsErrorOnView() {
        final RuntimeException error = new RuntimeException();
        accountObservable.onError(error);
        verify(view).showError(error);
    }
    @Test
    public void whenInteractorEmitsProfileImage_setsProfileImageOnView() {
        final UserProfileImageViewModel model = mock(UserProfileImageViewModel.class);
        photoObservable.onData(model);
        verify(view).setPhotoImage(model);
    }

    @Test
    public void whenInteractorEmitsProfileImageError_noInteractionsWithView() {
        photoObservable.onError(new RuntimeException());
        verify(view, never()).showError(any(Throwable.class));
    }

    @Test
    public void onEditProfile_navigatesToProfileEditor() {
        presenter.onEditProfile();
        verify(view).navigateToProfileEditor(PROFILE_USERNAME);
    }
}

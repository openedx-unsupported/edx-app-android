package org.edx.mobile.profiles;

import androidx.annotation.NonNull;

import org.edx.mobile.http.callback.Callback;
import org.edx.mobile.model.Page;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.user.UserService;
import org.edx.mobile.view.ViewHoldingPresenter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;

import java.util.ArrayList;
import java.util.List;

public class UserProfileAccomplishmentsPresenter extends ViewHoldingPresenter<UserProfileAccomplishmentsPresenter.ViewInterface> {

    @NonNull
    private final UserService userService;

    @NonNull
    private final String username;

    private final boolean viewingOwnProfile;

    private InfiniteScrollUtils.PageLoadController pageLoadController;

    private int page = 1;

    @NonNull
    private List<BadgeAssertion> badges = new ArrayList<>();

    private boolean pageLoading = false;

    public UserProfileAccomplishmentsPresenter(@NonNull UserService userService, @NonNull UserPrefs userPrefs, @NonNull String username) {
        this.userService = userService;
        this.username = username;
        final ProfileModel model = userPrefs.getProfile();
        viewingOwnProfile = null != model && model.username.equalsIgnoreCase(username);
    }

    @Override
    public void attachView(@NonNull final ViewInterface view) {
        super.attachView(view);
        pageLoadController = new InfiniteScrollUtils.PageLoadController<>(new InfiniteScrollUtils.ListContentController<BadgeAssertion>() {
            @Override
            public void clear() {
                badges.clear();
                setViewModel();
            }

            @Override
            public void addAll(List<BadgeAssertion> items) {
                badges.addAll(items);
                setViewModel();
            }

            @Override
            public void setProgressVisible(boolean visible) {
                pageLoading = visible;
                setViewModel();
            }
        }, new InfiniteScrollUtils.PageLoader<BadgeAssertion>() {
            @Override
            public void loadNextPage(@NonNull final InfiniteScrollUtils.PageLoadCallback<BadgeAssertion> callback) {
                userService.getBadges(username, page).enqueue(new Callback<Page<BadgeAssertion>>() {
                    @Override
                    protected void onResponse(@NonNull final Page<BadgeAssertion> badges) {
                        ++page;
                        callback.onPageLoaded(badges);
                    }

                    @Override
                    protected void onFailure(@NonNull Throwable error) {
                        // do nothing. Better to just deal show what we can
                    }
                });
            }
        });
        pageLoadController.loadMore();
    }

    private void setViewModel() {
        assert getView() != null;
        getView().setModel(new ViewModel(badges, pageLoading, viewingOwnProfile));
    }

    public void onScrolledToEnd() {
        if (null == pageLoadController) {
            return;
        }
        pageLoadController.loadMore();
    }

    public void onClickShare(@NonNull BadgeAssertion badgeAssertion) {
        assert getView() != null;
        getView().startBadgeShareIntent(badgeAssertion.getAssertionUrl());
    }

    public interface ViewInterface {
        void setModel(@NonNull ViewModel model);

        void startBadgeShareIntent(@NonNull String sharedContent);
    }

    public static class ViewModel {
        @NonNull
        public final List<BadgeAssertion> badges;
        public final boolean pageLoading;
        public final boolean enableSharing;

        public ViewModel(@NonNull List<BadgeAssertion> badges, boolean pageLoading, boolean enableSharing) {
            this.badges = badges;
            this.pageLoading = pageLoading;
            this.enableSharing = enableSharing;
        }
    }
}

package org.edx.mobile.profiles;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import org.edx.mobile.model.Page;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.user.UserAPI;
import org.edx.mobile.util.observer.AsyncCallableUtils;
import org.edx.mobile.util.observer.Observer;
import org.edx.mobile.view.ViewHoldingPresenter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class UserProfileAccomplishmentsPresenter extends ViewHoldingPresenter<UserProfileAccomplishmentsPresenter.ViewInterface> {

    @NonNull
    private final UserAPI userAPI;

    @NonNull
    private final String username;

    private final boolean viewingOwnProfile;

    private InfiniteScrollUtils.PageLoadController pageLoadController;

    private int page = 1;

    @NonNull
    private List<BadgeAssertion> badges = new ArrayList<>();

    private boolean pageLoading = false;

    public UserProfileAccomplishmentsPresenter(@NonNull UserAPI userAPI, @NonNull UserPrefs userPrefs, @NonNull String username) {
        this.userAPI = userAPI;
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
                AsyncCallableUtils.observe(new Callable<Page<BadgeAssertion>>() {
                    @Override
                    public Page<BadgeAssertion> call() throws Exception {
                        return userAPI.getBadges(username, page);
                    }
                }, new Observer<Page<BadgeAssertion>>() {
                    @Override
                    public void onData(@NonNull final Page<BadgeAssertion> data) {
                        ++page;
                        // TODO: Better way to schedule this on main thread
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onPageLoaded(data);

                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull Throwable error) {
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

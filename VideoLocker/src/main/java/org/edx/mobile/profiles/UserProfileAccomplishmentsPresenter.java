package org.edx.mobile.profiles;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import org.edx.mobile.model.Page;
import org.edx.mobile.user.UserAPI;
import org.edx.mobile.util.observer.AsyncCallableUtils;
import org.edx.mobile.util.observer.Observer;
import org.edx.mobile.view.ViewHoldingPresenter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;

import java.util.concurrent.Callable;

public class UserProfileAccomplishmentsPresenter extends ViewHoldingPresenter<InfiniteScrollUtils.ListContentController<BadgeAssertion>> {

    @NonNull
    private final UserAPI userAPI;

    @NonNull
    private final String username;

    private InfiniteScrollUtils.PageLoadController pageLoadController;

    private int page;

    public UserProfileAccomplishmentsPresenter(@NonNull UserAPI userAPI, @NonNull String username) {
        this.userAPI = userAPI;
        this.username = username;
    }

    @Override
    public void attachView(@NonNull final InfiniteScrollUtils.ListContentController<BadgeAssertion> view) {
        super.attachView(view);
        page = 1;
        pageLoadController = new InfiniteScrollUtils.PageLoadController<>(view, new InfiniteScrollUtils.PageLoader<BadgeAssertion>() {
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

    public void onScrolledToEnd() {
        if (null == pageLoadController) {
            return;
        }
        pageLoadController.loadMore();
    }
}

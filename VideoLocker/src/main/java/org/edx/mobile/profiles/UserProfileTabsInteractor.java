package org.edx.mobile.profiles;

import android.support.annotation.NonNull;

import org.edx.mobile.R;
import org.edx.mobile.model.Page;
import org.edx.mobile.user.UserAPI;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.observer.AsyncCallableUtils;
import org.edx.mobile.util.observer.CachingObservable;
import org.edx.mobile.util.observer.Observable;
import org.edx.mobile.util.observer.Observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class UserProfileTabsInteractor {

    @NonNull
    private final String username;


    @NonNull
    private final CachingObservable<List<UserProfileTab>> tabs = new CachingObservable<>();

    public UserProfileTabsInteractor(@NonNull String username, @NonNull final UserAPI userAPI, @NonNull Config config) {
        this.username = username;
        tabs.onData(builtInTabs());
        if (config.isBadgesEnabled()) {
            AsyncCallableUtils.observe(new Callable<Page<BadgeAssertion>>() {
                @Override
                public Page<BadgeAssertion> call() throws Exception {
                    return userAPI.getBadges(UserProfileTabsInteractor.this.username, 1);
                }
            }, new Observer<Page<BadgeAssertion>>() {
                @Override
                public void onData(@NonNull Page<BadgeAssertion> data) {
                    handleBadgesLoaded(data);
                }

                @Override
                public void onError(@NonNull Throwable error) {
                    // do nothing. Better to just deal show what we can
                }
            });
        }
    }

    @NonNull
    public Observable<List<UserProfileTab>> observeTabs() {
        return tabs;
    }


    private List<UserProfileTab> builtInTabs() {
        return Collections.singletonList(new UserProfileTab(R.string.profile_tab_bio, UserProfileBioFragment.class));
    }

    private void handleBadgesLoaded(@NonNull Page<BadgeAssertion> badges) {
        if (badges.getCount() == 0) {
            return;
        }
        final List<UserProfileTab> knownTabs = new ArrayList<>();
        knownTabs.addAll(builtInTabs());
        knownTabs.add(new UserProfileTab(R.string.profile_tab_accomplishment, UserProfileAccomplishmentsFragment.class));
        tabs.onData(knownTabs);
    }
}

package org.edx.mobile.profiles;

import android.support.annotation.NonNull;

import org.edx.mobile.R;
import org.edx.mobile.http.Callback;
import org.edx.mobile.model.Page;
import org.edx.mobile.user.UserService;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.observer.CachingObservable;
import org.edx.mobile.util.observer.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserProfileTabsInteractor {

    @NonNull
    private final String username;


    @NonNull
    private final CachingObservable<List<UserProfileTab>> tabs = new CachingObservable<>();

    public UserProfileTabsInteractor(@NonNull String username, @NonNull final UserService userService, @NonNull Config config) {
        this.username = username;
        tabs.onData(builtInTabs());
        if (config.isBadgesEnabled()) {
            userService.getBadges(UserProfileTabsInteractor.this.username, 1)
                    .enqueue(new Callback<Page<BadgeAssertion>>() {
                        @Override
                        protected void onResponse(@NonNull Page<BadgeAssertion> badges) {
                            handleBadgesLoaded(badges);
                        }

                        @Override
                        protected void onFailure(@NonNull Throwable error) {
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

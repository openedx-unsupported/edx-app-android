package org.edx.mobile.tta.event;

import org.edx.mobile.tta.data.model.feed.SuggestedUser;

public class UserFollowingChangedEvent {

    private SuggestedUser user;

    public UserFollowingChangedEvent(SuggestedUser user) {
        this.user = user;
    }

    public SuggestedUser getUser() {
        return user;
    }

    public void setUser(SuggestedUser user) {
        this.user = user;
    }
}

package org.edx.mobile.tta.data.model.profile;

public class FollowStatus {

    private boolean is_following;
    private boolean is_followed;

    public boolean is_following() {
        return is_following;
    }

    public void set_following(boolean is_following) {
        this.is_following = is_following;
    }

    public boolean is_followed() {
        return is_followed;
    }

    public void set_followed(boolean is_followed) {
        this.is_followed = is_followed;
    }
}

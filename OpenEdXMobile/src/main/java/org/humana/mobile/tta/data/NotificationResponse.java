
package org.humana.mobile.tta.data;

import java.util.List;
import com.google.gson.annotations.SerializedName;


public class NotificationResponse {

    @SerializedName("Notifications")
    private List<Notification> Notifications;

    public List<Notification> getNotifications() {
        return Notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        Notifications = notifications;
    }

}

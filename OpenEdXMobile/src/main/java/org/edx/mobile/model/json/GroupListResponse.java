package org.edx.mobile.model.json;

public class GroupListResponse {

    private long id;
    private String name;
    private String icon;
    private String description;
    private boolean administrator = false;
    private int unread;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isAdministrator() {
        return administrator;
    }

    public void setAdministrator(boolean administrator) {
        this.administrator = administrator;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }
}

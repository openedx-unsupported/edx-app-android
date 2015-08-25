package org.edx.mobile.discussion;


import java.util.Date;

public interface IAuthorData {

    String getAuthor();
    PinnedAuthor getAuthorLabel();
    Date getCreatedAt();
}

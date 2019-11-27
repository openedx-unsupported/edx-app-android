/**
 Copyright (c) 2015 Qualcomm Education, Inc.
 All rights reserved.


 Redistribution and use in source and binary forms, with or without modification, are permitted (subject to the limitations in the disclaimer below) provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

 * Neither the name of Qualcomm Education, Inc. nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

 NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package org.edx.mobile.discussion;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.user.DiscussionUser;
import org.edx.mobile.user.ProfileImage;
import org.edx.mobile.user.ProfileImageProvider;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DiscussionComment implements Serializable, IAuthorData, ProfileImageProvider {
    private @SerializedName("id") String identifier;
    private String parentId;
    private String threadId;
    private String rawBody;
    private String renderedBody;
    private String author;
    private String authorLabel;
    private boolean voted = false;
    private int voteCount = 0;
    private Date createdAt;
    private Date updatedAt;
    private boolean endorsed = false;
    private String endorsedBy;
    private String endorsedByLabel;
    private Date endorsedAt;
    private boolean abuseFlagged = false;
    private List<String> editableFields;
    private int childCount = 0;
    @Nullable
    private Map<String, DiscussionUser> users;

    public String getIdentifier() {
        return identifier;
    }

    public String getParentId() {
        return parentId;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getRawBody() {
        return rawBody;
    }

    public String getRenderedBody() {
        return renderedBody;
    }

    public String getAuthor() {
        return author;
    }

    public String getAuthorLabel() {
        return authorLabel;
    }

    public boolean isVoted() {
        return voted;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public boolean isEndorsed() {
        return endorsed;
    }

    public String getEndorsedBy() {
        return endorsedBy;
    }

    public String getEndorsedByLabel() {
        return endorsedByLabel;
    }

    public Date getEndorsedAt() {
        return endorsedAt;
    }

    public boolean isAbuseFlagged() {
        return abuseFlagged;
    }

    public List<String> getEditableFields() {
        return editableFields;
    }

    public int getChildCount() {
        return childCount;
    }

    public void incrementChildCount() {
        childCount++;
    }

    public IAuthorData getEndorserData() {
        if (!endorsed) {
            return null;
        } else {
            return new IAuthorData() {
                @Override
                public String getAuthor() {
                    return endorsedBy;
                }

                @Override
                public String getAuthorLabel() {
                    return endorsedByLabel;
                }

                @Override
                public Date getCreatedAt() {
                    return endorsedAt;
                }

                @Override
                public boolean isAuthorAnonymous() {
                    // because a response cannot be endorsed anonymously
                    return false;
                }
            };
        }
    }

    @Override
    public boolean isAuthorAnonymous() {
        // because a comment or a response cannot be posted anonymously
        return false;
    }

    @Nullable
    public Map<String, DiscussionUser> getUsers() {
        return users;
    }

    @Nullable
    @Override
    public ProfileImage getProfileImage() {
        if (users == null || isAuthorAnonymous()) {
            return null;
        } else {
            return users.get(author).getProfile().getImage();
        }
    }

    /**
     * Incase of PATCH calls we get a {@link DiscussionComment} object that doesn't have
     * {@link #users} object, so, we patch the new object with existing {@link #users}
     * object.
     *
     * @param newObj Updated {@link DiscussionComment} object returned from server.
     * @return The patched object.
     */
    @NonNull
    public DiscussionComment patchObject(@NonNull DiscussionComment newObj) {
        newObj.users = users;
        return newObj;
    }
}

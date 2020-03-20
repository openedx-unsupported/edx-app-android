/**
 * Copyright (c) 2015 Qualcomm Education, Inc.
 * All rights reserved.
 * <p/>
 * <p/>
 * Redistribution and use in source and binary forms, with or without modification, are permitted (subject to the limitations in the disclaimer below) provided that the following conditions are met:
 * <p/>
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * <p/>
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * <p/>
 * Neither the name of Qualcomm Education, Inc. nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * <p/>
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package org.edx.mobile.discussion;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DiscussionTopic implements Serializable {
    public static final String ALL_TOPICS_ID = "ALL_TOPICS";
    public static final String FOLLOWING_TOPICS_ID = "FOLLOWING_TOPICS";

    @Nullable
    @SerializedName("id")
    String identifier = "";
    String name = "";
    String threadListUrl = "";
    List<DiscussionTopic> children = new ArrayList<>();

    /**
     * Returns the identifier for a discussion topic.
     * <br>
     * NOTE: The identifier for a parent topic is always null.
     *
     * @return The identifier for a discussion topic.
     */
    @Nullable
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(@Nullable String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThreadListUrl() {
        return threadListUrl;
    }

    public void setThreadListUrl(String threadListUrl) {
        this.threadListUrl = threadListUrl;
    }

    public List<DiscussionTopic> getChildren() {
        return children;
    }

    public void setChildren(List<DiscussionTopic> children) {
        this.children = children;
    }

    public boolean hasSameId(@NonNull DiscussionTopic discussionTopic) {
        return identifier != null && identifier.equals(discussionTopic.getIdentifier());
    }

    public boolean containsThread(@NonNull DiscussionThread discussionThread) {
        if (isAllType() || (isFollowingType() && discussionThread.isFollowing())) {
            return true;
        } else if (identifier != null && identifier.equals(discussionThread.getTopicId())) {
            return true;
        }
        for (DiscussionTopic child : children) {
            if (child.containsThread(discussionThread)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAllType() {
        return DiscussionTopic.ALL_TOPICS_ID.equals(identifier);
    }

    public boolean isFollowingType() {
        return DiscussionTopic.FOLLOWING_TOPICS_ID.equals(identifier);
    }
}

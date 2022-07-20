/**
 * Copyright (c) 2015 Qualcomm Education, Inc.
 * All rights reserved.
 *
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted (subject to the limitations in the disclaimer below) provided that the following conditions are met:
 *
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *
 * Neither the name of Qualcomm Education, Inc. nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.edx.mobile.model.discussion

import com.google.gson.annotations.SerializedName
import org.edx.mobile.model.user.DiscussionUser
import org.edx.mobile.model.user.ProfileImage
import org.edx.mobile.user.ProfileImageProvider
import java.io.Serializable
import java.util.*

class DiscussionThread : Serializable, IAuthorData, ProfileImageProvider {

    enum class ThreadType(val value: Int) {
        @SerializedName("discussion")
        DISCUSSION(0),

        @SerializedName("question")
        QUESTION(1);
    }

    @SerializedName("id")
    val identifier: String? = null

    @SerializedName("type")
    val type: ThreadType? = null

    @SerializedName("course_id")
    val courseId: String? = null

    @SerializedName("topic_id")
    val topicId: String? = null

    @SerializedName("group_id")
    val groupId = 0

    @SerializedName("group_name")
    val groupName: String? = null

    @SerializedName("title")
    val title: String? = null

    @SerializedName("raw_body")
    val rawBody: String? = null

    @SerializedName("rendered_body")
    val renderedBody: String? = null

    @SerializedName("author")
    private val author: String? = null

    @SerializedName("author_label")
    private val authorLabel: String? = null

    @SerializedName("comment_count")
    var commentCount = 0
        private set

    @SerializedName("unread_comment_count")
    var unreadCommentCount = 0
        private set

    @SerializedName("comment_list_url")
    val commentListUrl: String? = null

    @SerializedName("has_endorsed")
    val isHasEndorsed = false

    @SerializedName("pinned")
    val isPinned = false

    @SerializedName("closed")
    val isClosed = false

    @SerializedName("following")
    val isFollowing = false

    @SerializedName("abuse_flagged")
    val isAbuseFlagged = false

    @SerializedName("voted")
    val isVoted = false

    @SerializedName("vote_count")
    val voteCount = 0

    @SerializedName("created_at")
    private val createdAt: Date? = null

    @SerializedName("updated_at")
    val updatedAt: Date? = null

    @SerializedName("editable_fields")
    val editableFields: List<String>? = null

    @SerializedName("read")
    var isRead = false
        set(read) {
            field = read
            if (read) {
                unreadCommentCount = 0
            }
        }

    @SerializedName("users")
    var users: Map<String?, DiscussionUser>? = null
        private set

    /**
     * Since the response count field is not provided in the thread list query, it is defaulted
     * to -1 to indicate that it's not available.
     *
     * @return The response count, or -1 if it's not available.
     */
    var responseCount = -1
        private set

    override fun getAuthor(): String? {
        return if (isAuthorAnonymous) {
            "anonymous"
        } else author
    }

    override fun getAuthorLabel(): String? {
        return authorLabel
    }

    /**
     * Increment the response count.
     */
    fun incrementResponseCount() {
        responseCount++
        incrementCommentCount()
    }

    override fun getCreatedAt(): Date? {
        return createdAt
    }

    fun incrementCommentCount() {
        ++commentCount
    }

    fun hasSameId(discussionThread: DiscussionThread): Boolean {
        return discussionThread.identifier == identifier
    }

    fun containsComment(comment: DiscussionComment): Boolean {
        return comment.threadId == identifier
    }

    override fun isAuthorAnonymous(): Boolean {
        return author == null || author.isEmpty()
    }

    override fun getProfileImage(): ProfileImage? {
        return if (users == null || isAuthorAnonymous) {
            null
        } else {
            users?.get(author)?.profile?.image
        }
    }

    /**
     * Incase of PATCH calls we get a [DiscussionThread] object that doesn't have
     * [.users] object, so, we patch the new object with existing [.users]
     * object.
     *
     * @param newObj Updated [DiscussionThread] object returned from server.
     * @return The patched object.
     */
    fun patchObject(newObj: DiscussionThread): DiscussionThread {
        newObj.users = users
        return newObj
    }
}

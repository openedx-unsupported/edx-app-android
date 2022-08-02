/**
 * Copyright (c) 2015 Qualcomm Education, Inc.
 * All rights reserved.
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

class DiscussionComment : Serializable, IAuthorData, ProfileImageProvider {
    @SerializedName("id")
    val identifier: String? = null

    @SerializedName("parent_id")
    val parentId: String? = null

    @SerializedName("thread_id")
    val threadId: String? = null

    @SerializedName("raw_body")
    val rawBody: String? = null

    @SerializedName("rendered_body")
    val renderedBody: String? = null

    @SerializedName("author")
    private val author: String? = null

    @SerializedName("author_label")
    private val authorLabel: String? = null

    @SerializedName("voted")
    val isVoted = false

    @SerializedName("vote_count")
    val voteCount = 0

    @SerializedName("created_at")
    private val createdAt: Date? = null

    @SerializedName("updated_at")
    val updatedAt: Date? = null

    @SerializedName("endorsed")
    val isEndorsed = false

    @SerializedName("endorsed_by")
    val endorsedBy: String? = null

    @SerializedName("endorsed_by_label")
    val endorsedByLabel: String? = null

    @SerializedName("endorsed_at")
    val endorsedAt: Date? = null

    @SerializedName("abuse_flagged")
    val isAbuseFlagged = false

    @SerializedName("editable_fields")
    val editableFields: List<String>? = null

    @SerializedName("child_count")
    var childCount = 0
        private set

    @SerializedName("users")
    var users: Map<String?, DiscussionUser>? = null
        private set

    override fun getAuthor(): String? {
        return author
    }

    override fun getAuthorLabel(): String? {
        return authorLabel
    }

    override fun getCreatedAt(): Date? {
        return createdAt
    }

    fun incrementChildCount() {
        childCount++
    }

    // because a response cannot be endorsed anonymously
    val endorserData: IAuthorData?
        get() = if (!isEndorsed) {
            null
        } else {
            object : IAuthorData {
                override fun getAuthor(): String? {
                    return endorsedBy
                }

                override fun getAuthorLabel(): String? {
                    return endorsedByLabel
                }

                override fun getCreatedAt(): Date? {
                    return endorsedAt
                }

                override fun isAuthorAnonymous(): Boolean {
                    // because a response cannot be endorsed anonymously
                    return false
                }
            }
        }

    override fun isAuthorAnonymous(): Boolean {
        // because a comment or a response cannot be posted anonymously
        return false
    }

    override fun getProfileImage(): ProfileImage? {
        return if (users == null || isAuthorAnonymous) {
            null
        } else {
            users?.get(author)?.profile?.image
        }
    }

    /**
     * Incase of PATCH calls we get a [DiscussionComment] object that doesn't have
     * [.users] object, so, we patch the new object with existing [.users]
     * object.
     *
     * @param newObj Updated [DiscussionComment] object returned from server.
     * @return The patched object.
     */
    fun patchObject(newObj: DiscussionComment): DiscussionComment {
        newObj.users = users
        return newObj
    }
}

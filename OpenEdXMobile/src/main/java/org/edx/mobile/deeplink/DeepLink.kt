package org.edx.mobile.deeplink

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

open class DeepLink(val screenName: String) : Parcelable {
    object Keys {
        const val SCREEN_NAME = "screen_name"
        const val COURSE_ID = "course_id"
        const val COMPONENT_ID = "component_id"
        const val PATH_ID = "path_id"
        const val TOPIC_ID = "topic_id"
        const val THREAD_ID = "thread_id"
    }

    var courseId: String? = null
    var componentId: String? = null
    var topicId: String? = null
    var threadId: String? = null
    var pathId: String? = null
        get() = if (screenName == Screen.DISCOVERY_COURSE_DETAIL) {
            courseId
        } else {
            field
        }

    constructor(screenName: String, paramsJson: JSONObject) : this(screenName) {
        courseId = paramsJson.optString(Keys.COURSE_ID)
        componentId = paramsJson.optString(Keys.COMPONENT_ID)
        pathId = paramsJson.optString(Keys.PATH_ID)
        topicId = paramsJson.optString(Keys.TOPIC_ID)
        threadId = paramsJson.optString(Keys.THREAD_ID)
    }

    constructor(screenName: String, bundle: Bundle) : this(screenName) {
        courseId = bundle.getString(Keys.COURSE_ID)
        componentId = bundle.getString(Keys.COMPONENT_ID)
        pathId = bundle.getString(Keys.PATH_ID)
        topicId = bundle.getString(Keys.TOPIC_ID)
        threadId = bundle.getString(Keys.THREAD_ID)
    }

    constructor(screenName: String, map: Map<String, String>) : this(screenName) {
        courseId = map[Keys.COURSE_ID]
        componentId = map[Keys.COMPONENT_ID]
        pathId = map[Keys.PATH_ID]
        topicId = map[Keys.TOPIC_ID]
        threadId = map[Keys.THREAD_ID]
    }

    override fun toString(): String {
        return "DeepLink(screenName='$screenName', courseId=$courseId, componentId=$componentId, " +
                "pathId=$pathId, topicID=$topicId, threadID=$threadId)"
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DeepLink> = object : Parcelable.Creator<DeepLink> {
            override fun createFromParcel(source: Parcel): DeepLink = DeepLink(source)
            override fun newArray(size: Int): Array<DeepLink?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString().toString()) {
        this.courseId = source.readString()
        this.componentId = source.readString()
        this.pathId = source.readString()
        this.topicId = source.readString()
        this.threadId = source.readString()
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(screenName)
        writeString(courseId)
        writeString(componentId)
        writeString(pathId)
        writeString(topicId)
        writeString(threadId)
    }
}

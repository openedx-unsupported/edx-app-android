package org.edx.mobile.model.api

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import org.edx.mobile.logger.Logger
import org.edx.mobile.model.iap.IAPFlowData
import java.io.Serializable
import java.lang.reflect.Type

data class EnrollmentResponse(
    @SerializedName("config")
    val appConfig: AppConfig,

    @SerializedName("enrollments")
    val enrollments: List<EnrolledCoursesResponse>
) : Serializable {

    class Deserializer : JsonDeserializer<EnrollmentResponse> {
        private val logger = Logger(Deserializer::class.java.name)

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): EnrollmentResponse {
            if (json == null) return EnrollmentResponse(AppConfig(), emptyList())

            var enrolledCourses: List<EnrolledCoursesResponse> = emptyList()
            return try {
                if (json.isJsonArray) {
                    enrolledCourses = Gson().fromJson(
                        json,
                        object : TypeToken<List<EnrolledCoursesResponse>>() {}.type
                    )
                    EnrollmentResponse(AppConfig(), enrolledCourses)
                } else {
                    enrolledCourses = Gson().fromJson(
                        (json as JsonObject).get("enrollments"),
                        object : TypeToken<List<EnrolledCoursesResponse>>() {}.type
                    )

                    /**
                     * To remove dependency on the backend, all the data related to Remote Config
                     * will be received under the `configs` key. The `config` is the key under
                     * 'configs` which defines the data that is related to the configuration of the
                     * app.
                     */
                    val config = json
                        .getAsJsonObject("configs")
                        .getAsJsonPrimitive("config")

                    val appConfig = Gson().fromJson(
                        config.asString,
                        AppConfig::class.java
                    )

                    EnrollmentResponse(appConfig, enrolledCourses)
                }
            } catch (ex: Exception) {
                logger.error(ex, true)
                EnrollmentResponse(AppConfig(), enrolledCourses)
            }
        }
    }
}

/**
 * Method to filter the audit courses from the given enrolled course list.
 *
 * @return the list of all audit courses with non-null Skus.
 */
fun List<EnrolledCoursesResponse>.getAuditCourses(): List<IAPFlowData> {
    return this.filter {
        it.isAuditMode && it.courseSku.isNullOrBlank().not()
    }.mapNotNull { course ->
        course.courseSku?.let { sku ->
            IAPFlowData(
                courseId = course.courseId,
                productId = sku,
                isCourseSelfPaced = course.course.isSelfPaced
            )
        }
    }.toList()
}

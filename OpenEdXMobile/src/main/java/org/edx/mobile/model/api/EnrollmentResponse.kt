package org.edx.mobile.model.api

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import org.edx.mobile.logger.Logger
import org.edx.mobile.model.course.EnrollmentMode
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

            return try {
                if (json.isJsonArray) {
                    val listType = object : TypeToken<List<EnrolledCoursesResponse>>() {}.type

                    EnrollmentResponse(
                        AppConfig(),
                        Gson().fromJson((json as JsonArray), listType)
                    )
                } else {
                    /**
                     * To remove dependency on the backend, all the data related to Remote Config
                     * will be received under the `configs` key. The `config` is the key under
                     * 'configs` which defines the data that is related to the configuration of the
                     * app.
                     */
                    val config = (json as JsonObject)
                        .getAsJsonObject("configs")
                        .getAsJsonPrimitive("config")

                    val appConfig = Gson().fromJson(
                        config.asString,
                        AppConfig::class.java
                    )
                    val enrolledCourses = Gson().fromJson<List<EnrolledCoursesResponse>>(
                        json.get("enrollments"),
                        object : TypeToken<List<EnrolledCoursesResponse>>() {}.type
                    )

                    EnrollmentResponse(appConfig, enrolledCourses)
                }
            } catch (ex: Exception) {
                logger.error(ex, true)
                EnrollmentResponse(AppConfig(), emptyList())
            }
        }
    }
}

/**
 * Method to filter the audit course Skus from the given enrolled course list.
 *
 * @return the list of all audit courses SKUs.
 */
fun List<EnrolledCoursesResponse>.getAuditCoursesSku(): List<String> {
    return this.filter { EnrollmentMode.AUDIT.toString().equals(it.mode, true) }
        .mapNotNull { it.courseSku }.toList()
}

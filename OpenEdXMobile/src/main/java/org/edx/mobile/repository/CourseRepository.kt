package org.edx.mobile.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.edx.mobile.course.CourseAPI
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.model.NetworkResponseCallback
import org.edx.mobile.http.model.Result
import org.edx.mobile.model.api.EnrollmentResponse
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.model.course.CourseStructureV1Model
import org.edx.mobile.services.CourseManager
import org.edx.mobile.viewModel.CourseViewModel.CoursesRequestType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepository @Inject constructor(
    private val courseAPI: CourseAPI, private var courseManager: CourseManager
) {
    fun fetchEnrolledCourses(
        type: CoursesRequestType,
        callback: NetworkResponseCallback<EnrollmentResponse>
    ) {
        val call = when (type) {
            CoursesRequestType.STALE -> courseAPI.enrolledCourses
            CoursesRequestType.PERSISTABLE_CACHE -> courseAPI.enrolledCoursesFromCache
            CoursesRequestType.LIVE -> courseAPI.enrolledCoursesWithoutStale
            else -> throw java.lang.Exception("Unknown Request Type: $type")
        }

        call.enqueue(object : Callback<EnrollmentResponse> {
            override fun onResponse(
                call: Call<EnrollmentResponse>,
                response: Response<EnrollmentResponse>
            ) {
                when (response.isSuccessful && response.body() != null) {
                    true -> callback.onSuccess(
                        Result.Success(
                            isSuccessful = response.isSuccessful,
                            data = response.body(),
                            code = response.code(),
                            message = response.message()
                        )
                    )
                    false -> callback.onError(
                        Result.Error(
                            HttpStatusException(response.code(), response.message())
                        )
                    )
                }
            }

            override fun onFailure(call: Call<EnrollmentResponse>, t: Throwable) {
                callback.onError(Result.Error(t))
            }
        })
    }

    suspend fun getCourseComponentsFromAppLevelCache(
        courseId: String,
        courseComponentId: String? = null,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): CourseComponent? = withContext(dispatcher) {
        if (courseComponentId?.isNotEmpty() == true) {
            // Course data exist in app session cache
            courseManager.getComponentByIdFromAppLevelCache(courseId, courseComponentId)
        } else {
            // Check if course data is available in app session cache
            courseManager.getCourseDataFromAppLevelCache(courseId)
        }
    }

    suspend fun getCourseDataFromPersistableCache(
        courseId: String,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): CourseComponent? = withContext(dispatcher) {
        courseManager.getCourseDataFromPersistableCache(courseId)
    }

    suspend fun getCourseStructure(
        courseId: String,
        coursesRequestType: CoursesRequestType = CoursesRequestType.LIVE,
        defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    ): CourseComponent? = withContext(defaultDispatcher) {
        val courseDataDeferred =
            async { getCourseStructureCall(courseId, coursesRequestType).execute() }
        val courseDataResponse = courseDataDeferred.await()
        var courseComponent: CourseComponent? = null
        if (courseDataResponse.isSuccessful) {
            courseDataResponse.body()?.also { courseStructureV1Model ->
                courseComponent = CourseAPI.normalizeCourseStructure(
                    courseStructureV1Model, courseId
                ) as CourseComponent?
                // Update the course data cache after Course Purchase
                courseComponent?.let {
                    courseManager.addCourseDataInAppLevelCache(courseId, it)
                }
            }
        } else {
            throw HttpStatusException(courseDataResponse.raw())
        }
        courseComponent
    }

    private fun getCourseStructureCall(
        courseId: String, coursesRequestType: CoursesRequestType
    ): Call<CourseStructureV1Model> {
        return when (coursesRequestType) {
            CoursesRequestType.STALE -> {
                courseAPI.getCourseStructureWithStale(courseId)
            }
            CoursesRequestType.LIVE -> {
                courseAPI.getCourseStructureWithoutStale(courseId)
            }
            else -> {
                throw java.lang.Exception("Unknown Request Type: $coursesRequestType")
            }
        }
    }
}

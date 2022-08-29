package org.edx.mobile.repository

import org.edx.mobile.course.CourseAPI
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.model.NetworkResponseCallback
import org.edx.mobile.http.model.Result
import org.edx.mobile.model.course.CourseBannerInfoModel
import org.edx.mobile.model.course.CourseDates
import org.edx.mobile.model.course.ResetCourseDates
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CourseDatesRepository(private val courseAPI: CourseAPI) {
    private var courseDates: CourseDates? = null

    /**
     * Fetch course dates against course Id.
     */
    fun getCourseDates(
        courseId: String,
        forceRefresh: Boolean,
        callback: NetworkResponseCallback<CourseDates>
    ) {
        if (forceRefresh || courseDates == null) {
            courseDates = null
            courseAPI.getCourseDates(courseId).enqueue(object : Callback<CourseDates> {
                override fun onResponse(call: Call<CourseDates>, response: Response<CourseDates>) {
                    courseDates = response.body()
                    callback.onSuccess(
                        Result.Success<CourseDates>(
                            isSuccessful = response.isSuccessful,
                            data = response.body(),
                            code = response.code(),
                            message = response.message()
                        )
                    )
                }

                override fun onFailure(call: Call<CourseDates>, t: Throwable) {
                    callback.onError(Result.Error(t))
                }
            })
        } else {
            callback.onSuccess(
                Result.Success<CourseDates>(
                    isSuccessful = true,
                    data = courseDates,
                    code = HttpStatus.OK,
                    message = ""
                )
            )
        }
    }

    /**
     * Fetch course dates deadline info against course Id
     */
    fun getCourseBannerInfo(
        courseId: String,
        callback: NetworkResponseCallback<CourseBannerInfoModel>
    ) {
        courseAPI.getCourseBannerInfo(courseId).enqueue(object : Callback<CourseBannerInfoModel> {
            override fun onResponse(
                call: Call<CourseBannerInfoModel>,
                response: Response<CourseBannerInfoModel>
            ) {
                callback.onSuccess(
                    Result.Success<CourseBannerInfoModel>(
                        isSuccessful = response.isSuccessful,
                        data = response.body(),
                        code = response.code(),
                        message = response.message()
                    )
                )
            }

            override fun onFailure(call: Call<CourseBannerInfoModel>, t: Throwable) {
                callback.onError(Result.Error(t))
            }
        })
    }

    /**
     * Reschedule course dates.
     */
    fun resetCourseDates(
        body: HashMap<String, String>,
        callback: NetworkResponseCallback<ResetCourseDates>
    ) {
        courseAPI.resetCourseDates(body).enqueue(object : Callback<ResetCourseDates> {
            override fun onResponse(
                call: Call<ResetCourseDates>,
                response: Response<ResetCourseDates>
            ) {
                callback.onSuccess(
                    Result.Success<ResetCourseDates>(
                        isSuccessful = response.isSuccessful,
                        data = response.body(),
                        code = response.code(),
                        message = response.message()
                    )
                )
            }

            override fun onFailure(call: Call<ResetCourseDates>, t: Throwable) {
                callback.onError(Result.Error(t))
            }
        })
    }
}

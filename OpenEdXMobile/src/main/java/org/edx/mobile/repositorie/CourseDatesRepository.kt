package org.edx.mobile.repositorie

import org.edx.mobile.base.MainApplication
import org.edx.mobile.course.CourseAPI
import org.edx.mobile.http.model.NetworkResponseCallback
import org.edx.mobile.http.model.Result
import org.edx.mobile.model.course.CourseDates
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import roboguice.RoboGuice

class CourseDatesRepository {

    private var courseAPI: CourseAPI = RoboGuice.getInjector(MainApplication.application).getInstance(CourseAPI::class.java)

    companion object {
        private var instance: CourseDatesRepository? = null
        fun getInstance(): CourseDatesRepository {
            instance?.let { it ->
                return it
            }
            val repository = CourseDatesRepository()
            instance = CourseDatesRepository()
            return repository
        }
    }

    fun getCourseDates(courseId: String, callback: NetworkResponseCallback<CourseDates>) {
        courseAPI.getCourseDates(courseId).enqueue(object : Callback<CourseDates> {
            override fun onResponse(call: Call<CourseDates>, response: Response<CourseDates>) {
                callback.onSuccess(Result.Success<CourseDates>(isSuccessful = response.isSuccessful,
                        data = response.body(),
                        code = response.code(),
                        message = response.message()))
            }

            override fun onFailure(call: Call<CourseDates>, t: Throwable) {
                callback.onError(Result.Error(t))
            }
        })
    }
}

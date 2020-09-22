package org.edx.mobile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.model.NetworkResponseCallback
import org.edx.mobile.http.model.Result
import org.edx.mobile.model.course.CourseDates
import org.edx.mobile.repositorie.CourseDatesRepository
import retrofit2.Response

class CourseDateViewModel(
        private val repository: CourseDatesRepository = CourseDatesRepository.getInstance()
) : ViewModel() {

    private val _showLoader = MutableLiveData<Boolean>()
    val showLoader: LiveData<Boolean>
        get() = _showLoader

    private val _swipeRefresh = MutableLiveData<Boolean>()
    val swipeRefresh: LiveData<Boolean>
        get() = _swipeRefresh

    private val _courseDates = MutableLiveData<CourseDates>()
    val courseDates: LiveData<CourseDates>
        get() = _courseDates

    private val _errorMessage = MutableLiveData<Throwable>()
    val errorMessage: LiveData<Throwable>
        get() = _errorMessage

    fun fetchCourseDates(courseID: String, isSwipeRefresh: Boolean = false) {
        _errorMessage.value = null
        _swipeRefresh.value = isSwipeRefresh
        _showLoader.value = isSwipeRefresh.not()
        repository.getCourseDates(
                courseId = courseID,
                callback = object : NetworkResponseCallback<CourseDates> {
                    override fun onSuccess(result: Result.Success<CourseDates>) {
                        if (result.isSuccessful && result.data != null) {
                            _courseDates.value = result.data
                        } else {
                            setError(result.code, result.message)
                        }
                        _showLoader.postValue(false)
                        _swipeRefresh.postValue(false)
                    }

                    override fun onError(error: Result.Error) {
                        _showLoader.postValue(false)
                        _errorMessage.value = error.throwable
                        _swipeRefresh.postValue(false)
                    }
                }
        )
    }

    fun setError(code: Int, msg: String) {
        _errorMessage.value = HttpStatusException(Response.error<Any>(code,
                ResponseBody.create(MediaType.parse("text/plain"), msg)))
    }
}

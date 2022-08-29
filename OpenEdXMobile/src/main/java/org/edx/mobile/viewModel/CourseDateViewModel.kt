package org.edx.mobile.viewModel

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.constants.ApiConstants
import org.edx.mobile.http.model.NetworkResponseCallback
import org.edx.mobile.http.model.Result
import org.edx.mobile.model.course.CourseBannerInfoModel
import org.edx.mobile.model.course.CourseDates
import org.edx.mobile.model.course.ResetCourseDates
import org.edx.mobile.repository.CourseDatesRepository
import org.edx.mobile.util.CalendarUtils
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CourseDateViewModel @Inject constructor(
    private val repository: CourseDatesRepository
) : ViewModel() {

    private val _syncLoader = MutableLiveData<Boolean>()
    val syncLoader: LiveData<Boolean>
        get() = _syncLoader

    private val _showLoader = MutableLiveData<Boolean>()
    val showLoader: LiveData<Boolean>
        get() = _showLoader

    private val _swipeRefresh = MutableLiveData<Boolean>()
    val swipeRefresh: LiveData<Boolean>
        get() = _swipeRefresh

    private val _courseDates = MutableLiveData<CourseDates>()
    val courseDates: LiveData<CourseDates>
        get() = _courseDates

    private val _bannerInfo = MutableLiveData<CourseBannerInfoModel?>()
    val bannerInfo: LiveData<CourseBannerInfoModel?>
        get() = _bannerInfo

    private val _resetCourseDates = MutableLiveData<ResetCourseDates?>()
    val resetCourseDates: LiveData<ResetCourseDates?>
        get() = _resetCourseDates

    private val _errorMessage = MutableLiveData<ErrorMessage?>()
    val errorMessage: LiveData<ErrorMessage?>
        get() = _errorMessage

    private var syncingCalendarTime: Long = 0L
    var areEventsUpdated: Boolean = false

    fun getSyncingCalendarTime(): Long = syncingCalendarTime
    fun resetSyncingCalendarTime() {
        syncingCalendarTime = 0L
    }

    fun addOrUpdateEventsInCalendar(
        context: Context,
        calendarId: Long,
        courseId: String,
        courseName: String,
        isDeepLinkEnabled: Boolean,
        updatedEvent: Boolean
    ) {
        resetSyncingCalendarTime()
        val syncingCalendarStartTime: Long = Calendar.getInstance().timeInMillis
        areEventsUpdated = updatedEvent
        _syncLoader.value = true

        viewModelScope.launch(Dispatchers.IO) {
            courseDates.value?.let { courseDates ->
                courseDates.courseDateBlocks?.forEach { courseDateBlock ->
                    CalendarUtils.addEventsIntoCalendar(
                        context = context,
                        calendarId = calendarId,
                        courseId = courseId,
                        courseName = courseName,
                        courseDateBlock = courseDateBlock,
                        isDeeplinkEnabled = isDeepLinkEnabled
                    )
                }
                syncingCalendarTime = Calendar.getInstance().timeInMillis - syncingCalendarStartTime
                if (courseDates.courseDateBlocks?.size == 0) {
                    syncingCalendarTime = 0
                } else if (syncingCalendarTime < 1000) {
                    // Add 1 sec delay to dismiss the dialog to avoid flickering
                    // if the event creation time is less then 1 sec
                    SystemClock.sleep(1000 - syncingCalendarTime)
                }
            } ?: run { syncingCalendarTime = 0 }
            _syncLoader.postValue(false)
        }
    }

    fun fetchCourseDates(
        courseID: String,
        forceRefresh: Boolean,
        showLoader: Boolean = false,
        isSwipeRefresh: Boolean = false
    ) {
        _errorMessage.value = null
        _swipeRefresh.value = isSwipeRefresh
        _showLoader.value = showLoader
        repository.getCourseDates(
            courseId = courseID,
            forceRefresh = forceRefresh,
            callback = object : NetworkResponseCallback<CourseDates> {
                override fun onSuccess(result: Result.Success<CourseDates>) {
                    if (result.isSuccessful && result.data != null) {
                        result.data.let {
                            _courseDates.postValue(it)
                        }
                        fetchCourseDatesBannerInfo(courseID, true)
                    } else {
                        setError(ErrorMessage.COURSE_DATES_CODE, result.code, result.message)
                    }
                    _showLoader.postValue(false)
                    _swipeRefresh.postValue(false)
                }

                override fun onError(error: Result.Error) {
                    _showLoader.postValue(false)
                    _errorMessage.postValue(
                        ErrorMessage(
                            ErrorMessage.COURSE_DATES_CODE,
                            error.throwable
                        )
                    )
                    _swipeRefresh.postValue(false)
                }
            }
        )
    }

    fun fetchCourseDatesBannerInfo(courseID: String, showLoader: Boolean = false) {
        _errorMessage.value = null
        _showLoader.value = showLoader
        repository.getCourseBannerInfo(
            courseId = courseID,
            callback = object : NetworkResponseCallback<CourseBannerInfoModel> {
                override fun onSuccess(result: Result.Success<CourseBannerInfoModel>) {
                    if (result.isSuccessful && result.data != null) {
                        _bannerInfo.postValue(result.data)
                    } else {
                        setError(ErrorMessage.BANNER_INFO_CODE, result.code, result.message)
                    }
                    _showLoader.postValue(false)
                    _swipeRefresh.postValue(false)
                }

                override fun onError(error: Result.Error) {
                    _showLoader.postValue(false)
                    _errorMessage.postValue(
                        ErrorMessage(
                            ErrorMessage.BANNER_INFO_CODE,
                            error.throwable
                        )
                    )
                    _swipeRefresh.postValue(false)
                }
            }
        )
    }

    fun resetCourseDatesBanner(courseID: String) {
        _errorMessage.value = null
        _showLoader.value = true
        val courseBody = HashMap<String, String>()
        courseBody[ApiConstants.COURSE_KEY] = courseID
        repository.resetCourseDates(
            body = courseBody,
            callback = object : NetworkResponseCallback<ResetCourseDates> {
                override fun onSuccess(result: Result.Success<ResetCourseDates>) {
                    if (result.isSuccessful && result.data != null) {
                        _resetCourseDates.postValue(result.data)
                        fetchCourseDates(
                            courseID,
                            forceRefresh = true,
                            showLoader = false,
                            isSwipeRefresh = false
                        )
                    } else {
                        setError(ErrorMessage.COURSE_RESET_DATES_CODE, result.code, result.message)
                    }
                    _showLoader.postValue(false)
                    _swipeRefresh.postValue(false)
                }

                override fun onError(error: Result.Error) {
                    _showLoader.postValue(false)
                    _errorMessage.postValue(
                        ErrorMessage(
                            ErrorMessage.COURSE_RESET_DATES_CODE,
                            error.throwable
                        )
                    )
                    _swipeRefresh.postValue(false)
                }
            }
        )
    }

    fun setError(errorCode: Int, httpStatusCode: Int, msg: String) {
        _errorMessage.value = ErrorMessage(errorCode, HttpStatusException(httpStatusCode, msg))
    }
}

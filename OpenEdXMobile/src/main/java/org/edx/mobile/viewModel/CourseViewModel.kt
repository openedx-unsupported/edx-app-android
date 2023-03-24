package org.edx.mobile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.model.NetworkResponseCallback
import org.edx.mobile.http.model.Result
import org.edx.mobile.logger.Logger
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.api.EnrollmentResponse
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.module.db.DataCallback
import org.edx.mobile.repository.CourseRepository
import org.edx.mobile.util.observer.Event
import org.edx.mobile.util.observer.postEvent
import org.edx.mobile.viewModel.CourseViewModel.CoursesRequestType.CACHE
import org.edx.mobile.viewModel.CourseViewModel.CoursesRequestType.LIVE
import org.edx.mobile.viewModel.CourseViewModel.CoursesRequestType.STALE
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val environment: IEdxEnvironment,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val logger: Logger = Logger(CourseViewModel::class.java.simpleName)

    private val _enrolledCourses = MutableLiveData<Event<List<EnrolledCoursesResponse>>>()
    val enrolledCoursesResponse: LiveData<Event<List<EnrolledCoursesResponse>>> = _enrolledCourses

    private val _courseComponent = MutableLiveData<Event<CourseComponent>>()
    val courseComponent: LiveData<Event<CourseComponent>> = _courseComponent

    private val _showProgress = MutableLiveData(true)
    val showProgress: LiveData<Boolean> = _showProgress

    private val _swipeRefresh = MutableLiveData<Boolean>()
    val swipeRefresh: LiveData<Boolean>
        get() = _swipeRefresh

    private val _handleError = MutableLiveData<Throwable>()
    val handleError: LiveData<Throwable> = _handleError

    var courseRequestType: CoursesRequestType

    init {
        courseRequestType = CoursesRequestType.NONE
    }

    fun fetchEnrolledCourses(
        type: CoursesRequestType,
        showProgress: Boolean = true
    ) {
        if (environment.loginPrefs.isUserLoggedIn.not()) {
            _handleError.value = HttpStatusException(HttpStatus.UNAUTHORIZED, "")
            return
        }
        _showProgress.postValue(showProgress)
        courseRepository.fetchEnrolledCourses(
            type = type,
            callback = object : NetworkResponseCallback<EnrollmentResponse> {

                override fun onSuccess(result: Result.Success<EnrollmentResponse>) {
                    result.data?.let {
                        courseRequestType = type
                        _enrolledCourses.postEvent(it.enrollments)
                        environment.appFeaturesPrefs.setAppConfig(it.appConfig)

                        if (type != CACHE) {
                            updateDatabaseAfterDownload(it.enrollments)
                        } else {
                            fetchEnrolledCourses(type = STALE, it.enrollments.isEmpty())
                        }
                    }
                }

                override fun onError(error: Result.Error) {
                    if (type == CACHE) {
                        fetchEnrolledCourses(type = STALE)
                    } else {
                        _handleError.value = error.throwable
                    }
                }
            })
    }

    private fun updateDatabaseAfterDownload(list: List<EnrolledCoursesResponse>?) {
        val dataCallback: DataCallback<Int> = object : DataCallback<Int>() {
            override fun onResult(result: Int) {}
            override fun onFail(ex: Exception) {
                logger.error(ex)
            }
        }

        if (list != null && list.isEmpty()) {
            //update all videos in the DB as Deactivated
            environment.database?.updateAllVideosAsDeactivated(dataCallback)

            // Update all videos for a course fetched in the API as Activated
            list.filter { it.isActive }.forEach {
                environment.database?.updateVideosActivatedForCourse(
                    it.courseId,
                    dataCallback
                )
            }

            //Delete all videos which are marked as Deactivated in the database
            environment.storage?.deleteAllUnenrolledVideos()
        }
    }

    fun getCourseData(
        courseId: String,
        courseComponentId: String?,
        showProgress: Boolean = false,
        swipeRefresh: Boolean = false,
        coursesRequestType: CoursesRequestType = LIVE
    ) {
        viewModelScope.launch {
            _swipeRefresh.postValue(swipeRefresh)
            _showProgress.postValue(showProgress)
            val courseComponentData: kotlin.Result<CourseComponent?> = when (coursesRequestType) {
                CACHE -> {
                    runCatching {
                        courseRepository.getCourseComponentsFromCache(
                            courseId,
                            courseComponentId,
                            Dispatchers.IO
                        )
                    }
                }
                STALE, LIVE -> {
                    runCatching {
                        courseRepository.getCourseStructure(
                            courseId,
                            coursesRequestType,
                            Dispatchers.IO
                        )
                    }
                }
                else -> {
                    throw java.lang.Exception("Unknown Request Type: $coursesRequestType")
                }
            }
            courseComponentData.onSuccess { courseComponent ->
                _swipeRefresh.postValue(false)
                _showProgress.postValue(false)
                if (courseComponent == null) {
                    if ((coursesRequestType == CACHE || coursesRequestType == STALE)) {
                        getCourseData(
                            courseId,
                            courseComponentId,
                            showProgress = showProgress,
                            swipeRefresh = false,
                            coursesRequestType = LIVE
                        )
                    }
                } else {
                    _courseComponent.postEvent(courseComponent)
                    // if course data exist in persistable cache update the course data in background
                    if (coursesRequestType == CACHE) {
                        getCourseData(
                            courseId,
                            courseComponentId,
                            showProgress = false,
                            swipeRefresh = false,
                            coursesRequestType = STALE
                        )
                    }
                }

            }.onFailure {
                if (coursesRequestType == LIVE || coursesRequestType == STALE) {
                    _handleError.postValue(it)
                } else {
                    getCourseData(
                        courseId,
                        courseComponentId,
                        showProgress = true,
                        swipeRefresh = false,
                        coursesRequestType = STALE
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        courseRequestType = CoursesRequestType.NONE
    }

    sealed class CoursesRequestType {
        object LIVE : CoursesRequestType()
        object STALE : CoursesRequestType()
        object CACHE : CoursesRequestType()
        object NONE : CoursesRequestType()
    }
}

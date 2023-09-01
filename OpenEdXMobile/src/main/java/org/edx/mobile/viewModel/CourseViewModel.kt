package org.edx.mobile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import org.edx.mobile.viewModel.CourseViewModel.CoursesRequestType.APP_LEVEL_CACHE
import org.edx.mobile.viewModel.CourseViewModel.CoursesRequestType.LIVE
import org.edx.mobile.viewModel.CourseViewModel.CoursesRequestType.PERSISTABLE_CACHE
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

    private val _lastAccessedComponent = MutableLiveData<Event<CourseComponent>>()
    val lastAccessedComponent: LiveData<Event<CourseComponent>> = _lastAccessedComponent

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
                        environment.featuresPrefs.appConfig = it.appConfig

                        if (type != PERSISTABLE_CACHE) {
                            updateDatabaseAfterDownload(it.enrollments)
                        } else {
                            fetchEnrolledCourses(type = STALE, it.enrollments.isEmpty())
                        }
                    }
                }

                override fun onError(error: Result.Error) {
                    if (type == PERSISTABLE_CACHE) {
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
        courseComponentId: String? = null,
        showProgress: Boolean = false,
        swipeRefresh: Boolean = false,
        coursesRequestType: CoursesRequestType = LIVE
    ) {
        viewModelScope.launch {
            _swipeRefresh.postValue(swipeRefresh)
            _showProgress.postValue(showProgress)

            val courseComponentResult =
                getCourseComponentResults(courseId, courseComponentId, coursesRequestType)

            courseComponentResult.onSuccess { courseComponent ->
                _swipeRefresh.postValue(false)
                _showProgress.postValue(false)
                if (courseComponent == null) {
                    var newRequestType: CoursesRequestType = LIVE
                    if (coursesRequestType == APP_LEVEL_CACHE) {
                        // Course data is not available in app session cache
                        newRequestType = PERSISTABLE_CACHE

                    } else if (coursesRequestType == PERSISTABLE_CACHE || coursesRequestType == STALE) {
                        // Course data is neither available in app session cache nor available in persistable cache
                        newRequestType = LIVE
                    }
                    getCourseData(
                        courseId,
                        courseComponentId,
                        showProgress = true,
                        swipeRefresh = false,
                        coursesRequestType = newRequestType
                    )
                } else {
                    if (coursesRequestType == PERSISTABLE_CACHE) {
                        // Course data exist in persistable cache
                        // Send a server call in background for refreshed data
                        getCourseData(
                            courseId,
                            courseComponentId,
                            showProgress = false,
                            swipeRefresh = false,
                            coursesRequestType = STALE
                        )
                    }
                    _courseComponent.postEvent(courseComponent)
                }
            }.onFailure {
                if (coursesRequestType == LIVE || coursesRequestType == STALE) {
                    _handleError.postValue(it)
                    _swipeRefresh.postValue(false)
                    _showProgress.postValue(false)
                } else {
                    // Unable to get Course data neither from app session cache nor available in persistable cache
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

    private suspend fun getCourseComponentResults(
        courseId: String,
        courseComponentId: String?,
        coursesRequestType: CoursesRequestType
    ): kotlin.Result<CourseComponent?> = when (coursesRequestType) {
        APP_LEVEL_CACHE -> {
            runCatching {
                courseRepository.getCourseComponentsFromAppLevelCache(
                    courseId,
                    courseComponentId
                )
            }
        }

        PERSISTABLE_CACHE -> {
            runCatching {
                courseRepository.getCourseDataFromPersistableCache(courseId)
            }
        }

        STALE, LIVE -> {
            runCatching {
                courseRepository.getCourseStructure(
                    courseId,
                    coursesRequestType
                )
            }
        }

        else -> {
            throw Exception("Unknown Request Type: $coursesRequestType")
        }
    }

    fun getCourseStatusInfo(courseId: String) {
        viewModelScope.launch {
            val courseStatusResult = runCatching {
                courseRepository.getCourseStatusInfo(courseId)
            }
            courseStatusResult.onSuccess {
                it?.let {
                    _lastAccessedComponent.postEvent(it)
                }
            }.onFailure {
                // nothing to do
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        courseRequestType = CoursesRequestType.NONE
    }

    @Suppress("ClassName")
    sealed class CoursesRequestType {
        object LIVE : CoursesRequestType()
        object STALE : CoursesRequestType()
        object PERSISTABLE_CACHE : CoursesRequestType() // represents max-stale with only-if-cached
        object APP_LEVEL_CACHE : CoursesRequestType() // represents the LruCache from CourseManager
        object NONE : CoursesRequestType()
    }
}

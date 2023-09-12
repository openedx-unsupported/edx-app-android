package org.edx.mobile.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import org.edx.mobile.base.http.HttpBaseTestCase
import org.edx.mobile.repository.CourseDatesRepository
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Ref: https://medium.com/@shehzab.developer/test-driven-development-in-android-mvvm-architecture-part-1-basic-introduction-cd887031a9f6
 */
@RunWith(AndroidJUnit4::class)
class CourseDateViewModelTest : HttpBaseTestCase() {

    private lateinit var courseDateViewModel: CourseDateViewModel

    @get:Rule
    var instantTaskExecuteRule = InstantTaskExecutorRule()

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        courseDateViewModel = CourseDateViewModel(CourseDatesRepository(courseAPI), Dispatchers.IO)
    }

    @Test
    fun startViewModel() {
        courseDateViewModel.fetchCourseDates(
            courseId = "",
            isSwipeRefresh = false,
            forceRefresh = false,
            showLoader = false
        )
        assertNotNull(courseDateViewModel.courseDates.getOrAwaitValue())
    }
}

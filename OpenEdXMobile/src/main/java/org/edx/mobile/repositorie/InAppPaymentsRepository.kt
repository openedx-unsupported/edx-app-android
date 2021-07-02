package org.edx.mobile.repositorie

import android.content.Context
import com.google.inject.Inject
import com.google.inject.Singleton
import org.edx.mobile.course.CourseAPI

@Singleton
class InAppPaymentsRepository @Inject constructor(
    context: Context,
    courseApi: CourseAPI
) {

    companion object {
        private val TAG = InAppPaymentsRepository::class.java.simpleName
    }
}
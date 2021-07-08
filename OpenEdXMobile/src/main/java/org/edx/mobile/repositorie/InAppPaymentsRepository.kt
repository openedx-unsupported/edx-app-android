package org.edx.mobile.repositorie

import android.content.Context
import com.google.inject.Inject
import com.google.inject.Singleton
import org.edx.mobile.course.CourseAPI
import org.edx.mobile.http.callback.Callback
import org.edx.mobile.model.iap.BasketResponse

@Singleton
class InAppPaymentsRepository @Inject constructor(
    val context: Context,
    val courseApi: CourseAPI
) {

    fun addToBasket(sku: String, callback: Callback<BasketResponse>) {
        courseApi.
        addToBasket(sku).enqueue(callback)
    }
}
package org.edx.mobile.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsResponseListener
import com.google.inject.Inject
import org.edx.mobile.repositorie.InAppPaymentsRepository

class InAppPurchaseViewModel @Inject constructor(
) :ViewModel() {
    companion object {
        private val TAG = InAppPurchaseViewModel::class.java.simpleName

        @Inject
        lateinit var inAppPaymentsRepository: InAppPaymentsRepository
    }
}
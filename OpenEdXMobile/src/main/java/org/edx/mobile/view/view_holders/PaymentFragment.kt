package org.edx.mobile.view.view_holders

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.android.billingclient.api.*
import com.google.inject.Inject
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragment
import org.edx.mobile.course.CourseAPI
import org.edx.mobile.databinding.FragmentPaymentsBinding
import org.edx.mobile.logger.Logger
import org.edx.mobile.model.iap.BasketResponse
import org.edx.mobile.model.iap.CheckoutResponse
import org.edx.mobile.repositorie.InAppPaymentsRepository
import org.edx.mobile.viewModel.InAppPurchaseViewModel

class PaymentFragment : BaseFragment(), PurchasesUpdatedListener {

    private lateinit var iapViewModel: InAppPurchaseViewModel
    private lateinit var billingClient: BillingClient
    private lateinit var binding: FragmentPaymentsBinding
    private val TAG = PaymentFragment::class.java.simpleName
    private val logger = Logger(TAG)

    @Inject
    private lateinit var courseAPI: CourseAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iapViewModel = InAppPurchaseViewModel(InAppPaymentsRepository(requireContext(), courseAPI))
        billingClient = BillingClient.newBuilder(contextOrThrow)
            .setListener(this)
            .enablePendingPurchases()
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_payments, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        billingClientConnection()
        binding.btnBuy.setOnClickListener {
            if (billingClient.isReady) {

                addToBasket("https://ecommerce-iap.sandbox.edx.org/api/iap/v1/basket/add/?sku=A5B6DBE")
//                getSkuDetails()
            }
        }
    }

    private fun initObserver() {
        iapViewModel.basketResponse.observe(viewLifecycleOwner, object : Observer<BasketResponse> {
            override fun onChanged(basketResponse: BasketResponse) {
                logger.debug(basketResponse.toString())
                binding.tvMessage.text = basketResponse.success
                binding.tvMessage.text =
                    binding.tvMessage.text as String + " -> " + basketResponse.basketId
                iapViewModel.checkout(basketResponse.basketId)
            }
        })

        iapViewModel.checkoutResponse.observe(viewLifecycleOwner, object : Observer<CheckoutResponse>{
            override fun onChanged(checkoutResponse: CheckoutResponse?) {
                binding.tvMessage.text =
                    binding.tvMessage.text as String + " -> " + checkoutResponse.
            }

        })
    }

    private fun addToBasket(url: String) {
        iapViewModel.addToBasket(url)
    }

    private fun getSkuDetails() {
        billingClient.querySkuDetailsAsync(
            SkuDetailsParams.newBuilder()
                .setType(BillingClient.SkuType.INAPP)
                // testing key
                .setSkusList(listOf("android.test.purchased"))
                .build()
        ) { billingResult, skuDetailsList ->
            Log.d(
                TAG,
                "Problem getting purchases: $billingResult"
            )
            if (skuDetailsList != null) {
                launchBillingFlow(skuDetailsList[0])
            }
        }
    }

    private fun launchBillingFlow(skuDetail: SkuDetails) {
        val billingFlowParamsBuilder = BillingFlowParams.newBuilder()
        billingFlowParamsBuilder.setSkuDetails(skuDetail)
        billingClient.launchBillingFlow(
            requireActivity(), billingFlowParamsBuilder.build()
        )
    }

    private fun billingClientConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.d(
                    TAG,
                    "Problem getting purchases: $billingResult" + billingResult.debugMessage
                )
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Toast.makeText(
                        context,
                        "The BillingClient is ready. You can query purchases here.",
                        Toast.LENGTH_LONG
                    ).show()
                    queryInAppPurchaes();
                }
            }

            override fun onBillingServiceDisconnected() {
                Toast.makeText(
                    context,
                    "Try to restart the connection on the next request to\n" +
                            "Google Play by calling the startConnection() method.",
                    Toast.LENGTH_LONG
                ).show()
                Log.d(
                    TAG,
                    "Problem getting purchases: " + "onBillingServiceDisconnected"
                )

            }
        })
    }

    private fun queryInAppPurchaes() {
        billingClient.queryPurchasesAsync(
            BillingClient.SkuType.INAPP,
            object : PurchasesResponseListener {
                override fun onQueryPurchasesResponse(
                    billingResult: BillingResult,
                    list: MutableList<Purchase>
                ) {
                    Log.d(
                        TAG,
                        "Problem getting purchases: " + billingResult.debugMessage
                    )
                }

            })
        billingClient.queryPurchasesAsync(
            BillingClient.SkuType.INAPP
        ) { billingResult: BillingResult, list: List<Purchase?>? ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.d(
                    TAG,
                    "Problem getting purchases: " + billingResult.debugMessage
                )
            } else {
                Log.d(
                    TAG,
                    list.toString() + billingResult.debugMessage
                )
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        Log.d(
            TAG,
            billingResult.toString() + "->" + purchases.toString()
        )
        if (purchases != null && !purchases[0].isAcknowledged) {
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchases[0].purchaseToken)
                    .build()
            ) { billingResult: BillingResult ->
                if (billingResult.responseCode
                    == BillingClient.BillingResponseCode.OK
                ) {
                    Log.d(
                        TAG,
                        billingResult.debugMessage
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        billingClient.endConnection()
    }
}

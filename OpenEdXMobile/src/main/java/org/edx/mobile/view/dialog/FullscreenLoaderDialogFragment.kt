package org.edx.mobile.view.dialog

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.DialogFullscreenLoaderBinding
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.module.analytics.InAppPurchasesAnalytics
import org.edx.mobile.util.InAppPurchasesException
import org.edx.mobile.util.NonNullObserver
import org.edx.mobile.viewModel.InAppPurchasesViewModel
import org.edx.mobile.wrapper.InAppPurchasesDialog
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class FullscreenLoaderDialogFragment : DialogFragment() {

    @Inject
    lateinit var environment: IEdxEnvironment

    @Inject
    lateinit var iapAnalytics: InAppPurchasesAnalytics

    @Inject
    lateinit var iapDialog: InAppPurchasesDialog

    private lateinit var binding: DialogFullscreenLoaderBinding

    private val iapViewModel: InAppPurchasesViewModel
            by viewModels(ownerProducer = { requireActivity() })

    private var loaderStartTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.AppTheme_NoActionBar
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogFullscreenLoaderBinding.inflate(inflater)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, args: Bundle?) {
        super.onViewCreated(view, args)
        loaderStartTime = args?.getLong(LOADER_START_TIME, Calendar.getInstance().timeInMillis)
            ?: Calendar.getInstance().timeInMillis
        intiViews()
        initObservers()
        if (iapViewModel.upgradeMode.isSilentMode()) {
            iapViewModel.refreshCourseData(true)
        } else if (iapViewModel.isVerificationPending) {
            iapViewModel.executeOrder(requireActivity())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(LOADER_START_TIME, loaderStartTime)
    }

    private fun intiViews() {
        binding.materialTextView.setText(getTitle(), TextView.BufferType.SPANNABLE)
    }

    private fun initObservers() {
        iapViewModel.errorMessage.observe(viewLifecycleOwner, NonNullObserver { errorMsg ->
            errorMsg.throwable as InAppPurchasesException
            iapDialog.handleIAPException(
                fragment = this@FullscreenLoaderDialogFragment,
                errorMessage = errorMsg,
                retryListener = { _, _ ->
                    if (errorMsg.requestType == ErrorMessage.EXECUTE_ORDER_CODE) {
                        iapViewModel.executeOrder(requireActivity())
                    } else {
                        iapViewModel.refreshCourseData(true)
                    }
                },
                cancelListener = { _, _ ->
                    resetPurchase()
                })
            iapViewModel.errorMessageShown()
        })
    }

    private fun getTitle(): SpannableStringBuilder {
        val unlocking = getString(R.string.fullscreen_loader_unlocking)
        val fullAccess = getString(R.string.fullscreen_loader_full_access)
        val toYourCourse = getString(R.string.fullscreen_loader_to_your_course)

        val spannable =
            SpannableStringBuilder(String.format("%s\n%s\n%s", unlocking, fullAccess, toYourCourse))

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.accentAColor)),
            unlocking.length,
            unlocking.length + fullAccess.length + 1,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        return spannable
    }

    /**
     * Method to get the remaining visible time for the loader
     * As per requirements the loader needs to be visible for at least 3 seconds
     */
    fun getRemainingVisibleTime(): Long {
        val totalVisibleTime = Calendar.getInstance().timeInMillis - loaderStartTime
        return if (totalVisibleTime < MINIMUM_DISPLAY_DELAY)
            MINIMUM_DISPLAY_DELAY - totalVisibleTime
        else
            0
    }

    private fun resetPurchase() {
        iapViewModel.resetPurchase(false)
        dismiss()
    }

    companion object {
        const val TAG = "FULLSCREEN_LOADER"
        private const val LOADER_START_TIME = "LOADER_START_TIME"
        const val MINIMUM_DISPLAY_DELAY: Long = 3_000

        @JvmStatic
        fun newInstance(): FullscreenLoaderDialogFragment {
            return FullscreenLoaderDialogFragment()
        }
    }
}

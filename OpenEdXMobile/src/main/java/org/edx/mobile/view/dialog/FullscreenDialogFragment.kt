package org.edx.mobile.view.dialog

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.DialogFullscreenLoaderBinding
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.util.InAppPurchasesException
import org.edx.mobile.util.NonNullObserver
import org.edx.mobile.viewModel.InAppPurchasesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FullscreenLoaderDialogFragment : DialogFragment() {

    @Inject
    lateinit var environment: IEdxEnvironment

    private lateinit var binding: DialogFullscreenLoaderBinding

    private val iapViewModel: InAppPurchasesViewModel
            by viewModels(ownerProducer = { requireActivity() })

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
        intiViews()
        initObservers()
        if (iapViewModel.isVerificationPending) {
            iapViewModel.executeOrder()
        }
    }

    private fun intiViews() {
        binding.materialTextView.setText(getTitle(), TextView.BufferType.SPANNABLE)
    }

    private fun initObservers() {
        iapViewModel.errorMessage.observe(viewLifecycleOwner, NonNullObserver { errorMsg ->
            if (errorMsg.throwable is InAppPurchasesException) {
                when (errorMsg.throwable.httpErrorCode) {
                    HttpStatus.UNAUTHORIZED -> {
                        environment.router?.forceLogout(
                            requireContext(),
                            environment.analyticsRegistry,
                            environment.notificationDelegate
                        )
                        return@NonNullObserver
                    }
                    else -> showUpgradeErrorDialog(errorMsg.errorResId)
                }
            } else {
                showUpgradeErrorDialog(errorMsg.errorResId)
            }
            iapViewModel.errorMessageShown()
        })
    }

    private fun showUpgradeErrorDialog(
        @StringRes errorResId: Int = R.string.general_error_message
    ) {
        AlertDialogFragment.newInstance(
            getString(R.string.title_upgrade_error),
            getString(errorResId),
            getString(R.string.label_close),
            { _, _ -> resetPurchase() },
            getString(R.string.label_get_help),
            { _, _ ->
                environment.router?.showFeedbackScreen(
                    requireActivity(),
                    getString(R.string.email_subject_upgrade_error)
                )
                resetPurchase()
            }).show(childFragmentManager, null)
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

    private fun resetPurchase() {
        iapViewModel.resetPurchase(false)
        dismiss()
    }

    companion object {
        const val TAG = "FULLSCREEN_LOADER"
        const val FULLSCREEN_DISPLAY_DELAY: Long = 3_000

        @JvmStatic
        fun newInstance(): FullscreenLoaderDialogFragment {
            return FullscreenLoaderDialogFragment()
        }
    }
}

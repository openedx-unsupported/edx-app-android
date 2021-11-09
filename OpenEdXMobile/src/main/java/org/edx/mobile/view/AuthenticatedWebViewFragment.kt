package org.edx.mobile.view

import android.os.Bundle
import android.text.TextUtils
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragment
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.ContentErrorBinding
import org.edx.mobile.databinding.FragmentAuthenticatedWebviewBinding
import org.edx.mobile.deeplink.Screen
import org.edx.mobile.logger.Logger
import org.edx.mobile.util.AgreementUrlType
import org.edx.mobile.util.ConfigUtil
import org.edx.mobile.util.UiUtils
import javax.inject.Inject

/**
 * Provides a webview which authenticates the user before loading a page,
 * Javascript can also be passed in arguments for evaluation.
 */
open class AuthenticatedWebViewFragment : BaseFragment() {

    @Inject
    private lateinit var environment: IEdxEnvironment

    protected val logger = Logger(javaClass.name)

    private var authWebViewBinding: FragmentAuthenticatedWebviewBinding? = null
    private var contentErrorBinding: ContentErrorBinding? = null

    /**
     * @return `true` if the system is updating the WebView package, `false`
     * otherwise.
     */
    var isSystemUpdatingWebView = false
        private set

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /*
         * In some cases like system updating the WebView, it is not available for rendering and the
         * system raises an InflateException. To handle this case we're showing a full-screen error
         * with reload button.
         * More info on JIRA story: LEARNER-7267
         * */
        return try {
            isSystemUpdatingWebView = false
            authWebViewBinding =
                FragmentAuthenticatedWebviewBinding.inflate(inflater, container, false)
            authWebViewBinding?.root
        } catch (e: InflateException) {
            logger.error(e, true)
            isSystemUpdatingWebView = true
            contentErrorBinding = ContentErrorBinding.inflate(inflater, container, false)
            contentErrorBinding?.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isSystemUpdatingWebView) {
            // Disable the SwipeRefreshLayout by-default to allow the subclasses to provide a proper implementation for it
            authWebViewBinding?.swipeContainer?.isEnabled = false
        } else {
            view.findViewById<View>(R.id.content_error).visibility = View.VISIBLE
            val ivContentError: AppCompatImageView = view.findViewById(R.id.content_error_icon)
            ivContentError.setImageDrawable(
                UiUtils.getDrawable(requireContext(), R.drawable.ic_error)
            )
            contentErrorBinding?.run {
                contentErrorText.text = getString(R.string.error_unknown)
                contentErrorAction.visibility = View.VISIBLE
                contentErrorAction.setText(R.string.lbl_reload)
                contentErrorAction.setOnClickListener {
                    UiUtils.restartFragment(
                        this@AuthenticatedWebViewFragment
                    )
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (!isSystemUpdatingWebView) {
            arguments?.run {
                val url = getString(ARG_URL)
                val javascript = getString(ARG_JAVASCRIPT)
                val isManuallyReloadable = getBoolean(ARG_IS_MANUALLY_RELOADABLE)
                authWebViewBinding?.authWebview?.initWebView(
                    requireActivity(),
                    false,
                    isManuallyReloadable,
                    false,
                    null,
                    { canDismiss, screenName ->
                        handleScreenNavigation(screenName)
                        if (canDismiss) {
                            activity?.finish()
                        }
                    }
                )
                authWebViewBinding?.authWebview?.loadUrlWithJavascript(true, url, javascript)
            }
        }
    }

    private fun handleScreenNavigation(screenName: String?) {
        when (screenName) {
            Screen.DELETE_ACCOUNT -> {
                environment.router.showAuthenticatedWebViewActivity(
                    requireActivity(),
                    environment.config.deleteAccountUrl,
                    getString(R.string.title_delete_my_account), false
                )
            }
            Screen.TERMS_OF_SERVICE -> {
                ConfigUtil.getAgreementUrl(
                    requireContext(),
                    environment.config.agreementUrlsConfig,
                    AgreementUrlType.TOS
                )?.let { url ->
                    environment.router.showWebViewActivity(
                        requireActivity(),
                        url,
                        getString(R.string.terms_of_service_title)
                    )
                }
            }
            Screen.PRIVACY_POLICY -> {
                ConfigUtil.getAgreementUrl(
                    requireContext(),
                    environment.config.agreementUrlsConfig,
                    AgreementUrlType.PRIVACY_POLICY
                )?.let { url ->
                    environment.router.showWebViewActivity(
                        requireActivity(),
                        url,
                        getString(R.string.privacy_policy_title)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isSystemUpdatingWebView) {
            authWebViewBinding?.authWebview?.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isSystemUpdatingWebView) {
            authWebViewBinding?.authWebview?.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isSystemUpdatingWebView) {
            authWebViewBinding?.authWebview?.onDestroy()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!isSystemUpdatingWebView) {
            authWebViewBinding?.authWebview?.onDestroyView()
        }
    }

    fun getBinding(): FragmentAuthenticatedWebviewBinding {
        return authWebViewBinding
            ?: throw IllegalStateException("FragmentAuthenticatedWebview is not attached to an fragment.")
    }

    companion object {
        const val ARG_URL = "ARG_URL"
        const val ARG_JAVASCRIPT = "ARG_JAVASCRIPT"
        const val ARG_IS_MANUALLY_RELOADABLE = "ARG_IS_MANUALLY_RELOADABLE"

        @JvmStatic
        fun makeArguments(url: String, javascript: String?, isManuallyReloadable: Boolean): Bundle {
            val args = Bundle()
            args.putString(ARG_URL, url)
            args.putBoolean(ARG_IS_MANUALLY_RELOADABLE, isManuallyReloadable)
            if (!TextUtils.isEmpty(javascript)) {
                args.putString(ARG_JAVASCRIPT, javascript)
            }
            return args
        }

        @JvmOverloads
        fun newInstance(url: String, javascript: String? = null): Fragment {
            val fragment: Fragment = AuthenticatedWebViewFragment()
            fragment.arguments = makeArguments(url, javascript, false)
            return fragment
        }

        fun newInstance(url: String, javascript: String?, isManuallyReloadable: Boolean): Fragment {
            val fragment: Fragment = AuthenticatedWebViewFragment()
            fragment.arguments = makeArguments(url, javascript, isManuallyReloadable)
            return fragment
        }
    }
}

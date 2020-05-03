package org.edx.mobile.view

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.inject.Inject
import org.edx.mobile.BuildConfig
import org.edx.mobile.R
import org.edx.mobile.annotation.Nullable
import org.edx.mobile.base.BaseFragment
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.FragmentAccountBinding
import org.edx.mobile.deeplink.Screen
import org.edx.mobile.deeplink.ScreenDef
import org.edx.mobile.module.prefs.LoginPrefs
import org.edx.mobile.util.Config

class AccountFragment : BaseFragment() {

    private lateinit var binding: FragmentAccountBinding
    @Inject
    private val config: Config? = null
    @Inject
    private val environment: IEdxEnvironment? = null
    @Inject
    private val loginPrefs: LoginPrefs? = null

    companion object {
        @JvmStatic
        fun newInstance(@Nullable bundle: Bundle?): AccountFragment {
            val fragment = AccountFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false)
        if (config?.isUserProfilesEnabled == true) {
            binding.profileBtn.setOnClickListener {
                activity?.let { activity ->
                    environment?.router?.showUserProfile(activity, loginPrefs?.username ?: "")
                }
            }
        } else {
            binding.profileBtn.visibility = View.GONE
        }
        binding.settingsBtn.setOnClickListener { environment?.router?.showSettings(activity) }
        binding.feedbackBtn.setOnClickListener {
            activity?.let { activity ->
                environment?.router?.showFeedbackScreen(activity, getString(R.string.email_subject))
            }
        }
        binding.logoutBtn.setOnClickListener {
            environment?.router?.performManualLogout(context,
                    environment.analyticsRegistry, environment.notificationDelegate)
        }
        binding.tvVersionNo.text = String.format("%s %s %s", getString(R.string.label_version),
                BuildConfig.VERSION_NAME, environment?.config?.environmentDisplayName)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleIntentBundle(arguments)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntentBundle(intent.extras)
    }

    private fun handleIntentBundle(bundle: Bundle?) {
        if (bundle != null) {
            @ScreenDef val screenName = bundle.getString(Router.EXTRA_SCREEN_NAME)
            if (!TextUtils.isEmpty(screenName) && screenName == Screen.SETTINGS) {
                environment?.router?.showSettings(activity)
            }
        }
    }
}

package org.edx.mobile.view

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.BuildConfig
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragment
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.FragmentAccountBinding
import org.edx.mobile.deeplink.Screen
import org.edx.mobile.deeplink.ScreenDef
import org.edx.mobile.event.AccountDataLoadedEvent
import org.edx.mobile.event.MediaStatusChangeEvent
import org.edx.mobile.event.ProfilePhotoUpdatedEvent
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.model.user.Account
import org.edx.mobile.model.video.VideoQuality
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.prefs.LoginPrefs
import org.edx.mobile.module.prefs.PrefManager
import org.edx.mobile.user.UserAPI.AccountDataUpdatedCallback
import org.edx.mobile.user.UserService
import org.edx.mobile.util.*
import org.edx.mobile.view.dialog.IDialogCallback
import org.edx.mobile.view.dialog.NetworkCheckDialogFragment
import org.edx.mobile.view.dialog.VideoDownloadQualityDialogFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import retrofit2.Call
import javax.inject.Inject

@AndroidEntryPoint
class AccountFragment : BaseFragment() {

    private lateinit var binding: FragmentAccountBinding

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var environment: IEdxEnvironment

    @Inject
    lateinit var loginPrefs: LoginPrefs

    @Inject
    lateinit var userService: UserService

    private var getAccountCall: Call<Account>? = null

    companion object {
        @JvmStatic
        fun newInstance(@Nullable bundle: Bundle?): AccountFragment {
            val fragment = AccountFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        environment.analyticsRegistry.trackScreenView(Analytics.Screens.PROFILE)
        EventBus.getDefault().register(this)
        sendGetUpdatedAccountCall()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false)
        return binding.root
    }

    private fun handleIntentBundle(bundle: Bundle?) {
        if (bundle != null) {
            @ScreenDef val screenName = bundle.getString(Router.EXTRA_SCREEN_NAME)
            val username = loginPrefs.username
            if (!screenName.isNullOrBlank() && !username.isNullOrBlank() && screenName == Screen.USER_PROFILE) {
                environment.router.showUserProfile(requireContext(), username)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPersonalInfo()
        handleIntentBundle(arguments)
        initVideoQuality()
        updateWifiSwitch()
        updateSDCardSwitch()
        initHelpFields()

        binding.containerPurchases.setVisibility(environment.appFeaturesPrefs.isIAPEnabled())
        if (!loginPrefs.username.isNullOrBlank()) {
            binding.btnSignOut.visibility = View.VISIBLE
            binding.btnSignOut.setOnClickListener {
                environment.router.performManualLogout(
                    context,
                    environment.analyticsRegistry, environment.notificationDelegate
                )
            }
        }

        binding.appVersion.text = String.format(
            "%s %s %s", getString(R.string.label_app_version),
            BuildConfig.VERSION_NAME, config.environmentDisplayName
        )

        config.deleteAccountUrl?.let { deleteAccountUrl ->
            binding.containerDeleteAccount.visibility = View.VISIBLE
            binding.btnDeleteAccount.setOnClickListener {
                environment.router.showAuthenticatedWebViewActivity(
                    this.requireContext(),
                    deleteAccountUrl, getString(R.string.title_delete_my_account), false
                )
                trackEvent(
                    Analytics.Events.DELETE_ACCOUNT_CLICKED,
                    Analytics.Values.DELETE_ACCOUNT_CLICKED
                )
            }
        }

        environment.analyticsRegistry.trackScreenViewEvent(
            Analytics.Events.PROFILE_PAGE_VIEWED,
            Analytics.Screens.PROFILE
        )
    }

    private fun initVideoQuality() {
        binding.containerVideoQuality.setOnClickListener {
            val videoQualityDialog: VideoDownloadQualityDialogFragment =
                VideoDownloadQualityDialogFragment.getInstance(environment,
                    callback = object : VideoDownloadQualityDialogFragment.IListDialogCallback {
                        override fun onItemClicked(videoQuality: VideoQuality) {
                            setVideoQualityDescription(videoQuality)
                        }
                    })
            videoQualityDialog.show(
                childFragmentManager,
                VideoDownloadQualityDialogFragment.TAG
            )

            trackEvent(
                Analytics.Events.PROFILE_VIDEO_DOWNLOAD_QUALITY_CLICKED,
                Analytics.Values.PROFILE_VIDEO_DOWNLOAD_QUALITY_CLICKED
            )
        }
    }

    private fun setVideoQualityDescription(videoQuality: VideoQuality) {
        binding.tvVideoDownloadQuality.setText(videoQuality.titleResId)
    }

    private fun initHelpFields() {
        if (!config.feedbackEmailAddress.isNullOrBlank() || !config.faqUrl.isNullOrBlank()) {
            binding.tvHelp.visibility = View.VISIBLE
            if (!config.feedbackEmailAddress.isNullOrBlank()) {
                binding.containerFeedback.visibility = View.VISIBLE
                binding.btnEmailSupport.setOnClickListener {
                    environment.router.showFeedbackScreen(
                        requireActivity(),
                        getString(R.string.email_subject)
                    )
                    trackEvent(
                        Analytics.Events.EMAIL_SUPPORT_CLICKED,
                        Analytics.Values.EMAIL_SUPPORT_CLICKED
                    )
                }
            }

            if (!config.faqUrl.isNullOrBlank()) {
                binding.containerFaq.visibility = View.VISIBLE
                binding.tvGetSupportDescription.text = ResourceUtil.getFormattedString(
                    resources, R.string.description_get_support,
                    AppConstants.PLATFORM_NAME, config.platformName
                ).toString()
                binding.btnFaq.setOnClickListener {
                    BrowserUtil.open(requireActivity(), environment.config.faqUrl, false)
                    trackEvent(Analytics.Events.FAQ_CLICKED, Analytics.Values.FAQ_CLICKED)
                }
            }
        }
    }

    private fun sendGetUpdatedAccountCall() {
        loginPrefs.username?.let { username ->
            getAccountCall = userService.getAccount(username)
            getAccountCall?.enqueue(
                AccountDataUpdatedCallback(
                    requireContext(),
                    username,
                    null,  // Disable global loading indicator
                    null
                )
            ) // No place to show an error notification
        }
    }

    private fun initPersonalInfo() {
        if (!config.isUserProfilesEnabled) {
            binding.containerPersonalInfo.visibility = View.GONE
            return
        }
        loginPrefs.let { prefs ->
            prefs.currentUserProfile?.let { profileModel ->
                if (!profileModel.email.isNullOrEmpty()) {
                    binding.tvEmail.text = ResourceUtil.getFormattedString(
                        resources,
                        R.string.profile_email_description,
                        AppConstants.EMAIL,
                        profileModel.email
                    )
                } else {
                    binding.tvEmail.visibility = View.GONE
                }

                if (!profileModel.username.isNullOrEmpty()) {
                    binding.tvUsername.text = ResourceUtil.getFormattedString(
                        resources,
                        R.string.profile_username_description,
                        AppConstants.USERNAME,
                        profileModel.username
                    )
                } else {
                    binding.tvUsername.visibility = View.GONE
                }

                binding.tvLimitedProfile.visibility =
                    if (profileModel.hasLimitedProfile) View.VISIBLE else View.GONE

                prefs.profileImage?.let { imageUrl ->
                    Glide.with(requireContext())
                        .load(imageUrl.imageUrlMedium)
                        .into(binding.profileImage)
                }
                    ?: run { binding.profileImage.setImageResource(R.drawable.profile_photo_placeholder) }
            }
            binding.containerPersonalInfo.visibility = View.VISIBLE
            binding.containerPersonalInfo.setOnClickListener {
                trackEvent(
                    Analytics.Events.PERSONAL_INFORMATION_CLICKED,
                    Analytics.Values.PERSONAL_INFORMATION_CLICKED
                )
                environment.router.showUserProfile(requireActivity(), prefs.username ?: "")
            }
            setVideoQualityDescription(prefs.videoQuality)
        }
    }

    private fun updateWifiSwitch() {
        val wifiPrefManager = PrefManager(requireContext(), PrefManager.Pref.WIFI)
        binding.switchWifi.setOnCheckedChangeListener(null)
        binding.switchWifi.isChecked =
            wifiPrefManager.getBoolean(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true)
        binding.switchWifi.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true)
                wifiPrefManager.put(PrefManager.Key.DOWNLOAD_OFF_WIFI_SHOW_DIALOG_FLAG, true)
                trackEvent(Analytics.Events.WIFI_ON, Analytics.Values.WIFI_ON)
            } else {
                showWifiDialog()
            }
        }
    }

    private fun showWifiDialog() {
        val dialogFragment =
            NetworkCheckDialogFragment.newInstance(getString(R.string.wifi_dialog_title_help),
                getString(R.string.wifi_dialog_message_help),
                object : IDialogCallback {
                    override fun onPositiveClicked() {
                        try {
                            val wifiPrefManager =
                                PrefManager(requireContext(), PrefManager.Pref.WIFI)
                            wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, false)
                            trackEvent(Analytics.Events.WIFI_ALLOW, Analytics.Values.WIFI_ALLOW)
                            trackEvent(Analytics.Events.WIFI_OFF, Analytics.Values.WIFI_OFF)
                            updateWifiSwitch()
                        } catch (ex: Exception) {
                        }
                    }

                    override fun onNegativeClicked() {
                        try {
                            val wifiPrefManager =
                                PrefManager(requireContext(), PrefManager.Pref.WIFI)
                            wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true)
                            wifiPrefManager.put(
                                PrefManager.Key.DOWNLOAD_OFF_WIFI_SHOW_DIALOG_FLAG,
                                true
                            )
                            trackEvent(
                                Analytics.Events.WIFI_DONT_ALLOW,
                                Analytics.Values.WIFI_DONT_ALLOW
                            )
                            updateWifiSwitch()
                        } catch (ex: Exception) {
                        }
                    }
                })
        dialogFragment.isCancelable = false
        activity?.let { dialogFragment.show(it.supportFragmentManager, AppConstants.DIALOG) }
    }

    private fun updateSDCardSwitch() {
        val prefManager = PrefManager(requireContext(), PrefManager.Pref.USER_PREF)
        if (!environment.config.isDownloadToSDCardEnabled || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            binding.containerSdCard.visibility = View.GONE
            binding.tvDescriptionSdCard.visibility = View.GONE
            prefManager.put(PrefManager.Key.DOWNLOAD_TO_SDCARD, false)
        } else {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this)
            }
            binding.switchSdCard.setOnCheckedChangeListener(null)
            binding.switchSdCard.isChecked = environment.userPrefs.isDownloadToSDCardEnabled
            binding.switchSdCard.setOnCheckedChangeListener { _, isChecked ->
                prefManager.put(PrefManager.Key.DOWNLOAD_TO_SDCARD, isChecked)
                // Send analytics
                if (isChecked) trackEvent(
                    Analytics.Events.DOWNLOAD_TO_SD_CARD_ON,
                    Analytics.Values.DOWNLOAD_TO_SD_CARD_SWITCH_ON
                )
                else trackEvent(
                    Analytics.Events.DOWNLOAD_TO_SD_CARD_OFF,
                    Analytics.Values.DOWNLOAD_TO_SD_CARD_SWITCH_OFF
                )
            }
            binding.switchSdCard.isEnabled = FileUtil.isRemovableStorageAvailable(requireContext())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (null != getAccountCall) {
            getAccountCall?.cancel()
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe(sticky = true)
    @SuppressWarnings("unused")
    fun onEventMainThread(event: MediaStatusChangeEvent) {
        binding.switchSdCard.isEnabled = event.isSdCardAvailable
    }

    @Subscribe(sticky = true)
    @SuppressWarnings("unused")
    fun onEventMainThread(@NonNull event: AccountDataLoadedEvent) {
        if (!environment.config.isUserProfilesEnabled) {
            return
        }
        initPersonalInfo()
    }

    @Subscribe(sticky = true)
    @SuppressWarnings("unused")
    fun onEventMainThread(event: ProfilePhotoUpdatedEvent) {
        UserProfileUtils.loadProfileImage(requireContext(), event, binding.profileImage)
    }

    private fun trackEvent(eventName: String, biValue: String) {
        environment.analyticsRegistry.trackEvent(eventName, biValue)
    }
}

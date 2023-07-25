package org.edx.mobile.view

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.edx.mobile.BuildConfig
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragment
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.FragmentAccountBinding
import org.edx.mobile.deeplink.Screen
import org.edx.mobile.deeplink.ScreenDef
import org.edx.mobile.event.AccountDataLoadedEvent
import org.edx.mobile.event.IAPFlowEvent
import org.edx.mobile.event.MediaStatusChangeEvent
import org.edx.mobile.event.MyCoursesRefreshEvent
import org.edx.mobile.event.ProfilePhotoUpdatedEvent
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.extenstion.CollapsingToolbarStatListener
import org.edx.mobile.extenstion.isVisible
import org.edx.mobile.extenstion.setTitleStateListener
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.model.iap.IAPFlowData
import org.edx.mobile.model.user.Account
import org.edx.mobile.model.video.VideoQuality
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.analytics.InAppPurchasesAnalytics
import org.edx.mobile.module.prefs.LoginPrefs
import org.edx.mobile.module.prefs.UserPrefs
import org.edx.mobile.user.UserAPI.AccountDataUpdatedCallback
import org.edx.mobile.user.UserService
import org.edx.mobile.util.AgreementUrlType
import org.edx.mobile.util.AppConstants
import org.edx.mobile.util.BrowserUtil
import org.edx.mobile.util.Config
import org.edx.mobile.util.ConfigUtil
import org.edx.mobile.util.FileUtil
import org.edx.mobile.util.InAppPurchasesException
import org.edx.mobile.util.NonNullObserver
import org.edx.mobile.util.ResourceUtil
import org.edx.mobile.util.UserProfileUtils
import org.edx.mobile.util.ViewAnimationUtil
import org.edx.mobile.util.observer.EventObserver
import org.edx.mobile.view.dialog.AlertDialogFragment
import org.edx.mobile.view.dialog.FullscreenLoaderDialogFragment
import org.edx.mobile.view.dialog.IDialogCallback
import org.edx.mobile.view.dialog.NetworkCheckDialogFragment
import org.edx.mobile.view.dialog.VideoDownloadQualityDialogFragment
import org.edx.mobile.viewModel.CourseViewModel
import org.edx.mobile.viewModel.InAppPurchasesViewModel
import org.edx.mobile.wrapper.InAppPurchasesDialog
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
    lateinit var userPrefs: UserPrefs

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var iapDialog: InAppPurchasesDialog

    @Inject
    lateinit var iapAnalytics: InAppPurchasesAnalytics

    private val courseViewModel: CourseViewModel by viewModels()
    private val iapViewModel: InAppPurchasesViewModel by viewModels()

    private var getAccountCall: Call<Account>? = null
    private var loaderDialog: AlertDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        sendGetUpdatedAccountCall()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun handleIntentBundle(bundle: Bundle?) {
        if (bundle != null) {
            @ScreenDef val screenName = bundle.getString(Router.EXTRA_SCREEN_NAME)
            if (loginPrefs.isUserLoggedIn && screenName == Screen.USER_PROFILE) {
                environment.router.showUserProfile(requireContext(), loginPrefs.username)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTitle()
        initPersonalInfo()
        initPurchases()
        handleIntentBundle(arguments)
        initVideoQuality()
        updateWifiSwitch()
        updateSDCardSwitch()
        initHelpFields()
        initPrivacyFields()
        initViews()
    }

    private fun initTitle() {
        arguments?.getString(Router.EXTRA_SCREEN_TITLE)?.let {
            binding.toolbar.root.setVisibility(true)
            binding.toolbar.tvTitle.text = it
            binding.toolbar.appbar.setTitleStateListener(
                binding.toolbar.collapsingToolbar,
                object : CollapsingToolbarStatListener {
                    override fun onExpanded() {
                        ViewAnimationUtil.animateTitleSize(
                            binding.toolbar.tvTitle,
                            resources.getDimension(R.dimen.edx_x_large)
                        )
                    }

                    override fun onCollapsed() {
                        ViewAnimationUtil.animateTitleSize(
                            binding.toolbar.tvTitle,
                            resources.getDimension(R.dimen.edx_large)
                        )
                    }
                })
        } ?: run {
            binding.toolbar.root.setVisibility(false)
        }
    }

    private fun initPurchases() {
        val iapEnabled = environment.featuresPrefs.isIAPEnabledForUser(loginPrefs.isOddUserId)
        if (iapEnabled) {
            initRestorePurchasesObservers()
            binding.containerPurchases.setVisibility(true)
            binding.btnRestorePurchases.setOnClickListener {
                iapAnalytics.reset()
                iapAnalytics.trackIAPEvent(Analytics.Events.IAP_RESTORE_PURCHASE_CLICKED)
                showLoader()
                lifecycleScope.launch {
                    courseViewModel.fetchEnrolledCourses(
                        type = CourseViewModel.CoursesRequestType.STALE,
                        showProgress = false
                    )
                }
            }
        } else {
            binding.containerPurchases.setVisibility(false)
        }
    }

    private fun initRestorePurchasesObservers() {
        courseViewModel.enrolledCoursesResponse.observe(
            viewLifecycleOwner,
            EventObserver { enrolledCourses ->
                iapViewModel.detectUnfulfilledPurchase(
                    loginPrefs.userId,
                    enrolledCourses,
                    IAPFlowData.IAPFlowType.RESTORE,
                    Analytics.Screens.PROFILE
                )
            })

        courseViewModel.handleError.observe(viewLifecycleOwner, NonNullObserver {
            loaderDialog?.dismiss()
        })

        iapViewModel.refreshCourseData.observe(viewLifecycleOwner, EventObserver {
            iapDialog.showNewExperienceAlertDialog(this, { _, _ ->
                iapAnalytics.trackIAPEvent(
                    eventName = Analytics.Events.IAP_NEW_EXPERIENCE_ALERT_ACTION,
                    actionTaken = Analytics.Values.ACTION_REFRESH
                )
                loaderDialog?.dismiss()
                iapAnalytics.initUnlockContentTime()
                showFullScreenLoader()
            }, { _, _ ->
                iapAnalytics.trackIAPEvent(
                    eventName = Analytics.Events.IAP_NEW_EXPERIENCE_ALERT_ACTION,
                    actionTaken = Analytics.Values.ACTION_CONTINUE_WITHOUT_UPDATE
                )
                loaderDialog?.dismiss()
            })
        })

        iapViewModel.fakeUnfulfilledCompletion.observe(
            viewLifecycleOwner,
            EventObserver { isCompleted ->
                if (isCompleted) {
                    loaderDialog?.dismiss()
                    iapDialog.showNoUnFulfilledPurchasesDialog(this)
                }
            })

        iapViewModel.errorMessage.observe(viewLifecycleOwner, EventObserver { errorMessage ->
            loaderDialog?.dismiss()
            var retryListener: DialogInterface.OnClickListener? = null
            if (errorMessage.canRetry()) {
                retryListener = DialogInterface.OnClickListener { _, _ ->
                    if (errorMessage.requestType == ErrorMessage.EXECUTE_ORDER_CODE) {
                        iapViewModel.executeOrder()
                    } else if (HttpStatus.NOT_ACCEPTABLE == (errorMessage.throwable as InAppPurchasesException).httpErrorCode) {
                        showFullScreenLoader()
                    }
                }
            }

            var cancelListener: DialogInterface.OnClickListener? = null
            if (errorMessage.isPostUpgradeErrorType()) {
                cancelListener =
                    DialogInterface.OnClickListener { _, _ -> iapViewModel.iapFlowData.clear() }
            }

            iapDialog.handleIAPException(
                fragment = this@AccountFragment,
                errorMessage = errorMessage,
                retryListener = retryListener,
                cancelListener = cancelListener
            )
        })
    }

    private fun showFullScreenLoader() {
        // To proceed with the same instance of dialog fragment in case of orientation change
        var fullScreenLoader =
            FullscreenLoaderDialogFragment.getRetainedInstance(fragmentManager = childFragmentManager)
        if (fullScreenLoader == null) {
            fullScreenLoader =
                FullscreenLoaderDialogFragment.newInstance(iapData = iapViewModel.iapFlowData)
        }
        fullScreenLoader.show(childFragmentManager, FullscreenLoaderDialogFragment.TAG)
    }

    private fun showLoader() {
        loaderDialog = AlertDialogFragment.newInstance(
            R.string.title_checking_purchases,
            R.layout.alert_dialog_progress
        )
        loaderDialog?.isCancelable = false
        loaderDialog?.showNow(childFragmentManager, null)
    }

    private fun initVideoQuality() {
        binding.containerVideoQuality.setOnClickListener {
            val videoQualityDialog: VideoDownloadQualityDialogFragment =
                VideoDownloadQualityDialogFragment.getInstance(
                    environment,
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
        loginPrefs.username.let { username ->
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
        if (!config.isUserProfilesEnabled || !loginPrefs.isUserLoggedIn) {
            binding.containerPersonalInfo.visibility = View.GONE
            return
        }

        binding.tvEmail.setVisibility(loginPrefs.userEmail.isNullOrEmpty().not())
        binding.tvEmail.text = ResourceUtil.getFormattedString(
            resources,
            R.string.profile_email_description,
            AppConstants.EMAIL,
            loginPrefs.userEmail
        )

        binding.tvUsername.setVisibility(loginPrefs.username.isNotEmpty())
        binding.tvUsername.text = ResourceUtil.getFormattedString(
            resources,
            R.string.profile_username_description,
            AppConstants.USERNAME,
            loginPrefs.username
        )

        binding.tvLimitedProfile.setVisibility(loginPrefs.currentUserProfile.hasLimitedProfile)

        loginPrefs.profileImage?.let { imageUrl ->
            Glide.with(requireContext())
                .load(imageUrl.imageUrlMedium)
                .into(binding.profileImage)
        } ?: run { binding.profileImage.setImageResource(R.drawable.profile_photo_placeholder) }

        binding.containerPersonalInfo.visibility = View.VISIBLE
        binding.containerPersonalInfo.setOnClickListener {
            trackEvent(
                Analytics.Events.PERSONAL_INFORMATION_CLICKED,
                Analytics.Values.PERSONAL_INFORMATION_CLICKED
            )
            environment.router.showUserProfileEditor(requireActivity(), loginPrefs.username)
        }
        setVideoQualityDescription(userPrefs.videoQuality)
    }

    private fun initPrivacyFields() {
        ConfigUtil.getAgreementUrl(
            this.requireContext(),
            config.agreementUrlsConfig,
            AgreementUrlType.PRIVACY_POLICY
        )?.let { privacyPolicyUrl ->
            binding.tvPrivacyPolicy.setVisibility(true)
            binding.tvPrivacyPolicy.setOnClickListener {
                environment.router.showAuthenticatedWebViewActivity(
                    this.requireContext(),
                    privacyPolicyUrl,
                    getString(R.string.label_privacy_policy),
                    false
                )
                trackEvent(
                    Analytics.Events.PRIVACY_POLICY_CLICKED,
                    Analytics.Values.PRIVACY_POLICY_CLICKED
                )
            }
        }

        ConfigUtil.getAgreementUrl(
            this.requireContext(),
            config.agreementUrlsConfig,
            AgreementUrlType.COOKIE_POLICY
        )?.let { cookiePolicyUrl ->
            binding.tvCookiePolicy.setVisibility(true)
            binding.tvCookiePolicy.setOnClickListener {
                environment.router.showAuthenticatedWebViewActivity(
                    this.requireContext(),
                    cookiePolicyUrl,
                    getString(R.string.label_cookie_policy),
                    false
                )
                trackEvent(
                    Analytics.Events.COOKIE_POLICY_CLICKED,
                    Analytics.Values.COOKIE_POLICY_CLICKED
                )
            }
        }

        ConfigUtil.getAgreementUrl(
            this.requireContext(),
            config.agreementUrlsConfig,
            AgreementUrlType.DATA_CONSENT
        )?.let { dataConsentUrl ->
            binding.tvDataConsentPolicy.setVisibility(true)
            binding.tvDataConsentPolicy.setOnClickListener {
                environment.router.showAuthenticatedWebViewActivity(
                    this.requireContext(),
                    dataConsentUrl,
                    getString(R.string.label_do_not_sell_my_personal_data),
                    false
                )
                trackEvent(
                    Analytics.Events.DO_NOT_SELL_DATA_CLICKED,
                    Analytics.Values.DO_NOT_SELL_DATA_CLICKED
                )
            }
        }

        val isContainerVisible = binding.tvPrivacyPolicy.isVisible()
                || binding.tvCookiePolicy.isVisible() || binding.tvDataConsentPolicy.isVisible()
        binding.containerPrivacy.setVisibility(isContainerVisible)
    }

    private fun initViews() {
        if (loginPrefs.isUserLoggedIn) {
            binding.btnSignOut.visibility = View.VISIBLE
            binding.btnSignOut.setOnClickListener {
                environment.router.performManualLogout(
                    context,
                    environment.analyticsRegistry, environment.notificationDelegate
                )
            }

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
        }

        binding.appVersion.text = String.format(
            "%s %s %s", getString(R.string.label_app_version),
            BuildConfig.VERSION_NAME, config.environmentDisplayName
        )
    }

    private fun updateWifiSwitch() {
        binding.switchWifi.setOnCheckedChangeListener(null)
        binding.switchWifi.isChecked = userPrefs.isDownloadOverWifiOnly
        binding.switchWifi.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                userPrefs.isDownloadOverWifiOnly = true
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
                        userPrefs.isDownloadOverWifiOnly = false
                        trackEvent(Analytics.Events.WIFI_ALLOW, Analytics.Values.WIFI_ALLOW)
                        trackEvent(Analytics.Events.WIFI_OFF, Analytics.Values.WIFI_OFF)
                        updateWifiSwitch()
                    }

                    override fun onNegativeClicked() {
                        userPrefs.isDownloadOverWifiOnly = true
                        trackEvent(
                            Analytics.Events.WIFI_DONT_ALLOW,
                            Analytics.Values.WIFI_DONT_ALLOW
                        )
                        updateWifiSwitch()
                    }
                })
        dialogFragment.isCancelable = false
        activity?.let { dialogFragment.show(it.supportFragmentManager, AppConstants.DIALOG) }
    }

    private fun updateSDCardSwitch() {
        if (!environment.config.isDownloadToSDCardEnabled || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            binding.containerSdCard.visibility = View.GONE
            binding.tvDescriptionSdCard.visibility = View.GONE
            userPrefs.isDownloadToSDCardEnabled = false
        } else {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this)
            }
            binding.switchSdCard.setOnCheckedChangeListener(null)
            binding.switchSdCard.isChecked = environment.userPrefs.isDownloadToSDCardEnabled
            binding.switchSdCard.setOnCheckedChangeListener { _, isChecked ->
                userPrefs.isDownloadToSDCardEnabled = isChecked
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

    @Subscribe
    @SuppressWarnings("unused")
    fun onEventMainThread(event: IAPFlowEvent) {
        if (this.isResumed && event.flowAction == IAPFlowData.IAPAction.PURCHASE_FLOW_COMPLETE) {
            EventBus.getDefault().post(MyCoursesRefreshEvent())
            requireActivity().finish()
        }
    }

    @Subscribe(sticky = true)
    @SuppressWarnings("unused")
    fun onEventMainThread(event: MediaStatusChangeEvent) {
        binding.switchSdCard.isEnabled = event.isSdCardAvailable
    }

    @Subscribe(sticky = true)
    @Suppress("UNUSED_PARAMETER")
    fun onEventMainThread(event: AccountDataLoadedEvent) {
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

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): AccountFragment {
            val fragment = AccountFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}

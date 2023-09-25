package org.edx.mobile.view

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.Tab
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragment
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.FragmentMainTabsDashboardBinding
import org.edx.mobile.databinding.TabItemBinding
import org.edx.mobile.deeplink.Screen
import org.edx.mobile.deeplink.ScreenDef
import org.edx.mobile.event.FragmentSelectionEvent
import org.edx.mobile.event.MoveToDiscoveryTabEvent
import org.edx.mobile.event.ScreenArgumentsEvent
import org.edx.mobile.event.ScreenArgumentsEvent.Companion.getNewInstance
import org.edx.mobile.extenstion.setImageDrawable
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.model.FragmentItemModel
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.util.UiUtils.enforceSingleScrollDirection
import org.edx.mobile.view.adapters.FragmentItemPagerAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

@AndroidEntryPoint
class MainTabsDashboardFragment : BaseFragment() {

    @Inject
    lateinit var environment: IEdxEnvironment

    lateinit var binding: FragmentMainTabsDashboardBinding

    lateinit var fragmentItems: List<FragmentItemModel>

    private var selectedTabPosition = -1

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val learnTabPosition =
                if (environment.config.discoveryConfig.isDiscoveryEnabled) 1 else 0
            if (selectedTabPosition != learnTabPosition) {
                binding.viewPager.currentItem = learnTabPosition
                return
            }
            requireActivity().finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainTabsDashboardBinding.inflate(inflater, container, false)
        val savedArguments = arguments
        savedInstanceState?.getInt(SELECTED_POSITION)?.let {
            savedArguments?.putInt(SELECTED_POSITION, it)
        }
        fragmentItems = getTabItems()
        initializeTabs(savedArguments)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        enforceSingleScrollDirection(binding.viewPager)
        handleTabSelection(arguments)

        binding.viewPager.isUserInputEnabled = false

        requestPostNotificationsPermission()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_POSITION, selectedTabPosition)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleTabSelection(intent.extras)
        intent.extras?.let {
            EventBus.getDefault().post(ScreenArgumentsEvent(it))
        }
    }

    private fun requestPostNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun initializeTabs(arguments: Bundle?) {
        val tabLayout = binding.tabs
        tabLayout.setVisibility(fragmentItems.size > 1)

        val tabChangeListener = object : OnTabSelectedListener {
            override fun onTabSelected(tab: Tab) {
                binding.viewPager.currentItem = tab.position
                selectedTabPosition = tab.position
            }

            override fun onTabUnselected(tab: Tab) {}
            override fun onTabReselected(tab: Tab) {}
        }
        val pageChangeCallback = object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val item = fragmentItems[position]
                activity?.let { it.title = item.title }
                item.listener?.onFragmentSelected()
            }
        }

        val adapter = FragmentItemPagerAdapter(requireActivity(), fragmentItems)
        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(pageChangeCallback)
        if (fragmentItems.size - 1 > 1) {
            binding.viewPager.offscreenPageLimit = fragmentItems.size - 1
        }

        // Attach TabLayout with ViewPager
        TabLayoutMediator(
            tabLayout,
            binding.viewPager
        ) { tab: Tab, position: Int ->
            createTab(tab, fragmentItems[position])
        }.attach()

        val defaultTabPosition = if (environment.config.discoveryConfig.isDiscoveryEnabled) 1 else 0
        selectedTabPosition = arguments?.getInt(
            SELECTED_POSITION, defaultTabPosition
        ) ?: defaultTabPosition
        tabLayout.selectTab(tabLayout.getTabAt(selectedTabPosition))
        tabLayout.addOnTabSelectedListener(tabChangeListener)

        /*
         The ViewPager may not automatically call the onPageSelected method for the first item, so
         it's necessary to manually invoke the method to ensure it's called. It's also important to
         take steps to prevent it from being called again during orientation changes.
         Inspiration for this solution: https://stackoverflow.com/a/16074152/1402616
         */
        if ((arguments?.getInt(SELECTED_POSITION, -1) ?: -1) < 0) {
            binding.viewPager.post {
                pageChangeCallback.onPageSelected(binding.viewPager.currentItem)
            }
        }
    }

    private fun createTab(tab: Tab, fragmentItem: FragmentItemModel) {
        val tabItemBinding = TabItemBinding.inflate(layoutInflater)
        tabItemBinding.title.text = fragmentItem.title
        tabItemBinding.icon.setImageDrawable(fragmentItem.iconResId)

        tab.customView = tabItemBinding.root
        tab.contentDescription = fragmentItem.title
    }

    private fun getTabItems(): List<FragmentItemModel> {
        val items = mutableListOf<FragmentItemModel>()

        // Add Discover screen
        if (environment.config.discoveryConfig.isDiscoveryEnabled) {
            items.add(FragmentItemModel(
                MainDiscoveryFragment::class.java,
                resources.getString(R.string.label_discover),
                R.drawable.ic_search, arguments
            ) {
                environment.analyticsRegistry.trackScreenView(Analytics.Screens.FIND_COURSES)
            })
        }

        // Add Learn screen
        items.add(FragmentItemModel(
            LearnFragment::class.java,
            resources.getString(R.string.label_learn),
            R.drawable.ic_menu_book, arguments
        ) {
            EventBus.getDefault().post(FragmentSelectionEvent())
        })

        // Add Profile screen
        items.add(FragmentItemModel(
            AccountFragment::class.java,
            resources.getString(R.string.profile_title),
            R.drawable.ic_person, arguments
        ) {
            // Todo: We need to discuss the usage of this event
            environment.analyticsRegistry.trackScreenView(Analytics.Screens.PROFILE)
            environment.analyticsRegistry.trackScreenViewEvent(
                Analytics.Events.PROFILE_PAGE_VIEWED,
                Analytics.Screens.PROFILE
            )
        })

        return items
    }

    /**
     * To handle the tab-selection of the ViewPager based on screen name [Screen] which may be
     * sent through a deep link.
     *
     * @param bundle arguments
     */
    private fun handleTabSelection(bundle: Bundle?) {
        bundle?.apply {
            @ScreenDef val screenName = getString(Router.EXTRA_SCREEN_NAME)
            if (screenName != null && !getBoolean(Router.EXTRA_SCREEN_SELECTED, false)) {
                val fragmentItems = fragmentItems

                fragmentItems.forEachIndexed { index, item ->
                    if (shouldSelectFragment(item, screenName)) {
                        binding.viewPager.currentItem = index
                        return@forEachIndexed
                    }
                }
                // Setting `EXTRA_SCREEN_SELECTED` to true, so that upon recreation of the fragment
                // the tab defined in the deep link is not auto-selected again.
                putBoolean(Router.EXTRA_SCREEN_SELECTED, true)
            }
        }
    }

    /**
     * Determines if a tab fragment needs to be selected based on screen name.
     *
     * @param item       [FragmentItemModel] assigned to a tab.
     * @param screenName Screen name param coming from [org.edx.mobile.deeplink.DeepLinkManager]
     * @return           True if the specified tab needs to be selected, False otherwise
     */
    private fun shouldSelectFragment(
        item: FragmentItemModel,
        @ScreenDef screenName: String
    ) = when {
        screenName == Screen.PROGRAM && item.iconResId == R.drawable.ic_menu_book -> true
        screenName == Screen.PROFILE && item.iconResId == R.drawable.ic_person -> true
        screenName == Screen.DISCOVERY && item.iconResId == R.drawable.ic_search -> true
        screenName == Screen.DISCOVERY_COURSE_DETAIL && item.iconResId == R.drawable.ic_search -> true
        screenName == Screen.DISCOVERY_PROGRAM_DETAIL && item.iconResId == R.drawable.ic_search -> true
        else -> false
    }

    @Subscribe
    fun onEventMainThread(event: MoveToDiscoveryTabEvent) {
        if (!environment.config.discoveryConfig.isDiscoveryEnabled) {
            return
        }
        binding.viewPager.currentItem = 0
        if (event.screenName != null) {
            EventBus.getDefault().post(getNewInstance(event.screenName))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        private const val SELECTED_POSITION = "selected_position"
    }
}

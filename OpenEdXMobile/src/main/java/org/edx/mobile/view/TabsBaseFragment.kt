package org.edx.mobile.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.Tab
import com.google.android.material.tabs.TabLayoutMediator
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragment
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.FragmentTabsBaseBinding
import org.edx.mobile.databinding.TabItemBinding
import org.edx.mobile.deeplink.Screen
import org.edx.mobile.deeplink.ScreenDef
import org.edx.mobile.event.ScreenArgumentsEvent
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.model.FragmentItemModel
import org.edx.mobile.util.UiUtils.enforceSingleScrollDirection
import org.edx.mobile.util.UiUtils.getDrawable
import org.edx.mobile.view.adapters.FragmentItemPagerAdapter
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

abstract class TabsBaseFragment : BaseFragment() {

    @Inject
    protected lateinit var environment: IEdxEnvironment

    protected lateinit var binding: FragmentTabsBaseBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTabsBaseBinding.inflate(inflater, container, false)
        initializeTabs()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enforceSingleScrollDirection(binding.viewPager2)
        handleTabSelection(arguments)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleTabSelection(intent.extras)
        intent.extras?.let {
            EventBus.getDefault().post(ScreenArgumentsEvent(it))
        }
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
                        binding.viewPager2.currentItem = index
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
     * @param screenName screen name param coming from [org.edx.mobile.deeplink.DeepLinkManager]
     * @return `true` if the specified tab fragment needs to be selected, `false` otherwise
     */
    private fun shouldSelectFragment(
        item: FragmentItemModel,
        @ScreenDef screenName: String
    ): Boolean {
        return screenName == Screen.PROGRAM && item.iconResId == R.drawable.ic_collections_bookmark ||
                screenName == Screen.DISCOVERY && item.iconResId == R.drawable.ic_search ||
                screenName == Screen.DISCOVERY_COURSE_DETAIL && item.iconResId == R.drawable.ic_search ||
                screenName == Screen.DISCOVERY_PROGRAM_DETAIL && item.iconResId == R.drawable.ic_search
    }

    private fun initializeTabs() {
        val tabLayout = activity?.findViewById<TabLayout>(R.id.tabs) ?: return

        tabLayout.setVisibility(fragmentItems.size > 1)
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: Tab) {
                binding.viewPager2.currentItem = tab.position
            }

            override fun onTabUnselected(tab: Tab) {}
            override fun onTabReselected(tab: Tab) {}
        })

        val adapter = FragmentItemPagerAdapter(requireActivity(), fragmentItems)
        binding.viewPager2.adapter = adapter
        binding.viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val item = fragmentItems[position]
                activity?.let { it.title = item.title }
                item.listener?.onFragmentSelected()
            }
        })
        if (fragmentItems.size - 1 > 1) {
            binding.viewPager2.offscreenPageLimit = fragmentItems.size - 1
        }

        // Attach TabLayout with ViewPager
        TabLayoutMediator(
            tabLayout,
            binding.viewPager2
        ) { tab: Tab, position: Int ->
            createTab(tab, fragmentItems[position])
        }.attach()


    }

    private fun createTab(tab: Tab, fragmentItem: FragmentItemModel) {
        val iconDrawable = getDrawable(requireContext(), fragmentItem.iconResId)

        val tabItemBinding = TabItemBinding.inflate(layoutInflater)
        tabItemBinding.title.text = fragmentItem.title
        tabItemBinding.icon.setImageDrawable(iconDrawable)

        tab.customView = tabItemBinding.root
        tab.contentDescription = fragmentItem.title
    }

    /**
     * Defines the [FragmentItemModel] that we need to assign to each tab.
     *
     * @return List of [FragmentItemModel].
     */
    protected abstract val fragmentItems: List<FragmentItemModel>
}

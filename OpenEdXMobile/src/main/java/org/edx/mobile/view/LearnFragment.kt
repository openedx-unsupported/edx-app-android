package org.edx.mobile.view

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.databinding.FragmentLearnBinding
import org.edx.mobile.event.FragmentSelectionEvent
import org.edx.mobile.extenstion.CollapsingToolbarStatListener
import org.edx.mobile.extenstion.setImageDrawable
import org.edx.mobile.extenstion.setTitleStateListener
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.util.ViewAnimationUtil
import org.edx.mobile.util.images.ImageUtils
import org.edx.mobile.view.adapters.LearnDropDownAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

@AndroidEntryPoint
class LearnFragment : OfflineSupportBaseFragment() {

    private lateinit var binding: FragmentLearnBinding
    private var items: ArrayList<LearnScreenItem> = arrayListOf()
    private var selectedItemPosition = -1
    private var lastPopupWindowDismissTime = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLearnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val savedArguments = arguments
        savedInstanceState?.getInt(SELECTED_POSITION)?.let {
            savedArguments?.putInt(SELECTED_POSITION, it)
        }
        initViews(savedArguments)
        EventBus.getDefault().register(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_POSITION, selectedItemPosition)
    }

    private fun initViews(arguments: Bundle?) {
        items = arrayListOf(LearnScreenItem.MY_COURSES)
        if (environment.config.programConfig.isEnabled) {
            items.add(LearnScreenItem.MY_PROGRAMS)
            binding.llLearnSelection.setVisibility(true)
            binding.llLearnSelection.setOnClickListener {
                // To prevent the reopening of a PopupWindow upon dismissal by clicking on an
                // already open window
                val currentTime = SystemClock.elapsedRealtime()
                if (currentTime - lastPopupWindowDismissTime > MIN_CLICK_INTERVAL) {
                    showLearnPopupMenu(binding.tvSelectedItem, items)
                    binding.ivSelectorIcon.setImageDrawable(R.drawable.ic_drop_up)
                }
            }
        } else {
            binding.ivSelectorIcon.setVisibility(false)
        }

        binding.appbar.setTitleStateListener(
            binding.collapsingToolbar,
            object : CollapsingToolbarStatListener {
                override fun onExpanded() {
                    ViewAnimationUtil.animateTitleSize(
                        binding.tvSelectedItem,
                        resources.getDimension(R.dimen.edx_x_large)
                    )
                    ImageUtils.animateIconSize(binding.ivSelectorIcon, 1f)
                }

                override fun onCollapsed() {
                    ViewAnimationUtil.animateTitleSize(
                        binding.tvSelectedItem,
                        resources.getDimension(R.dimen.edx_large)
                    )
                    ImageUtils.animateIconSize(binding.ivSelectorIcon, 0.75f)
                }
            })

        selectedItemPosition = arguments?.getInt(SELECTED_POSITION) ?: items.first().ordinal
        // no need to track event cuz handle event through event bus on tab selection.
        updateScreen(items[selectedItemPosition], false)
    }

    private fun showLearnPopupMenu(parentView: View, items: ArrayList<LearnScreenItem>) {
        val listPopupWindow = ListPopupWindow(requireContext())
        listPopupWindow.anchorView = parentView
        val adapter = LearnDropDownAdapter(requireContext(), R.layout.learn_selection_item)
        adapter.addAll(items)
        listPopupWindow.setAdapter(adapter)
        listPopupWindow.setOnItemClickListener { parent: AdapterView<*>?, _: View?, position: Int, _: Long ->
            if (parent?.selectedItemPosition != position) {
                selectedItemPosition = position
                updateScreen(items[position])
            }
            listPopupWindow.dismiss()
        }
        listPopupWindow.show()
        adapter.select(selectedItemPosition)
        adapter.notifyDataSetChanged()
        listPopupWindow.listView?.setSelector(android.R.color.transparent)

        listPopupWindow.setOnDismissListener {
            binding.ivSelectorIcon.setImageDrawable(R.drawable.ic_drop_down)
            lastPopupWindowDismissTime = SystemClock.elapsedRealtime()
        }
    }

    private fun updateScreen(item: LearnScreenItem, trackEvent: Boolean = true) {
        val screenName: String
        val fragment: Fragment = when (item) {
            LearnScreenItem.MY_COURSES -> {
                screenName = Analytics.Screens.MY_COURSES
                MyCoursesListFragment().apply {
                    arguments = this@LearnFragment.arguments
                }
            }

            LearnScreenItem.MY_PROGRAMS -> {
                screenName = Analytics.Screens.MY_PROGRAM
                WebViewProgramFragment.newInstance(environment.config.programConfig.url)
            }
        }
        if (childFragmentManager.findFragmentByTag(fragment.javaClass.name) == null) {
            val fragmentTransaction = childFragmentManager.beginTransaction()
            fragmentTransaction.replace(binding.flLearn.id, fragment, fragment.javaClass.name)
            fragmentTransaction.commit()
        }
        binding.tvSelectedItem.text = getString(item.labelRes)
        if (trackEvent) {
            environment.analyticsRegistry.trackScreenView(screenName)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onEventMainThread(event: FragmentSelectionEvent) {
        updateScreen(items[selectedItemPosition])
    }

    override fun isShowingFullScreenError(): Boolean {
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        private const val SELECTED_POSITION = "selected_position"
        private const val MIN_CLICK_INTERVAL = 500L
    }

    enum class LearnScreenItem(val labelRes: Int) {
        MY_COURSES(R.string.label_my_courses),
        MY_PROGRAMS(R.string.label_my_programs)
    }
}

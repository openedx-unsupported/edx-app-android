package org.edx.mobile.view;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentTabsBaseBinding;
import org.edx.mobile.deeplink.DeepLinkManager;
import org.edx.mobile.deeplink.Screen;
import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.event.ScreenArgumentsEvent;
import org.edx.mobile.model.FragmentItemModel;
import org.edx.mobile.util.UiUtils;
import org.edx.mobile.view.adapters.FragmentItemPagerAdapter;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

public abstract class TabsBaseFragment extends BaseFragment {
    @Inject
    protected IEdxEnvironment environment;

    @Nullable
    protected FragmentTabsBaseBinding binding;

    @ColorRes
    private int TAB_COLOR_SELECTOR_RES = R.color.tab_selector;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tabs_base, container, false);
        initializeTabs();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Enforce to intercept single scrolling direction
        if (binding != null) {
            UiUtils.INSTANCE.enforceSingleScrollDirection(binding.viewPager2);
        }
        handleTabSelection(getArguments());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleTabSelection(intent.getExtras());
        if (intent.getExtras() == null) {
            return;
        }
        EventBus.getDefault().post(new ScreenArgumentsEvent(intent.getExtras()));
    }

    /**
     * Method to handle the tab-selection of the ViewPager based on screen name {@link Screen}
     * which may be sent through a deep link.
     *
     * @param bundle arguments
     */
    private void handleTabSelection(@Nullable Bundle bundle) {
        if (bundle != null && binding != null) {
            @ScreenDef String screenName = bundle.getString(Router.EXTRA_SCREEN_NAME);
            if (screenName != null && !bundle.getBoolean(Router.EXTRA_SCREEN_SELECTED, false)) {
                final List<FragmentItemModel> fragmentItems = getFragmentItems();
                for (int i = 0; i < fragmentItems.size(); i++) {
                    final FragmentItemModel item = fragmentItems.get(i);
                    if (shouldSelectFragment(item, screenName)) {
                        binding.viewPager2.setCurrentItem(i);
                        break;
                    }
                }
                // Setting `EXTRA_SCREEN_SELECTED` to true, so that upon recreation of the fragment the tab defined in
                // the deep link is not auto-selected again.
                bundle.putBoolean(Router.EXTRA_SCREEN_SELECTED, true);
            }
        }
    }

    /**
     * Determines if a tab fragment needs to be selected based on screen name.
     *
     * @param item       {@link FragmentItemModel} assigned to a tab.
     * @param screenName screen name param coming from {@link DeepLinkManager}
     * @return <code>true</code> if the specified tab fragment needs to be selected, <code>false</code> otherwise
     */
    private boolean shouldSelectFragment(@NonNull FragmentItemModel item, @NonNull @ScreenDef String screenName) {
        return (screenName.equals(Screen.PROGRAM) && item.getIconResId() == R.drawable.ic_collections_bookmark) ||
                (screenName.equals(Screen.DISCOVERY) && item.getIconResId() == R.drawable.ic_search) ||
                (screenName.equals(Screen.DISCOVERY_COURSE_DETAIL) && item.getIconResId() == R.drawable.ic_search) ||
                (screenName.equals(Screen.DISCOVERY_PROGRAM_DETAIL) && item.getIconResId() == R.drawable.ic_search);
    }

    private void initializeTabs() {
        // Get fragment items list
        final List<FragmentItemModel> fragmentItems = getFragmentItems();
        // Init tabs
        final TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.tabs);

        // This check is necessary to run test cases on this fragment in which parent activity of
        // fragment will not have any TabLayout view e.g. CourseTabsDashboardFragmentTest.
        // TODO: Remove this check when we would find out how to initialize a fragment with associated activity in Roboelectric.
        if (tabLayout == null) {
            return;
        }

        // No need to show tabs if we only have
        if (fragmentItems.size() <= 1) {
            tabLayout.setVisibility(View.GONE);
        } else {
            tabLayout.setVisibility(View.VISIBLE);
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    binding.viewPager2.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        }

        // Init page change listener
        final ViewPager2.OnPageChangeCallback pageChangeListener = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                final FragmentItemModel item = fragmentItems.get(position);
                if (getActivity() != null) {
                    getActivity().setTitle(item.getTitle());
                }
                if (item.getListener() != null) {
                    item.getListener().onFragmentSelected();
                }
            }
        };

        // Init view pager
        final FragmentItemPagerAdapter adapter = new FragmentItemPagerAdapter(this.getActivity(), fragmentItems);
        binding.viewPager2.setAdapter(adapter);
        binding.viewPager2.registerOnPageChangeCallback(pageChangeListener);

        // Attach Tab layout with viewpager2
        new TabLayoutMediator(tabLayout, binding.viewPager2, (tab, position) -> {
            createTab(tab, fragmentItems.get(position));
        }).attach();
        /*
         It will load all of the fragments on creation and will stay in memory till ViewPager's
         life time, it will greatly improve our user experience as all fragments will be available
         to view all the time. We can decrease the limit if it creates memory problems on low-end devices.
         */
        if (fragmentItems.size() - 1 > 1) {
            binding.viewPager2.setOffscreenPageLimit(fragmentItems.size() - 1);
        }
    }

    protected void createTab(@NonNull TabLayout.Tab tab, @NonNull FragmentItemModel fragmentItem) {
        // Tabs doesn't support `IconDrawable.colorRes` with material theme so use custom view having `ImageView`
        Drawable iconDrawable = UiUtils.INSTANCE.getDrawable(requireContext(), fragmentItem.getIconResId());
        iconDrawable.setTintList(ContextCompat.getColorStateList(requireContext(), TAB_COLOR_SELECTOR_RES));
        iconDrawable.setTintMode(PorterDuff.Mode.SRC_IN);
        final View tabItem = LayoutInflater.from(getContext()).inflate(R.layout.tab_item, null);
        final AppCompatImageView icon = (AppCompatImageView) tabItem.findViewById(R.id.icon);
        int size;
        if (showTitleInTabs()) {
            size = requireContext().getResources().getDimensionPixelSize(R.dimen.edx_small);
            final TextView title = (TextView) tabItem.findViewById(R.id.title);
            title.setText(fragmentItem.getTitle());
            title.setTextColor(ContextCompat.getColorStateList(requireContext(), TAB_COLOR_SELECTOR_RES));
        } else {
            size = requireContext().getResources().getDimensionPixelSize(R.dimen.edx_x_large);
            // set tab view ids for the course dash board screen for the automation.
            String resourceString = fragmentItem.getTitle().toString().toLowerCase().replace(" ", "_");
            int id = getResources().getIdentifier(resourceString, "id", BuildConfig.APPLICATION_ID);
            tabItem.setId(id);
        }
        icon.getLayoutParams().height = size;
        icon.getLayoutParams().width = size;
        icon.setImageDrawable(iconDrawable);
        tab.setCustomView(tabItem);
        tab.setContentDescription(fragmentItem.getTitle());
    }

    /**
     * Tells if we need to show the title text with icons in a tab.
     *
     * @return <code>true</code> if title needs to be shown, <code>false</code> otherwise.
     */
    protected abstract boolean showTitleInTabs();

    /**
     * Defines the {@link FragmentItemModel} that we need to assign to each tab.
     *
     * @return List of {@link FragmentItemModel}.
     */
    protected abstract List<FragmentItemModel> getFragmentItems();
}

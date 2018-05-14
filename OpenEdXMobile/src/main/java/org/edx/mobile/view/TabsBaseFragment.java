package org.edx.mobile.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.IconDrawable;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentTabsBaseBinding;
import org.edx.mobile.model.FragmentItemModel;
import org.edx.mobile.view.adapters.FragmentItemPagerAdapter;

import java.util.List;

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
            for (FragmentItemModel fragmentItem : fragmentItems) {
                tabLayout.addTab(createTab(tabLayout, fragmentItem));
            }
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    binding.viewPager.setCurrentItem(tab.getPosition());
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
        final TabLayout.TabLayoutOnPageChangeListener pageChangeListener = new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                final FragmentItemModel item = fragmentItems.get(position);
                getActivity().setTitle(item.getTitle());
                if (item.getListener() != null) {
                    item.getListener().onFragmentSelected();
                }
            }
        };

        // Init view pager
        final FragmentItemPagerAdapter adapter = new FragmentItemPagerAdapter(this.getActivity().getSupportFragmentManager(), fragmentItems);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.addOnPageChangeListener(pageChangeListener);
        /*
         It will load all of the fragments on creation and will stay in memory till ViewPager's
         life time, it will greatly improve our user experience as all fragments will be available
         to view all the time. We can decrease the limit if it creates memory problems on low-end devices.
         */
        binding.viewPager.setOffscreenPageLimit(fragmentItems.size() - 1);

        /*
         ViewPager doesn't call the onPageSelected for its first item, so we have to explicitly
         call it ourselves.
         Inspiration for this solution: https://stackoverflow.com/a/16074152/1402616
         */
        binding.viewPager.post(new Runnable() {
            @Override
            public void run() {
                pageChangeListener.onPageSelected(binding.viewPager.getCurrentItem());
            }
        });
    }

    protected TabLayout.Tab createTab(TabLayout tabLayout, FragmentItemModel fragmentItem) {
        final IconDrawable iconDrawable = new IconDrawable(getContext(), fragmentItem.getIcon());
        iconDrawable.colorRes(getContext(), TAB_COLOR_SELECTOR_RES);
        final TabLayout.Tab tab;
        tab = tabLayout.newTab();
        if (showTitleInTabs()) {
            iconDrawable.sizeRes(getContext(), R.dimen.edx_small);
            final View tabItem = LayoutInflater.from(getContext()).inflate(R.layout.tab_item, null);
            final TextView title = (TextView) tabItem.findViewById(R.id.title);
            final ImageView icon = (ImageView) tabItem.findViewById(R.id.icon);
            title.setText(fragmentItem.getTitle());

            title.setTextColor(ContextCompat.getColorStateList(getContext(), TAB_COLOR_SELECTOR_RES));
            icon.setImageDrawable(iconDrawable);
            tab.setCustomView(tabItem);
        } else {
            tab.setIcon(iconDrawable);
        }
        tab.setContentDescription(fragmentItem.getTitle());
        return tab;
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

package org.edx.mobile.view;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import org.edx.mobile.R;
import org.edx.mobile.event.DiscoveryTabSelectedEvent;
import org.edx.mobile.event.MoveToDiscoveryTabEvent;
import org.edx.mobile.event.ScreenArgumentsEvent;
import org.edx.mobile.model.FragmentItemModel;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.util.ConfigUtil;
import org.edx.mobile.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MainTabsDashboardFragment extends TabsBaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.my_courses, menu);
        menu.findItem(R.id.menu_item_account).setVisible(true);
        menu.findItem(R.id.menu_item_account).setIcon(
                UiUtils.INSTANCE.getDrawable(requireContext(), R.drawable.ic_settings,
                        R.dimen.action_bar_icon_size));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_account: {
                environment.getRouter().showAccountActivity(getActivity());
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    protected boolean showTitleInTabs() {
        return true;
    }

    @Override
    public List<FragmentItemModel> getFragmentItems() {
        ArrayList<FragmentItemModel> items = new ArrayList<>();

        items.add(new FragmentItemModel(MyCoursesListFragment.class,
                getResources().getString(R.string.label_my_courses), R.drawable.ic_bookmark_border, getArguments(),
                new FragmentItemModel.FragmentStateListener() {
                    @Override
                    public void onFragmentSelected() {
                        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.MY_COURSES);
                    }
                }));

        if (environment.getConfig().getProgramConfig().isEnabled()) {
            items.add(new FragmentItemModel(WebViewProgramFragment.class,
                    getResources().getString(R.string.label_my_programs), R.drawable.ic_collections_bookmark,
                    WebViewProgramFragment.makeArguments(environment.getConfig().getProgramConfig().getUrl(),
                            null, true),
                    new FragmentItemModel.FragmentStateListener() {
                        @Override
                        public void onFragmentSelected() {

                        }
                    }));
        }

        if (ConfigUtil.Companion.isCourseDiscoveryEnabled(environment) ||
                ConfigUtil.Companion.isProgramDiscoveryEnabled(environment)) {
            items.add(new FragmentItemModel(MainDiscoveryFragment.class,
                    getResources().getString(R.string.label_discovery), R.drawable.ic_search,
                    getArguments(),
                    () -> EventBus.getDefault().post(new DiscoveryTabSelectedEvent())
            ));
        }

        return items;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull MoveToDiscoveryTabEvent event) {
        if (!ConfigUtil.Companion.isCourseDiscoveryEnabled(environment)) {
            return;
        }
        if (binding != null) {
            binding.viewPager2.setCurrentItem(binding.viewPager2.getAdapter().getItemCount() - 1);
            if (event.getScreenName() != null) {
                EventBus.getDefault().post(ScreenArgumentsEvent.Companion.getNewInstance(event.getScreenName()));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}

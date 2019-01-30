package org.edx.mobile.view;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentMainDiscoveryBinding;
import org.edx.mobile.event.DiscoveryTabSelectedEvent;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.view.dialog.NativeFindCoursesFragment;

import de.greenrobot.event.EventBus;

public class MainDiscoveryFragment extends BaseFragment {
    @Inject
    protected IEdxEnvironment environment;

    @Nullable
    protected FragmentMainDiscoveryBinding binding;

    private ToolbarCallbacks toolbarCallbacks;

    private SparseArray<Fragment> fragmentsArray = new SparseArray<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_discovery,
                container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFragments();
        binding.options.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onFragmentSelected(checkedId, true);
            }
        });
        EventBus.getDefault().register(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarCallbacks = getActivity() instanceof ToolbarCallbacks ?
                (ToolbarCallbacks) getActivity() : null;
        onFragmentVisibilityChanged(getUserVisibleHint());
    }

    private void initFragments() {
        // Course discovery
        if (environment.getConfig().getDiscoveryConfig().getCourseDiscoveryConfig() != null &&
                environment.getConfig().getDiscoveryConfig().getCourseDiscoveryConfig().isDiscoveryEnabled()) {
            Fragment courseDiscoveryFragment;
            if (environment.getConfig().getDiscoveryConfig().getCourseDiscoveryConfig().isWebviewDiscoveryEnabled()) {
                courseDiscoveryFragment = getChildFragmentManager().findFragmentByTag("fragment_courses_webview");
                if (courseDiscoveryFragment == null) {
                    courseDiscoveryFragment = new WebViewDiscoverCoursesFragment();
                    courseDiscoveryFragment.setArguments(getArguments());
                    commitFragmentTransaction(R.id.fl_container, courseDiscoveryFragment, "fragment_courses_webview");
                }
            } else {
                courseDiscoveryFragment = getChildFragmentManager().findFragmentByTag("fragment_courses_native");
                if (courseDiscoveryFragment == null) {
                    courseDiscoveryFragment = new NativeFindCoursesFragment();
                    commitFragmentTransaction(R.id.fl_container, courseDiscoveryFragment, "fragment_courses_native");
                }
            }

            fragmentsArray.put(R.id.option_courses, courseDiscoveryFragment);
            addTabItem(R.id.option_courses, R.string.label_my_courses);
        }

        // Program discovery
        if (environment.getConfig().getDiscoveryConfig().getProgramDiscoveryConfig() != null &&
                environment.getConfig().getDiscoveryConfig().getProgramDiscoveryConfig().isDiscoveryEnabled(environment)) {
            Fragment programDiscoveryFragment = getChildFragmentManager().findFragmentByTag("fragment_programs");
            if (programDiscoveryFragment == null) {
                programDiscoveryFragment = new WebViewDiscoverProgramsFragment();
                commitFragmentTransaction(R.id.fl_container, programDiscoveryFragment, "fragment_programs");
            }

            fragmentsArray.put(R.id.option_programs, programDiscoveryFragment);
            addTabItem(R.id.option_programs, R.string.label_my_programs);
        }

        if (fragmentsArray.size() > 1) {
            setTabsBackground(binding.options);
            final int firstBtnId = fragmentsArray.keyAt(0);
            if (firstBtnId != -1) {
                onFragmentSelected(firstBtnId, false);
                binding.options.check(firstBtnId);
            }
        } else {
            hideTabsBar();
        }
    }

    private void onFragmentSelected(@IdRes int resId, final boolean isUserSelected) {
        for (int i = 0; i < fragmentsArray.size(); i++) {
            if (resId == fragmentsArray.keyAt(i)) {
                showFragment(fragmentsArray.valueAt(i));
            } else {
                hideFragment(fragmentsArray.valueAt(i));
            }
        }
        if (isUserSelected) {
            switch (resId) {
                case R.id.option_courses:
                    environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FIND_COURSES);
                    break;
                case R.id.option_programs:
                    environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FIND_PROGRAMS);
                    break;
            }
        }
    }

    private void commitFragmentTransaction(@IdRes int containerViewId, Fragment fragment,
                                           @Nullable String tag) {
        final FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(containerViewId, fragment, tag);
        fragmentTransaction.commit();
    }

    private void showFragment(@Nullable Fragment fragment) {
        if (fragment == null || !fragment.isHidden()) {
            return;
        }
        getChildFragmentManager().beginTransaction()
                .show(fragment)
                .commit();
    }

    private void hideFragment(@Nullable Fragment fragment) {
        if (fragment == null || fragment.isHidden()) {
            return;
        }
        getChildFragmentManager().beginTransaction()
                .hide(fragment)
                .commit();
    }

    private void addTabItem(@IdRes int id, @StringRes int label) {
        final RadioButton radioButton = (RadioButton) getLayoutInflater().inflate(
                R.layout.segment_control_button_base, binding.options, false);
        radioButton.setId(id);
        radioButton.setText(label);
        binding.options.addView(radioButton);
    }

    private void setTabsBackground(@NonNull RadioGroup options) {
        final int childCount = options.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i == 0) {
                options.getChildAt(i).setBackgroundResource(R.drawable.edx_segmented_control_left_background);
            } else if (i == childCount - 1) {
                options.getChildAt(i).setBackgroundResource(R.drawable.edx_segmented_control_right_background);
            } else {
                options.getChildAt(i).setBackgroundResource(R.drawable.edx_segmented_control_middle_background);
            }
        }
    }

    private void hideTabsBar() {
        binding.options.setVisibility(View.GONE);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull DiscoveryTabSelectedEvent event) {
        onFragmentSelected(binding.options.getCheckedRadioButtonId(), true);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        onFragmentVisibilityChanged(isVisibleToUser);
    }

    public void onFragmentVisibilityChanged(final boolean isVisibleToUser) {
        if (toolbarCallbacks != null && toolbarCallbacks.getSearchView() != null) {
            // Sometimes when fragment visibility is changed by swiping left/right the viewpager,
            // visibility of SearchView doesn't get changed accordingly, putting the code within
            // runnable does the job in this case.
            toolbarCallbacks.getSearchView().post(new Runnable() {
                @Override
                public void run() {
                    final Fragment nativeCoursesFragment = getChildFragmentManager().findFragmentByTag("fragment_courses_native");
                    if (nativeCoursesFragment != null && nativeCoursesFragment.isVisible()) {
                        toolbarCallbacks.getSearchView().setVisibility(View.GONE);
                    } else {
                        toolbarCallbacks.getSearchView().setVisibility(isVisibleToUser ? View.VISIBLE : View.GONE);
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}

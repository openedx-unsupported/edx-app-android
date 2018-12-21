package org.edx.mobile.view;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private Fragment courseDiscoveryFragment;
    private Fragment programDiscoveryFragment;

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

    public void initFragments() {
        @IdRes
        int checkedId = -1;

        if (environment.getConfig().getDiscoveryConfig().getProgramDiscoveryConfig() != null &&
                environment.getConfig().getDiscoveryConfig().getProgramDiscoveryConfig().isDiscoveryEnabled(environment)) {
            programDiscoveryFragment = getChildFragmentManager().findFragmentByTag("fragment_programs");
            if (programDiscoveryFragment == null) {
                programDiscoveryFragment = new WebViewDiscoverProgramsFragment();
                commitFragmentTransaction(R.id.fl_programs, programDiscoveryFragment, "fragment_programs");
            }
            checkedId = R.id.option_programs;
        } else {
            hideTabsBar();
        }

        if (environment.getConfig().getDiscoveryConfig().getCourseDiscoveryConfig() != null &&
                environment.getConfig().getDiscoveryConfig().getCourseDiscoveryConfig().isDiscoveryEnabled()) {
            if (environment.getConfig().getDiscoveryConfig().getCourseDiscoveryConfig().isWebviewDiscoveryEnabled()) {
                courseDiscoveryFragment = getChildFragmentManager().findFragmentByTag("fragment_courses_webview");
                if (courseDiscoveryFragment == null) {
                    courseDiscoveryFragment = new WebViewDiscoverCoursesFragment();
                    courseDiscoveryFragment.setArguments(getArguments());
                    commitFragmentTransaction(R.id.fl_courses, courseDiscoveryFragment, "fragment_courses_webview");
                }
            } else {
                courseDiscoveryFragment = getChildFragmentManager().findFragmentByTag("fragment_courses_native");
                if (courseDiscoveryFragment == null) {
                    courseDiscoveryFragment = new NativeFindCoursesFragment();
                    commitFragmentTransaction(R.id.fl_courses, courseDiscoveryFragment, "fragment_courses_native");
                }
            }

            checkedId = R.id.option_courses;
        } else {
            hideTabsBar();
        }

        if (checkedId != -1) {
            onFragmentSelected(checkedId, false);
            binding.options.check(checkedId);
        }
    }

    public void onFragmentSelected(@IdRes int resId, final boolean isUserSelected) {
        switch (resId) {
            case R.id.option_courses:
                showFragment(courseDiscoveryFragment);
                hideFragment(programDiscoveryFragment);
                if (isUserSelected) {
                    environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FIND_COURSES);
                }
                break;
            case R.id.option_programs:
                showFragment(programDiscoveryFragment);
                hideFragment(courseDiscoveryFragment);
                if (isUserSelected) {
                    environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FIND_PROGRAMS);
                }
                break;
        }
    }

    public void commitFragmentTransaction(@IdRes int containerViewId, Fragment fragment,
                                           @Nullable String tag) {
        final FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(containerViewId, fragment, tag);
        fragmentTransaction.commit();
    }

    public void showFragment(@Nullable Fragment fragment) {
        if (fragment == null || !fragment.isHidden()) {
            return;
        }
        getChildFragmentManager().beginTransaction()
                .show(fragment)
                .commit();
    }

    public void hideFragment(@Nullable Fragment fragment) {
        if (fragment == null || fragment.isHidden()) {
            return;
        }
        getChildFragmentManager().beginTransaction()
                .hide(fragment)
                .commit();
    }

    public void hideTabsBar() {
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

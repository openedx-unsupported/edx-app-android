package org.edx.mobile.view;


import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentMainDiscoveryBinding;
import org.edx.mobile.deeplink.Screen;
import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.event.DiscoveryTabSelectedEvent;
import org.edx.mobile.event.ScreenArgumentsEvent;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.util.ConfigUtil;
import org.edx.mobile.view.dialog.NativeFindCoursesFragment;

import de.greenrobot.event.EventBus;

import static org.edx.mobile.view.Router.EXTRA_PATH_ID;
import static org.edx.mobile.view.Router.EXTRA_SCREEN_NAME;

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
        if (getArguments() != null) {
            handleTabSelection(getArguments());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarCallbacks = getActivity() instanceof ToolbarCallbacks ?
                (ToolbarCallbacks) getActivity() : null;
    }

    @Override
    public void onResume() {
        super.onResume();
        onFragmentVisibilityChanged(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        onFragmentVisibilityChanged(false);
    }

    private void initFragments() {
        // Course discovery
        if (ConfigUtil.Companion.isCourseDiscoveryEnabled(environment)) {
            Fragment courseDiscoveryFragment;
            if (ConfigUtil.Companion.isCourseWebviewDiscoveryEnabled(environment)) {
                courseDiscoveryFragment = getChildFragmentManager().findFragmentByTag("fragment_courses_webview");
                if (courseDiscoveryFragment == null) {
                    courseDiscoveryFragment = new WebViewDiscoverCoursesFragment();
                    commitFragmentTransaction(courseDiscoveryFragment, "fragment_courses_webview");
                }
            } else {
                courseDiscoveryFragment = getChildFragmentManager().findFragmentByTag("fragment_courses_native");
                if (courseDiscoveryFragment == null) {
                    courseDiscoveryFragment = new NativeFindCoursesFragment();
                    commitFragmentTransaction(courseDiscoveryFragment, "fragment_courses_native");
                }
            }
            courseDiscoveryFragment.setArguments(getArguments());
            fragmentsArray.put(R.id.option_courses, courseDiscoveryFragment);
            addTabItem(R.id.option_courses, R.string.label_my_courses);
        }

        // Program discovery
        if (ConfigUtil.Companion.isProgramDiscoveryEnabled(environment)) {
            Fragment programDiscoveryFragment = getChildFragmentManager().findFragmentByTag("fragment_programs");
            if (programDiscoveryFragment == null) {
                programDiscoveryFragment = new WebViewDiscoverProgramsFragment();
                commitFragmentTransaction(programDiscoveryFragment, "fragment_programs");
            }

            fragmentsArray.put(R.id.option_programs, programDiscoveryFragment);
            addTabItem(R.id.option_programs, R.string.label_my_programs);
        }

        // Degree discovery
        if (ConfigUtil.Companion.isDegreeDiscoveryEnabled(environment)) {
            Fragment degreeDiscoveryFragment = getChildFragmentManager().findFragmentByTag("fragment_degrees");
            if (degreeDiscoveryFragment == null) {
                degreeDiscoveryFragment = new WebViewDiscoverDegreesFragment();
                commitFragmentTransaction(degreeDiscoveryFragment, "fragment_degrees");
            }

            fragmentsArray.put(R.id.option_degrees, degreeDiscoveryFragment);
            addTabItem(R.id.option_degrees, R.string.label_degrees);
        }

        if (fragmentsArray.size() < 2) {
            hideTabsBar();
        }
        if (fragmentsArray.size() > 0) {
            final int firstBtnId = fragmentsArray.keyAt(0);
            onFragmentSelected(firstBtnId, false);
            binding.options.check(firstBtnId);
        }
    }

    private void onFragmentSelected(@IdRes int fragmentTabId, final boolean isUserSelected) {
        // First we need to hide all the fragments along with their shared search view,
        // then show the required fragment and check either that fragment needs to show the
        // search view or not through its `onHiddenChanged` method.
        Fragment selectedFragment = null;
        for (int i = 0; i < fragmentsArray.size(); i++) {
            if (fragmentTabId == fragmentsArray.keyAt(i)) {
                selectedFragment = fragmentsArray.valueAt(i);
            } else {
                hideFragment(fragmentsArray.valueAt(i));
            }
        }
        if (selectedFragment != null) {
            showFragment(selectedFragment);
        }
        if (isUserSelected) {
            switch (fragmentTabId) {
                case R.id.option_courses:
                    environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FIND_COURSES);
                    break;
                case R.id.option_programs:
                    environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FIND_PROGRAMS);
                    break;
                case R.id.option_degrees:
                    environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FIND_DEGREES);
                    break;
            }
        }
    }

    private void commitFragmentTransaction(@NonNull Fragment fragment, @Nullable String tag) {
        final FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fl_container, fragment, tag);
        fragmentTransaction.commit();
    }

    private void showFragment(@Nullable Fragment fragment) {
        if (fragment == null || !fragment.isHidden()) {
            return;
        }
        // Use commitNow() method to perform the FragmentManager transaction synchronously
        // instated of commit(), otherwise program discovery screen is not visible to the guest user
        // in case of deep linking.
        getChildFragmentManager().beginTransaction()
                .show(fragment)
                .commitNow();
    }

    private void hideFragment(@Nullable Fragment fragment) {
        if (fragment == null || fragment.isHidden()) {
            return;
        }
        // Use commitNow() method to perform the FragmentManager transaction synchronously
        // instated of commit(), otherwise program discovery screen is not visible to the guest user
        // in case of deep linking.
        getChildFragmentManager().beginTransaction()
                .hide(fragment)
                .commitNow();
    }

    private void addTabItem(@IdRes int id, @StringRes int label) {
        final RadioButton radioButton = (RadioButton) getLayoutInflater().inflate(
                R.layout.segment_control_button_base, binding.options, false);
        radioButton.setId(id);
        radioButton.setText(label);
        binding.options.addView(radioButton);
    }

    private void hideTabsBar() {
        binding.options.setVisibility(View.GONE);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull DiscoveryTabSelectedEvent event) {
        onFragmentSelected(binding.options.getCheckedRadioButtonId(), true);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull ScreenArgumentsEvent event) {
        handleTabSelection(event.getBundle());
    }

    private void handleTabSelection(@NonNull Bundle bundle) {
        @ScreenDef final String screenName = bundle.getString(EXTRA_SCREEN_NAME);
        if (screenName == null) {
            return;
        }
        final int btnId = getBtnIdAgainstScreeName(screenName);
        if (btnId != -1) {
            onFragmentSelected(btnId, true);
            binding.options.check(btnId);
        }

        final String pathId = bundle.getString(EXTRA_PATH_ID);
        if (!TextUtils.isEmpty(pathId)) {
            switch (screenName) {
                case Screen.PROGRAM:
                    environment.getRouter().showProgramWebViewActivity(getActivity(),
                            environment, pathId, getActivity().getString(R.string.label_my_programs));
                    break;
                case Screen.COURSE_DISCOVERY:
                    environment.getRouter().showCourseInfo(getActivity(), pathId);
                    break;
                case Screen.PROGRAM_DISCOVERY:
                case Screen.DEGREE_DISCOVERY:
                    environment.getRouter().showProgramInfo(getActivity(), pathId);
                    break;
            }
        }
        // Setting this to null, so that upon recreation of the fragment, relevant activity
        // shouldn't be auto created again.
        bundle.putString(Router.EXTRA_SCREEN_NAME, null);
    }

    private int getBtnIdAgainstScreeName(@NonNull @ScreenDef String screeName) {
        switch (screeName) {
            case Screen.COURSE_DISCOVERY:
                return R.id.option_courses;
            case Screen.PROGRAM_DISCOVERY:
                return R.id.option_programs;
            case Screen.DEGREE_DISCOVERY:
                return R.id.option_degrees;
            default:
                return -1;
        }
    }

    public void onFragmentVisibilityChanged(final boolean isVisibleToUser) {
        if (toolbarCallbacks != null && toolbarCallbacks.getSearchView() != null) {
            // Sometimes when fragment visibility is changed by swiping left/right the viewpager,
            // visibility of SearchView doesn't get changed accordingly, putting the code within
            // runnable does the job in this case.
            toolbarCallbacks.getSearchView().post(new Runnable() {
                @Override
                public void run() {
                    // This check is added to fix a crash i-e- LEARNER-7137, it happens when the
                    // fragment is no longer attached to its parent activity/fragment, so a simple
                    // isAdded() check should resolve the issue. Ref: https://stackoverflow.com/a/44845661
                    if (!isAdded()) {
                        return;
                    }
                    final Fragment nativeCoursesFragment = getChildFragmentManager().findFragmentByTag("fragment_courses_native");
                    if ((nativeCoursesFragment != null && nativeCoursesFragment.isVisible()) || !isVisibleToUser) {
                        toolbarCallbacks.getSearchView().setVisibility(View.GONE);
                    } else {
                        updateShownFragmentsVisibility();
                    }
                }
            });
        }
    }

    /**
     * This function lets the currently shown Fragment know that it has now become visible to
     * the user by calling its {@link Fragment#setUserVisibleHint(boolean)} function.
     */
    private void updateShownFragmentsVisibility() {
        for (int i = 0; i < fragmentsArray.size(); i++) {
            final Fragment fragment = fragmentsArray.get(fragmentsArray.keyAt(i));
            if (fragment != null && fragment.getUserVisibleHint()
                    && fragment.isVisible()) {
                fragment.setUserVisibleHint(true);
                return;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}

package org.edx.mobile.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentMainDiscoveryBinding;
import org.edx.mobile.deeplink.Screen;
import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.event.ScreenArgumentsEvent;
import org.edx.mobile.util.ConfigUtil;
import org.edx.mobile.view.dialog.NativeFindCoursesFragment;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainDiscoveryFragment extends BaseFragment {
    @Inject
    protected IEdxEnvironment environment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return FragmentMainDiscoveryBinding.inflate(inflater, container, false)
                .getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFragments();
        EventBus.getDefault().register(this);
        if (getArguments() != null) {
            handleDeepLink(getArguments());
        }
    }

    private void initFragments() {
        // Course discovery
        if (ConfigUtil.Companion.isCourseDiscoveryEnabled(environment)) {
            Fragment courseDiscoveryFragment;
            if (ConfigUtil.Companion.isCourseWebviewDiscoveryEnabled(environment)) {
                courseDiscoveryFragment = getChildFragmentManager().findFragmentByTag("fragment_courses_webview");
                if (courseDiscoveryFragment == null) {
                    courseDiscoveryFragment = new WebViewDiscoverFragment();
                    commitFragmentTransaction(courseDiscoveryFragment, "fragment_courses_webview");
                }
            } else {
                courseDiscoveryFragment = getChildFragmentManager().findFragmentByTag("fragment_courses_native");
                if (courseDiscoveryFragment == null) {
                    courseDiscoveryFragment = new NativeFindCoursesFragment();
                    commitFragmentTransaction(courseDiscoveryFragment, "fragment_courses_native");
                }
            }
        }
    }

    private void commitFragmentTransaction(@NonNull Fragment fragment, @Nullable String tag) {
        final FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fl_container, fragment, tag);
        fragmentTransaction.commit();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull ScreenArgumentsEvent event) {
        handleDeepLink(event.getBundle());
    }

    private void handleDeepLink(@NonNull Bundle bundle) {
        @ScreenDef final String screenName = bundle.getString(Router.EXTRA_SCREEN_NAME);
        if (screenName == null) {
            return;
        }

        final String pathId = bundle.getString(Router.EXTRA_PATH_ID);
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
                    environment.getRouter().showProgramInfo(getActivity(), pathId);
                    break;
            }
        }
        // Setting this to null, so that upon recreation of the fragment, relevant activity
        // shouldn't be auto created again.
        bundle.putString(Router.EXTRA_SCREEN_NAME, null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}

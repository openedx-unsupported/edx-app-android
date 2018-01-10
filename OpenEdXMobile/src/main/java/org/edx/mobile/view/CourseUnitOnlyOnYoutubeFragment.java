package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.services.ViewPagerDownloadManager;

public class CourseUnitOnlyOnYoutubeFragment extends CourseUnitFragment {

    private String youtubeApiKey;
    private YouTubeMediaPlayerSupportFragment youTubePlayerFragment;
    private boolean startVideoWhileInitializingYoutubeVideo;

    public static CourseUnitOnlyOnYoutubeFragment newInstance(CourseComponent unit, String youtubeApiKey) {
        CourseUnitOnlyOnYoutubeFragment fragment = new CourseUnitOnlyOnYoutubeFragment();
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        args.putString(Router.EXTRA_YOUTUBE_API_KEY, youtubeApiKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        youtubeApiKey = getArguments() == null ? null :
                getArguments().getString(Router.EXTRA_YOUTUBE_API_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_unit_only_on_youtube, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // If youtube fragment is first page in viewPager, being stated that "onPageShow" isn't called.
        // We have to handle the initial play for first youtube fragment ourselves
        if (startVideoWhileInitializingYoutubeVideo)
            initializeYoutubePlayer();
    }

    @Override
    public void setInitialPage() {
        super.setInitialPage();
        startVideoWhileInitializingYoutubeVideo = true;
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        initializeYoutubePlayer();
    }

    @Override
    public void onPageDisappear() {
        super.onPageDisappear();
        // Youtube api allows only one youtube player fragment inside one parent.
        // So in view pager we have to destroy the previous loaded youtube fragment as soon as user goes away.
        getChildFragmentManager().beginTransaction().replace(R.id.youtube_fragment, new Fragment()).commit();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (ViewPagerDownloadManager.instance.inInitialPhase(unit))
            ViewPagerDownloadManager.instance.addTask(this);
    }

    @Override
    public void run() {
        ViewPagerDownloadManager.instance.done(this, true);
    }

    private void initializeYoutubePlayer() {
        String url = ((VideoBlockModel) unit).getData().encodedVideos.youtube.url;
        String videoId = url.split("v=")[1];
        youTubePlayerFragment = YouTubeMediaPlayerSupportFragment.newInstance(youtubeApiKey, videoId);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.youtube_fragment, youTubePlayerFragment).commit();
    }
}

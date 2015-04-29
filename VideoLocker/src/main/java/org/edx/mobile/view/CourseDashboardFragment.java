package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.images.ImageCacheManager;
import org.edx.mobile.view.custom.CourseImageHeader;

public class CourseDashboardFragment extends Fragment {

    protected final Logger logger = new Logger(getClass().getName());
    static public String TAG = CourseHandoutFragment.class.getCanonicalName();
    static public String CourseData = TAG + ".course_data";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_dashboard, container,
                false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        try {
            final Bundle bundle = getArguments();
            final EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) bundle
                    .getSerializable(CourseData);

            if ( courseData == null )
                return;

            Button videoButton = (Button) getView().findViewById(R.id.course_videos_btn);
            videoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Router.getInstance().showCourseAssessment(getActivity(), courseData);
                }
            });

            Button forumButton = (Button) getView().findViewById(R.id.course_forum_btn);
            forumButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            forumButton.setEnabled(MainApplication.ForumEnabled);

            Button handoutButton = (Button) getView().findViewById(R.id.course_handout_btn);
            handoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Router.getInstance().showHandouts(getActivity(), courseData);
                }
            });

            Button announcementButton = (Button) getView().findViewById(R.id.course_announcement_btn);
            announcementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Router.getInstance().showCourseAnnouncement(getActivity(), courseData);
                }
            });

            String headerImageUrl = courseData.getCourse().getCourse_image(getActivity());
            CourseImageHeader headerImageView = (CourseImageHeader)getView().findViewById(R.id.header_image_view);
            headerImageView.setImageUrl(headerImageUrl, ImageCacheManager.getInstance().getImageLoader() );

        } catch (Exception ex) {
            logger.error(ex);
        }
    }

}

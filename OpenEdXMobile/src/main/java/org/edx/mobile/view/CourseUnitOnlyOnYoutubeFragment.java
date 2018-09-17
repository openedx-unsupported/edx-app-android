package org.edx.mobile.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.util.AppConstants;

public class CourseUnitOnlyOnYoutubeFragment extends CourseUnitFragment {

    public static CourseUnitOnlyOnYoutubeFragment newInstance(CourseComponent unit) {
        final CourseUnitOnlyOnYoutubeFragment fragment = new CourseUnitOnlyOnYoutubeFragment();
        final Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View courseUnitOnlyOnYoutubeView = inflater.inflate(R.layout.fragment_course_unit_only_on_youtube, container, false);
        if (environment.getConfig().getEmbeddedYoutubeConfig().isYoutubeEnabled()) {
            ((TextView) courseUnitOnlyOnYoutubeView.findViewById(R.id.only_youtube_available_message)).setText(R.string.assessment_needed_updating_youtube);
            courseUnitOnlyOnYoutubeView.findViewById(R.id.update_youtube_button).setVisibility(View.VISIBLE);
            courseUnitOnlyOnYoutubeView.findViewById(R.id.update_youtube_button).setOnClickListener(v -> {
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.BROWSER_PLAYSTORE_YOUTUBE_URI));
                startActivity(intent);
            });
        }

        courseUnitOnlyOnYoutubeView.findViewById(R.id.view_on_youtube_button).setOnClickListener(v -> {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(((VideoBlockModel) unit).getData().encodedVideos.youtube.url));
            startActivity(intent);
        });

        return courseUnitOnlyOnYoutubeView;
    }
}

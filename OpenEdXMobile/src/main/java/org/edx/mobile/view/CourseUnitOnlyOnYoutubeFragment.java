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

public class CourseUnitOnlyOnYoutubeFragment extends CourseUnitFragment {

    public static CourseUnitOnlyOnYoutubeFragment newInstance(CourseComponent unit) {
        CourseUnitOnlyOnYoutubeFragment fragment = new CourseUnitOnlyOnYoutubeFragment();
        Bundle args = new Bundle();
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
        View v = inflater.inflate(R.layout.fragment_course_unit_only_on_youtube, container, false);
        if (environment.getConfig().getEmbeddedYoutubeConfig().isYoutubeEnabled()) {
            ((TextView) v.findViewById(R.id.only_youtube_available_message)).setText(R.string.assessment_needed_updating_youtube);
            v.findViewById(R.id.update_youtube_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.youtube"));
                        startActivity(i);
                    } catch (android.content.ActivityNotFoundException e) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.youtube"));
                        startActivity(i);
                    }
                }
            });
        } else {
            ((TextView) v.findViewById(R.id.only_youtube_available_message)).setText(R.string.assessment_only_on_youtube);
        }
        v.findViewById(R.id.view_on_youtube_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(((VideoBlockModel) unit).getData().encodedVideos.youtube.url));
                startActivity(i);
            }
        });
        return v;
    }
}

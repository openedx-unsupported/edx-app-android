package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.edx.mobile.R;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class CourseDiscussionFragment extends RoboFragment {
    @InjectView
    ListView discussion_topics_listview;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_topics, container, false);
    }

}

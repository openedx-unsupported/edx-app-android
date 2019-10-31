package org.humana.mobile.tta.ui.course.discussion;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.humana.mobile.R;
import org.humana.mobile.discussion.DiscussionTopicDepth;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.tta.ui.base.TaBaseFragment;
import org.humana.mobile.tta.ui.course.discussion.view_model.DiscussionTopicViewModel;
import org.humana.mobile.tta.utils.BreadcrumbUtil;

public class DiscussionTopicFragment extends TaBaseFragment {
    public static final String TAG = DiscussionTopicFragment.class.getCanonicalName();
    private int RANK;

    private DiscussionTopicViewModel viewModel;

    private DiscussionTopicDepth topicDepth;
    private EnrolledCoursesResponse course;

    public static DiscussionTopicFragment newInstance(EnrolledCoursesResponse course, DiscussionTopicDepth topicDepth){
        DiscussionTopicFragment fragment = new DiscussionTopicFragment();
        fragment.course = course;
        fragment.topicDepth = topicDepth;
        fragment.RANK = BreadcrumbUtil.getCurrentRank() + 1;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new DiscussionTopicViewModel(getActivity(), this, course, topicDepth);
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_discussion_topic, viewModel).getRoot();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}

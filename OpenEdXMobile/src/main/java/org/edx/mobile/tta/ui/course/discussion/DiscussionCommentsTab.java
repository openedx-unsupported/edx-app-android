package org.edx.mobile.tta.ui.course.discussion;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.data.enums.SortType;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.course.discussion.view_model.DiscussionCommentsTabViewModel;
import org.edx.mobile.tta.ui.interfaces.DiscussionCommentClickListener;
import org.edx.mobile.tta.utils.BreadcrumbUtil;

import java.util.List;

public class DiscussionCommentsTab extends TaBaseFragment {
    public static final String TAG = DiscussionCommentsTab.class.getCanonicalName();
    private int RANK;

    private DiscussionCommentsTabViewModel viewModel;

    private EnrolledCoursesResponse course;
    private DiscussionTopic topic;
    private DiscussionThread thread;
    private DiscussionCommentClickListener listener;
    private List<DiscussionComment> comments;
    private SortType sortType;

    public static DiscussionCommentsTab newInstance(EnrolledCoursesResponse course, DiscussionTopic topic,
                                                    DiscussionThread thread, DiscussionCommentClickListener listener,
                                                    List<DiscussionComment> comments, SortType sortType){
        DiscussionCommentsTab fragment = new DiscussionCommentsTab();
        fragment.course = course;
        fragment.topic = topic;
        fragment.thread = thread;
        fragment.listener = listener;
        fragment.sortType = sortType;
        fragment.comments = comments;
        fragment.RANK = BreadcrumbUtil.getCurrentRank() + 1;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new DiscussionCommentsTabViewModel(getActivity(), this, course, topic, thread, listener, comments, sortType);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_discussion_comments_tab, viewModel).getRoot();

        return view;
    }

    public void refreshList() {
        if (viewModel != null) {
            viewModel.refreshList();
        }
    }

    public void setLoaded(){
        if (viewModel != null){
            viewModel.setLoaded();
        }
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        Nav nav;
        switch (sortType){
            case all:
                nav = Nav.all;
                break;
            case recent:
                nav = Nav.recently_added;
                break;
            default:
                nav = Nav.most_relevant;
        }
        logger.debug("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, nav.name()));
    }
}

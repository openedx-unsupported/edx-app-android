package org.edx.mobile.tta.ui.course.discussion;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.course.discussion.view_model.DiscussionCommentViewModel;

public class DiscussionCommentActivity extends BaseVMActivity {

    private DiscussionCommentViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new DiscussionCommentViewModel(this);
        binding(R.layout.t_activity_discussion_comment, viewModel);
    }
}

package org.edx.mobile.tta.ui.course.discussion.view_model;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.v4.app.FragmentManager;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.tta.ui.base.BasePagerAdapter;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;

public class DiscussionThreadViewModel extends BaseViewModel {

    public EnrolledCoursesResponse course;
    public DiscussionTopic topic;
    public DiscussionThread thread;

    public ObservableField<String> threadDate = new ObservableField<>();
    public ObservableInt likeIcon = new ObservableInt(R.drawable.t_icon_like);
    public ObservableBoolean replyingToVisible = new ObservableBoolean();
    public ObservableBoolean commentFocus = new ObservableBoolean();
    public ObservableField<String> replyingToText = new ObservableField<>();
    public ObservableField<String> comment = new ObservableField<>();

    public CommentsPagerAdapter adapter;

    public DiscussionThreadViewModel(BaseVMActivity activity, EnrolledCoursesResponse course, DiscussionTopic topic, DiscussionThread thread) {
        super(activity);
        this.course = course;
        this.topic = topic;
        this.thread = thread;
    }

    public void shareThread(){

    }

    public void likeThread(){

    }

    public void addComment(){

    }

    public void resetReplyToComment(){

    }

    public void addReplyToComment(){

    }

    public class CommentsPagerAdapter extends BasePagerAdapter {
        public CommentsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
    }
}

package org.edx.mobile.tta.ui.course.discussion.view_model;

import android.content.Context;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowDiscussionThreadBinding;
import org.edx.mobile.discussion.DiscussionPostsSort;
import org.edx.mobile.discussion.DiscussionRequestFields;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionThreadPostedEvent;
import org.edx.mobile.discussion.DiscussionTopicDepth;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.course.discussion.DiscussionThreadActivity;
import org.edx.mobile.tta.ui.profile.OtherProfileActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.user.ProfileImage;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.view.DiscussionAddPostActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

public class DiscussionTopicViewModel extends BaseViewModel {

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_PAGE = 1;

    public DiscussionTopicDepth topicDepth;
    private EnrolledCoursesResponse course;
    private List<DiscussionThread> threads;

    public DiscussionThreadsAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    private int take, page;
    private boolean allLoaded;

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.page++;
        fetchThreads();
        return true;
    };

    public DiscussionTopicViewModel(Context context, TaBaseFragment fragment, EnrolledCoursesResponse course, DiscussionTopicDepth topicDepth) {
        super(context, fragment);
        this.course = course;
        this.topicDepth = topicDepth;
        threads = new ArrayList<>();
        take = DEFAULT_TAKE;
        page = DEFAULT_PAGE;
        allLoaded = false;

        adapter = new DiscussionThreadsAdapter(mActivity);
        adapter.setItemClickListener((view, item) -> {
            switch (view.getId()){
                case R.id.user_image:
                case R.id.user_name:
                    Bundle parameter = new Bundle();
                    parameter.putString(Constants.KEY_USERNAME, item.getAuthor());
                    ActivityUtil.gotoPage(mActivity, OtherProfileActivity.class, parameter);
                    break;

                default:
                    Bundle parameters = new Bundle();
                    parameters.putSerializable(Constants.KEY_ENROLLED_COURSE, course);
                    parameters.putSerializable(Constants.KEY_DISCUSSION_TOPIC, topicDepth.getDiscussionTopic());
                    parameters.putSerializable(Constants.KEY_DISCUSSION_THREAD, item);
                    ActivityUtil.gotoPage(mActivity, DiscussionThreadActivity.class, parameters);
            }
        });
        adapter.setItems(threads);

        mActivity.showLoading();
        fetchThreads();
    }

    private void fetchThreads() {

        mDataManager.getDiscussionThreads(course.getCourse().getId(),
                Collections.singletonList(topicDepth.getDiscussionTopic().getIdentifier()),
                null, DiscussionPostsSort.LAST_ACTIVITY_AT.name().toLowerCase(), take, page,
                Collections.singletonList(DiscussionRequestFields.PROFILE_IMAGE.getQueryParamValue()),
                new OnResponseCallback<List<DiscussionThread>>() {
                    @Override
                    public void onSuccess(List<DiscussionThread> data) {
                        mActivity.hideLoading();
                        if (data.size() < take){
                            allLoaded = true;
                        }
                        populateThreads(data);
                        adapter.setLoadingDone();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        allLoaded = true;
                        adapter.setLoadingDone();
                    }
                });

    }

    private void populateThreads(List<DiscussionThread> data) {
        boolean newItemsAdded = false;
        int n = 0;
        for (DiscussionThread thread: data){
            if (!threads.contains(thread)){
                threads.add(thread);
                newItemsAdded = true;
                n++;
            }
        }
        if (newItemsAdded) {
            adapter.notifyItemRangeInserted(threads.size() - n, n);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new LinearLayoutManager(mActivity);
    }

    public void startDiscussion(){
        mDataManager.getEdxEnvironment().getRouter().showCourseDiscussionAddPost(mActivity, topicDepth.getDiscussionTopic(), course);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DiscussionThreadPostedEvent event) {
        DiscussionThread newThread = event.getDiscussionThread();
        if (topicDepth.getDiscussionTopic().getIdentifier().equals(newThread.getTopicId())) {
            adapter.add(0, newThread);
        }
    }

    public void registerEventBus(){
        EventBus.getDefault().register(this);
    }

    public void unRegisterEventBus(){
        EventBus.getDefault().unregister(this);
    }

    public class DiscussionThreadsAdapter extends MxInfiniteAdapter<DiscussionThread> {
        public DiscussionThreadsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull DiscussionThread model, @Nullable OnRecyclerItemClickListener<DiscussionThread> listener) {
            if (binding instanceof TRowDiscussionThreadBinding){
                TRowDiscussionThreadBinding threadBinding = (TRowDiscussionThreadBinding) binding;
                threadBinding.setViewModel(model);

                threadBinding.userName.setText(model.getAuthorDisplayName() == null ?
                        mDataManager.getLoginPrefs().getDisplayName() : model.getAuthorDisplayName());
                threadBinding.date.setText(DateUtil.getDisplayTime(model.getUpdatedAt()));

                ProfileImage profileImage = model.getProfileImage();
                if (profileImage == null){
                    profileImage = mDataManager.getLoginPrefs().getProfileImage();
                }
                if (profileImage != null) {
                    Glide.with(getContext())
                            .load(profileImage.getImageUrlMedium())
                            .placeholder(R.drawable.profile_photo_placeholder)
                            .into(threadBinding.roundedUserImage);
                } else {
                    threadBinding.roundedUserImage.setImageResource(R.drawable.profile_photo_placeholder);
                }

                threadBinding.userImage.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                threadBinding.userName.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                threadBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }
}

package org.edx.mobile.tta.ui.course.discussion.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowDiscussionThreadBinding;
import org.edx.mobile.databinding.TRowDiscussionTopicBinding;
import org.edx.mobile.discussion.DiscussionPostsSort;
import org.edx.mobile.discussion.DiscussionRequestFields;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionThreadPostedEvent;
import org.edx.mobile.discussion.DiscussionTopicDepth;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.event.ShowDiscussionTopicEvent;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.course.discussion.DiscussionThreadActivity;
import org.edx.mobile.tta.ui.course.discussion.DiscussionTopicFragment;
import org.edx.mobile.tta.ui.profile.OtherProfileActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.user.ProfileImage;
import org.edx.mobile.util.DateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class DiscussionLandingViewModel extends BaseViewModel {

    private EnrolledCoursesResponse course;
    private Map<String, List<DiscussionThread>> topicThreadsMap;
    private List<DiscussionTopicDepth> topics;

    public DiscussionTopicsAdapter topicsAdapter;
    public RecyclerView.LayoutManager topicsLayoutManager;

    public ObservableBoolean progressVisible = new ObservableBoolean();

    public DiscussionLandingViewModel(Context context, TaBaseFragment fragment, EnrolledCoursesResponse course) {
        super(context, fragment);
        this.course = course;
        topicThreadsMap = new HashMap<>();

        topicsAdapter = new DiscussionTopicsAdapter(mActivity);
        topicsAdapter.setItemClickListener((view, item) -> {
            switch (view.getId()){
                case R.id.see_more_btn:
                    if (topicThreadsMap.containsKey(item.getDiscussionTopic().getIdentifier())) {
                        ActivityUtil.replaceFragmentInActivity(
                                mActivity.getSupportFragmentManager(),
                                DiscussionTopicFragment.newInstance(course, item),
                                R.id.discussion_tab,
                                DiscussionTopicFragment.TAG,
                                true, null
                        );
                    } else {
                        mDataManager.getEdxEnvironment().getRouter().showCourseDiscussionAddPost(mActivity, item.getDiscussionTopic(), course);
                    }
                    break;
            }
        });

        fetchDiscussionTopics();
    }

    @Override
    public void onResume() {
        super.onResume();
        topicsLayoutManager = new LinearLayoutManager(mActivity);
    }

    private void fetchDiscussionTopics() {
        progressVisible.set(true);

        if (course != null) {
            mDataManager.getDiscussionTopics(course.getCourse().getId(), new OnResponseCallback<List<DiscussionTopicDepth>>() {
                @Override
                public void onSuccess(List<DiscussionTopicDepth> data) {
                    topics = data;

                    for (DiscussionTopicDepth topic : data) {

                        mDataManager.getDiscussionThreads(course.getCourse().getId(),
                                Collections.singletonList(topic.getDiscussionTopic().getIdentifier()),
                                null,
                                DiscussionPostsSort.LAST_ACTIVITY_AT.name().toLowerCase(), 3, 1,
                                Collections.singletonList(DiscussionRequestFields.PROFILE_IMAGE.getQueryParamValue()),
                                new OnResponseCallback<List<DiscussionThread>>() {
                                    @Override
                                    public void onSuccess(List<DiscussionThread> data) {
                                        progressVisible.set(false);

                                        for (DiscussionThread thread: data){
                                            if (topicThreadsMap.containsKey(thread.getTopicId())){
                                                topicThreadsMap.get(thread.getTopicId()).add(thread);
                                            } else {
                                                List<DiscussionThread> threads = new ArrayList<>();
                                                threads.add(thread);
                                                topicThreadsMap.put(thread.getTopicId(), threads);
                                            }
                                        }

                                        populateTopicsList();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        progressVisible.set(false);
                                    }
                                });

                    }

                    populateTopicsList();
                }

                @Override
                public void onFailure(Exception e) {
                    progressVisible.set(false);
                    mActivity.showLongSnack(e.getLocalizedMessage());
                }
            });
        }

    }

    private void populateTopicsList() {
        topicsAdapter.setItems(topics);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DiscussionThreadPostedEvent event) {
        DiscussionThread newThread = event.getDiscussionThread();
        if (topicThreadsMap.containsKey(newThread.getTopicId())){
            topicThreadsMap.get(newThread.getTopicId()).add(0, newThread);
        } else {
            List<DiscussionThread> threads = new ArrayList<>();
            threads.add(newThread);
            topicThreadsMap.put(newThread.getTopicId(), threads);
        }
        topicsAdapter.notifyDataSetChanged();
    }

    public void registerEventBus(){
        EventBus.getDefault().register(this);
    }

    public void unRegisterEventBus(){
        EventBus.getDefault().unregister(this);
    }

    public class DiscussionTopicsAdapter extends MxInfiniteAdapter<DiscussionTopicDepth> {
        public DiscussionTopicsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull DiscussionTopicDepth model, @Nullable OnRecyclerItemClickListener<DiscussionTopicDepth> listener) {
            if (binding instanceof TRowDiscussionTopicBinding) {
                TRowDiscussionTopicBinding topicBinding = (TRowDiscussionTopicBinding) binding;
                topicBinding.setViewModel(model.getDiscussionTopic());

                topicBinding.seeMoreBtn.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                if (topicThreadsMap.containsKey(model.getDiscussionTopic().getIdentifier())){
                    topicBinding.seeMoreBtn.setText(mActivity.getString(R.string.see_more));
                    DiscussionThreadsAdapter threadsAdapter = new DiscussionThreadsAdapter(getContext());
                    threadsAdapter.setItemLayout(R.layout.t_row_discussion_thread);
                    threadsAdapter.setItems(topicThreadsMap.get(model.getDiscussionTopic().getIdentifier()));
                    threadsAdapter.setItemClickListener((view, item) -> {
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
                                parameters.putSerializable(Constants.KEY_DISCUSSION_TOPIC, model.getDiscussionTopic());
                                parameters.putSerializable(Constants.KEY_DISCUSSION_THREAD, item);
                                ActivityUtil.gotoPage(mActivity, DiscussionThreadActivity.class, parameters);
                        }
                    });
                    topicBinding.limitedThreadsList.setLayoutManager(new LinearLayoutManager(getContext()));
                    topicBinding.limitedThreadsList.setAdapter(threadsAdapter);
                } else {
                    topicBinding.seeMoreBtn.setText(mActivity.getString(R.string.start_discussion));
                }
            }
        }
    }

    public class DiscussionThreadsAdapter extends MxFiniteAdapter<DiscussionThread> {
        /**
         * Base constructor.
         * Allocate adapter-related objects here if needed.
         *
         * @param context Context needed to retrieve LayoutInflater
         */
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

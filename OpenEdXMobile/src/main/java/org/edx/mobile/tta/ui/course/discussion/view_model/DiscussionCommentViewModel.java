package org.edx.mobile.tta.ui.course.discussion.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
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
import org.edx.mobile.databinding.TRowDiscussionReplyBinding;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionRequestFields;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.analytics.analytics_enums.Action;
import org.edx.mobile.tta.analytics.analytics_enums.DiscussionTopicType;
import org.edx.mobile.tta.analytics.analytics_enums.Source;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.profile.OtherProfileActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.user.ProfileImage;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.NetworkUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

public class DiscussionCommentViewModel extends BaseViewModel {

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_PAGE = 1;

    public EnrolledCoursesResponse course;
    public DiscussionTopic topic;
    public DiscussionThread thread;
    public DiscussionComment comment;
    private List<DiscussionComment> replies;

    public ObservableField<String> userImage = new ObservableField<>();
    public ObservableInt userPlaceholder = new ObservableInt(R.drawable.profile_photo_placeholder);
    public ObservableField<String> commentDate = new ObservableField<>();
    public ObservableField<String> reply = new ObservableField<>();
    public ObservableField<String> likeCount = new ObservableField<>("0");
    public ObservableField<String> commentsCount = new ObservableField<>("0");
    public ObservableInt likeIcon = new ObservableInt(R.drawable.t_icon_like);
    public ObservableBoolean commentFocus = new ObservableBoolean();
    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public ObservableBoolean offlineVisible = new ObservableBoolean();

    public DiscussionRepliesAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    private int take, page;
    private boolean allLoaded;

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.page++;
        fetchReplies();
        return true;
    };

    public DiscussionCommentViewModel(BaseVMActivity activity, EnrolledCoursesResponse course, DiscussionTopic topic, DiscussionThread thread, DiscussionComment comment) {
        super(activity);
        this.course = course;
        this.topic = topic;
        this.thread = thread;
        this.comment = comment;
        replies = new ArrayList<>();
        take = DEFAULT_TAKE;
        page = DEFAULT_PAGE;
        allLoaded = false;

        userImage.set(comment.getProfileImage() == null ?
                (mDataManager.getLoginPrefs().getProfileImage() == null ? "" :
                        mDataManager.getLoginPrefs().getProfileImage().getImageUrlMedium()) :
                comment.getProfileImage().getImageUrlMedium());
        commentDate.set(DateUtil.getDisplayTime(comment.getUpdatedAt()));
        likeIcon.set(comment.isVoted() ? R.drawable.t_icon_like_filled : R.drawable.t_icon_like);
        likeCount.set(String.valueOf(comment.getVoteCount()));
        commentsCount.set(String.valueOf(comment.getChildCount()));

        adapter = new DiscussionRepliesAdapter(mActivity);
        adapter.setItems(replies);
        adapter.setItemClickListener((view, item) -> {
            switch (view.getId()){
                case R.id.reply_comment_like_layout:
                    likeReply(item);
                    break;

                case R.id.reply_user_name:
                case R.id.reply_user_image:
                    Bundle parameters = new Bundle();
                    parameters.putString(Constants.KEY_USERNAME, item.getAuthor());
                    ActivityUtil.gotoPage(mActivity, OtherProfileActivity.class, parameters);
                    break;
            }
        });

        mActivity.showLoading();
        fetchReplies();
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new LinearLayoutManager(mActivity);
        onEventMainThread(new NetworkConnectivityChangeEvent());
    }

    private void fetchReplies() {

        final List<String> requestedFields = Collections.singletonList(
                DiscussionRequestFields.PROFILE_IMAGE.getQueryParamValue());

        mDataManager.getCommentReplies(comment.getIdentifier(), take, page, requestedFields,
                new OnResponseCallback<List<DiscussionComment>>() {
                    @Override
                    public void onSuccess(List<DiscussionComment> data) {
                        mActivity.hideLoading();
                        if (data.size() < take){
                            allLoaded = true;
                        }
                        populateReplies(data);
                        adapter.setLoadingDone();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        allLoaded = true;
                        adapter.setLoadingDone();
                        toggleEmptyVisibility();
                    }
                });

    }

    private void populateReplies(List<DiscussionComment> data) {
        boolean newItemsAdded = false;

        for (DiscussionComment reply: data){
            if (!replies.contains(reply)){
                replies.add(reply);
                newItemsAdded = true;
            }
        }

        if (newItemsAdded) {
            adapter.notifyDataSetChanged();
        }

        toggleEmptyVisibility();
    }

    private void toggleEmptyVisibility(){
        if (replies == null || replies.isEmpty()){
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    public void onClickThreadUser(){
        Bundle parameters = new Bundle();
        parameters.putString(Constants.KEY_USERNAME, comment.getAuthor());
        ActivityUtil.gotoPage(mActivity, OtherProfileActivity.class, parameters);
    }

    public void likeComment(){
        mActivity.showLoading();
        mDataManager.likeDiscussionComment(comment.getIdentifier(), !comment.isVoted(),
                new OnResponseCallback<DiscussionComment>() {
                    @Override
                    public void onSuccess(DiscussionComment data) {
                        mActivity.hideLoading();
                        comment.setVoted(data.isVoted());
                        comment.setVoteCount(data.getVoteCount());
                        likeCount.set(String.valueOf(comment.getVoteCount()));
                        likeIcon.set(comment.isVoted() ? R.drawable.t_icon_like_filled : R.drawable.t_icon_like);

                        mActivity.analytic.addMxAnalytics_db(thread.getIdentifier(),
                                data.isVoted() ? Action.DBCommentlike : Action.DBCommentUnlike,
                                course.getCourse().getName(),
                                Source.Mobile, comment.getIdentifier());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        mActivity.showLongSnack(e.getLocalizedMessage());
                    }
                });
    }

    public void addComment(){
        if (commentFocus.get()) {
            commentFocus.set(false);
        }
        commentFocus.set(true);
    }

    public void comment(){
        String reply = this.reply.get();
        if (reply == null || reply.trim().equals("")){
            mActivity.showShortToast("Reply cannot be empty");
            return;
        }

        mActivity.showLoading();
        mDataManager.createDiscussionComment(thread.getIdentifier(), reply.trim(), comment.getIdentifier(),
                new OnResponseCallback<DiscussionComment>() {
                    @Override
                    public void onSuccess(DiscussionComment data) {
                        mActivity.hideLoading();
                        adapter.add(0, data);
                        comment.incrementChildCount();
                        commentsCount.set(String.valueOf(comment.getChildCount()));
                        DiscussionCommentViewModel.this.reply.set("");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        mActivity.showLongSnack(e.getLocalizedMessage());
                    }
                });
    }

    private void likeReply(DiscussionComment reply){
        mActivity.showLoading();
        mDataManager.likeDiscussionComment(reply.getIdentifier(), !reply.isVoted(),
                new OnResponseCallback<DiscussionComment>() {
                    @Override
                    public void onSuccess(DiscussionComment data) {
                        mActivity.hideLoading();
                        reply.setVoted(data.isVoted());
                        reply.setVoteCount(data.getVoteCount());
                        adapter.notifyItemChanged(adapter.getItemPosition(reply));
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        mActivity.showLongSnack(e.getLocalizedMessage());
                    }
                });

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NetworkConnectivityChangeEvent event){
        if (NetworkUtil.isConnected(mActivity)){
            offlineVisible.set(false);
        } else {
            offlineVisible.set(true);
        }
    }

    public void registerEventBus(){
        EventBus.getDefault().register(this);
    }

    public void unRegisterEventBus(){
        EventBus.getDefault().unregister(this);
    }

    public class DiscussionRepliesAdapter extends MxInfiniteAdapter<DiscussionComment> {
        public DiscussionRepliesAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull DiscussionComment model, @Nullable OnRecyclerItemClickListener<DiscussionComment> listener) {
            if (binding instanceof TRowDiscussionReplyBinding){
                TRowDiscussionReplyBinding replyBinding = (TRowDiscussionReplyBinding) binding;
                replyBinding.setViewModel(model);

                ProfileImage profileImage = model.getProfileImage();
                if (profileImage == null){
                    profileImage = mDataManager.getLoginPrefs().getProfileImage();
                }
                if (profileImage != null) {
                    Glide.with(getContext())
                            .load(profileImage.getImageUrlMedium())
                            .placeholder(R.drawable.profile_photo_placeholder)
                            .into(replyBinding.replyRoundedUserImage);
                } else {
                    replyBinding.replyRoundedUserImage.setImageResource(R.drawable.profile_photo_placeholder);
                }

                replyBinding.replyDate.setText(DateUtil.getDisplayTime(model.getUpdatedAt()));

                replyBinding.replyCommentLikeImage.setImageResource(
                        model.isVoted() ? R.drawable.t_icon_like_filled : R.drawable.t_icon_like
                );

                replyBinding.replyCommentLikeLayout.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                replyBinding.replyUserImage.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                replyBinding.replyUserName.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }
}

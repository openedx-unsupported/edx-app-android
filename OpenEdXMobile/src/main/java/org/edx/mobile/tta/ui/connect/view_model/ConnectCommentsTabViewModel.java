package org.edx.mobile.tta.ui.connect.view_model;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowCommentsBinding;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.interfaces.CommentClickListener;
import org.edx.mobile.tta.wordpress_client.model.Comment;
import org.edx.mobile.tta.wordpress_client.model.Post;
import org.edx.mobile.util.DateUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConnectCommentsTabViewModel extends BaseViewModel {

    private Content content;
    private Post post;
    private List<Comment> comments;

    public CommentsAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;
    private CommentClickListener commentClickListener;

    public ConnectCommentsTabViewModel(Context context, TaBaseFragment fragment, Content content, Post post, List<Comment> comments, CommentClickListener commentClickListener) {
        super(context, fragment);
        this.content = content;
        this.post = post;
        this.comments = comments;
        this.commentClickListener = commentClickListener;
        adapter = new CommentsAdapter(mActivity);
        setListeners();
        layoutManager = new LinearLayoutManager(mActivity);
        adapter.setItems(comments);
    }

    private void setListeners() {

        adapter.setItemClickListener((view, item) -> {
            switch (view.getId()){
                case R.id.comment_like_layout:
                    if (commentClickListener != null){
                        commentClickListener.onClickLike(item);
                    }
                    break;

                case R.id.comment_reply_layout:
                    if (commentClickListener != null){
                        commentClickListener.onClickReply(item);
                    }
                    break;

                case R.id.user_image:
                case R.id.user_name:
                    if (commentClickListener != null){
                        commentClickListener.onClickUser(item);
                    }
                    break;
            }
        });

    }

    public void refreshList(){
        adapter.notifyDataSetChanged();
    }

    public class CommentsAdapter extends MxInfiniteAdapter<Comment> {
        public CommentsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Comment model, @Nullable OnRecyclerItemClickListener<Comment> listener) {
            if (binding instanceof TRowCommentsBinding){
                TRowCommentsBinding commentsBinding = (TRowCommentsBinding) binding;

                Glide.with(commentsBinding.userImage.getContext())
                        .load(content.getIcon())
                        .placeholder(R.drawable.placeholder_course_card_image)
                        .into(commentsBinding.roundedUserImage);

                commentsBinding.userName.setText(model.getAuthorName());
                commentsBinding.date.setText(DateUtil.getDisplayTime(model.getDate()));
                commentsBinding.comment.setText(Html.fromHtml(model.getContent().getRendered()));
                commentsBinding.commentLikes.setText("80");
                commentsBinding.commentReplies.setText("5");

                commentsBinding.commentLikeLayout.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                commentsBinding.commentReplyLayout.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                commentsBinding.userImage.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                commentsBinding.userName.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

            }
        }
    }
}

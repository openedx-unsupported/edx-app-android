package org.edx.mobile.tta.ui.connect.view_model;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowCommentsBinding;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

public class ConnectCommentsTabViewModel extends BaseViewModel {

    private Content content;

    public CommentsAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    public ConnectCommentsTabViewModel(Context context, TaBaseFragment fragment, Content content) {
        super(context, fragment);
        this.content = content;
        adapter = new CommentsAdapter(mActivity);
        layoutManager = new LinearLayoutManager(mActivity);
        loadData();
    }

    private void loadData() {
        List<Content> contents = new ArrayList<>();
        for (int i = 0; i < 5; i++){
            contents.add(new Content());
        }
        adapter.setItems(contents);
    }

    public class CommentsAdapter extends MxInfiniteAdapter<Content> {
        public CommentsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Content model, @Nullable OnRecyclerItemClickListener<Content> listener) {
            if (binding instanceof TRowCommentsBinding){
                TRowCommentsBinding commentsBinding = (TRowCommentsBinding) binding;

                Glide.with(commentsBinding.userImage.getContext())
                        .load(content.getIcon())
                        .placeholder(R.drawable.placeholder_course_card_image)
                        .into(commentsBinding.roundedUserImage);

                commentsBinding.userName.setText("User Name");
                commentsBinding.date.setText("19 Feb 2019");
                commentsBinding.comment.setText(
                        "अगर हम एक एक कर के सभी छात्रों का निवारन लें तो हम यह ज्ञात कर" +
                                " सकते हैं की हम किस प्रकार एक जुट होकर एक सलूशन निकले जिस्सेय " +
                                "हमें और छात्रों को आसानी रहे सब सिखने में");
                commentsBinding.commentLikes.setText("80");
                commentsBinding.commentReplies.setText("5");

            }
        }
    }
}

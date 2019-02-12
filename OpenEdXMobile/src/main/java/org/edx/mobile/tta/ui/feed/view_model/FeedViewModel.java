package org.edx.mobile.tta.ui.feed.view_model;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowFeedLikeBinding;
import org.edx.mobile.tta.data.local.db.table.Feed;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;

public class FeedViewModel extends BaseViewModel {

    public FeedAdapter feedAdapter;

    public FeedViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
        feedAdapter = new FeedAdapter(context);
        getFeeds();
    }

    private void getFeeds() {

    }

    public void showNotifications(){

    }

    public class FeedAdapter extends MxInfiniteAdapter<Feed> {
        public FeedAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Feed model, @Nullable OnRecyclerItemClickListener<Feed> listener) {
            if (binding instanceof TRowFeedLikeBinding){
                TRowFeedLikeBinding likeBinding = (TRowFeedLikeBinding) binding;
//                likeBinding.feedTitle.setText();
            }
        }

        @Override
        public int getItemLayout(int position) {
            return R.layout.t_row_feed_like;
        }
    }
}

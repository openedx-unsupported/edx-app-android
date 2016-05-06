package org.edx.mobile.profiles;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import org.edx.mobile.R;
import org.edx.mobile.databinding.AccomplishmentItemBinding;
import org.edx.mobile.view.adapters.CourseDiscussionResponsesAdapter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;

import java.util.ArrayList;
import java.util.List;

public class AccomplishmentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements InfiniteScrollUtils.ListContentController<BadgeAssertion> {

    @NonNull
    private final List<BadgeAssertion> items = new ArrayList<>();

    @NonNull
    private final String imageUrlPrefix;

    @NonNull
    private final Listener listener;

    private boolean isProgressVisible = false;

    public AccomplishmentListAdapter(@NonNull String imageUrlPrefix, @NonNull Listener listener) {
        this.imageUrlPrefix = imageUrlPrefix;
        this.listener = listener;
    }

    static class RowType {
        static final int ITEM = 0;
        static final int PROGRESS = 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case RowType.ITEM:
                return new ViewHolder(parent);
            case RowType.PROGRESS: {
                return new CourseDiscussionResponsesAdapter.ShowMoreViewHolder(LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.list_view_footer_progress, parent, false));
            }
            default: {
                throw new IllegalArgumentException(String.valueOf(viewType));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isProgressVisible && position == getItemCount() - 1) {
            return RowType.PROGRESS;
        }
        return RowType.ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case RowType.ITEM: {
                ((ViewHolder) holder).setContent(items.get(position));
                break;
            }
            case RowType.PROGRESS: {
                // Don't need to do anything here
                break;
            }
            default: {
                throw new IllegalArgumentException(String.valueOf(viewType));
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size() + (isProgressVisible ? 1 : 0);
    }

    public void clear() {
        final int size = items.size();
        items.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addAll(@NonNull List<BadgeAssertion> newItems) {
        final int oldSize = items.size();
        items.addAll(newItems);
        notifyItemRangeInserted(oldSize, newItems.size());
    }

    @Override
    public void setProgressVisible(boolean visible) {
        if (visible != isProgressVisible) {
            isProgressVisible = visible;
            if (isProgressVisible) {
                notifyItemInserted(getItemCount() - 1);
            } else {
                notifyItemRemoved(getItemCount());
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        public final AccomplishmentItemBinding binding;

        public ViewHolder(@NonNull ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.accomplishment_item, parent, false));
            binding = DataBindingUtil.bind(itemView);
        }

        public void setContent(@NonNull final BadgeAssertion badgeAssertion) {
            Glide.with(itemView.getContext()).load(imageUrlPrefix + badgeAssertion.getImageUrl()).into(binding.image);
            binding.name.setText(badgeAssertion.getBadgeClass().getDisplayName());
            binding.description.setText(badgeAssertion.getBadgeClass().getDescription());
            binding.date.setText(DateUtils.formatDateTime(itemView.getContext(), badgeAssertion.getCreated().getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
            binding.share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onShare(badgeAssertion);
                }
            });
        }
    }

    public interface Listener {
        void onShare(@NonNull BadgeAssertion badgeAssertion);
    }
}

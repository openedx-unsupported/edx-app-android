package org.edx.mobile.profiles;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import org.edx.mobile.R;
import org.edx.mobile.databinding.AccomplishmentItemBinding;
import org.edx.mobile.view.adapters.LoadingViewHolder;

import java.util.ArrayList;
import java.util.List;

public class AccomplishmentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @NonNull
    private final List<BadgeAssertion> items = new ArrayList<>();

    @NonNull
    private final String imageUrlPrefix;

    @NonNull
    private final Listener listener;

    private boolean isProgressVisible = false;

    private boolean isSharingEnabled = false;

    public AccomplishmentListAdapter(@NonNull String imageUrlPrefix, @NonNull Listener listener) {
        this.imageUrlPrefix = imageUrlPrefix;
        this.listener = listener;
    }

    public void setSharingEnabled(boolean enableSharing) {
        isSharingEnabled = enableSharing;
        notifyDataSetChanged();
    }

    @VisibleForTesting
    public static class RowType {
        static final int ITEM = 0;
        static final int PROGRESS = 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case RowType.ITEM:
                return createItemViewHolder(parent);
            case RowType.PROGRESS: {
                return createProgressViewHolder(parent);
            }
            default: {
                throw new IllegalArgumentException(String.valueOf(viewType));
            }
        }
    }

    @NonNull
    @VisibleForTesting
    protected ItemViewHolder createItemViewHolder(@NonNull ViewGroup parent) {
        return new ItemViewHolder(parent);
    }

    @NonNull
    @VisibleForTesting
    protected LoadingViewHolder createProgressViewHolder(@NonNull ViewGroup parent) {
        return new LoadingViewHolder(parent);
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
                ((ItemViewHolder) holder).setContent(items.get(position), isSharingEnabled);
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

    public void setItems(@NonNull List<BadgeAssertion> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setPageLoading(boolean visible) {
        if (visible != isProgressVisible) {
            isProgressVisible = visible;
            notifyDataSetChanged();
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        public final AccomplishmentItemBinding binding;

        public ItemViewHolder(@NonNull ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.accomplishment_item, parent, false));
            binding = DataBindingUtil.bind(itemView);
        }

        public void setContent(@NonNull final BadgeAssertion badgeAssertion, final boolean sharingEnabled) {
            Glide.with(itemView.getContext()).load(imageUrlPrefix + badgeAssertion.getImageUrl()).into(binding.image);
            binding.name.setText(badgeAssertion.getBadgeClass().getDisplayName());
            binding.description.setText(badgeAssertion.getBadgeClass().getDescription());
            binding.date.setText(DateUtils.formatDateTime(itemView.getContext(), badgeAssertion.getCreated().getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
            if (sharingEnabled) {
                binding.share.setVisibility(View.VISIBLE);
                binding.share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onShare(badgeAssertion);
                    }
                });
            } else {
                binding.share.setVisibility(View.GONE);
            }
        }
    }

    public interface Listener {
        void onShare(@NonNull BadgeAssertion badgeAssertion);
    }
}

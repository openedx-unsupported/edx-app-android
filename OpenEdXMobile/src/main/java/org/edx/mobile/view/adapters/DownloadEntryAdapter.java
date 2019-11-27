package org.edx.mobile.view.adapters;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.util.MemoryUtil;

public abstract class DownloadEntryAdapter extends BaseListAdapter<DownloadEntryAdapter.Item> {

    public DownloadEntryAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_download_list, environment);
    }

    @Override
    public void render(BaseViewHolder tag, final Item item) {
        final ViewHolder holder = (ViewHolder) tag;
        holder.title.setText(item.getTitle());
        if (TextUtils.isEmpty(item.getDuration())) {
            holder.duration.setVisibility(View.GONE);
        } else {
            holder.duration.setVisibility(View.VISIBLE);
            holder.duration.setText(item.getDuration());
        }
        holder.progress.setProgress(item.getPercent());
        @DrawableRes
        final int progressDrawable;
        final String progressText;
        final String errorText;
        switch (item.getStatus()) {
            case PENDING: {
                progressText = getContext().getString(R.string.download_pending);
                progressDrawable = R.drawable.custom_progress_bar_horizontal_success;
                errorText = null;
                break;
            }
            case DOWNLOADING: {
                progressText = getByteCountProgressText(item);
                progressDrawable = R.drawable.custom_progress_bar_horizontal_success;
                errorText = null;
                break;
            }
            case FAILED: {
                errorText = getContext().getString(R.string.error_download_failed);
                progressDrawable = R.drawable.custom_progress_bar_horizontal_error;
                if (item.getDownloadedByteCount() > 0) {
                    progressText = getByteCountProgressText(item);
                } else {
                    progressText = null;
                }
                break;
            }
            default: {
                throw new IllegalArgumentException(item.getStatus().name());
            }
        }
        holder.progress
                .setProgressDrawable(getContext()
                        .getResources()
                        .getDrawable(progressDrawable));
        if (null == progressText) {
            holder.percent.setVisibility(View.GONE);
        } else {
            holder.percent.setText(progressText);
            holder.percent.setVisibility(View.VISIBLE);
        }
        if (null == errorText) {
            holder.error.setVisibility(View.GONE);
        } else {
            holder.error.setText(errorText);
            holder.error.setVisibility(View.VISIBLE);
        }

        holder.cross_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteClicked(item);
            }
        });
    }

    @NonNull
    private String getByteCountProgressText(Item item) {
        final Long totalByteCount = item.getTotalByteCount();
        String downloadedText = MemoryUtil.format(getContext(), item.getDownloadedByteCount());
        if (null != totalByteCount) {
            downloadedText += " / " + MemoryUtil.format(getContext(), totalByteCount);
        }
        return downloadedText;
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        return new ViewHolder(convertView);
    }

    private static class ViewHolder extends BaseViewHolder {
        final TextView title;
        final TextView duration;
        final TextView percent;
        final ImageView cross_button;
        final TextView error;
        final ProgressBar progress;

        public ViewHolder(@NonNull View view) {
            title = (TextView) view.findViewById(R.id.downloads_name);
            duration = (TextView) view
                    .findViewById(R.id.download_time);
            percent = (TextView) view
                    .findViewById(R.id.download_percentage);
            error = (TextView) view
                    .findViewById(R.id.txtDownloadFailed);
            progress = (ProgressBar) view
                    .findViewById(R.id.progressBar);
            cross_button = (ImageView) view
                    .findViewById(R.id.close_btn);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        DownloadEntryAdapter.Item item = getItem(position);
        if (item != null) onItemClicked(item);
    }

    public abstract void onItemClicked(DownloadEntryAdapter.Item model);

    public abstract void onDeleteClicked(DownloadEntryAdapter.Item model);

    public interface Item {
        @NonNull
        String getTitle();

        @NonNull
        String getDuration();

        @NonNull
        Status getStatus();

        /**
         * @return Total download size in bytes, or null if size is not yet known
         */
        @Nullable
        Long getTotalByteCount();

        long getDownloadedByteCount();

        int getPercent();

        enum Status {
            PENDING,
            DOWNLOADING,
            FAILED
        }
    }
}

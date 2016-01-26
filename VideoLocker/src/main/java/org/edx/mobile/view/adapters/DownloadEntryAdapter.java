package org.edx.mobile.view.adapters;

import android.app.DownloadManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
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
    public void render(BaseViewHolder tag, final DownloadEntryAdapter.Item item) {
        ViewHolder holder = (ViewHolder) tag;

        holder.title.setText(item.getTitle());
        holder.duration.setText(item.getDuration());
        holder.percent.setText(item.getDownloaded() + " / "
                + MemoryUtil.format(getContext(), item.getSize()));

        holder.progress.setProgress(item.getPercent());
        if (item.getStatus() == DownloadManager.STATUS_FAILED) {
            holder.error.setVisibility(View.VISIBLE);
            holder.error.setTag(item);
            holder.error.setText(getContext()
                    .getString(R.string.error_download_failed));

            holder.progress.setProgressDrawable(getContext().getResources()
                    .getDrawable(
                            R.drawable.custom_progress_bar_horizontal_red));
        } else {
            holder.error.setVisibility(View.GONE);

            holder.progress
                    .setProgressDrawable(getContext()
                            .getResources()
                            .getDrawable(
                                    R.drawable.custom_progress_bar_horizontal_green));
        }

        holder.cross_image_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteClicked(item);
            }
        });
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        return new ViewHolder(convertView);
    }

    private static class ViewHolder extends BaseViewHolder {
        final TextView title;
        final TextView duration;
        final TextView percent;
        final LinearLayout cross_image_layout;
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
            cross_image_layout = (LinearLayout) view
                    .findViewById(R.id.close_btn_layout);
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
        long getSize();

        String getTitle();

        String getDuration();

        String getDownloaded();

        int getStatus();

        int getPercent();
    }
}

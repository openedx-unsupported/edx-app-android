package org.edx.mobile.view.adapters;

import android.app.DownloadManager;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.util.MemoryUtil;

public abstract class DownloadEntryAdapter extends BaseListAdapter<DownloadEntry> {

    private IStorage storage;

    public DownloadEntryAdapter(Context context) {
        super(context, R.layout.row_download_list);
    }

    public void setStore(IStorage storage) {
        this.storage = storage;
    }   
    
    @Override
    public void render(BaseViewHolder tag, final DownloadEntry model) {
        ViewHolder holder = (ViewHolder) tag;

        NativeDownloadModel nativeModel = storage.getNativeDownlaod(model.dmId);

        if (nativeModel != null && nativeModel.getPercent() == 100) {
            onDownloadComplete(model);
            return;
        }

        holder.txtTitle.setText(model.getTitle());
        holder.txtDuration.setText(model.getDurationReadable());
        if (nativeModel != null) {
            if(model.size == 0){
                holder.txtPercent.setText(nativeModel.getDownloaded() + " / "
                        + nativeModel.getSize());
            }else{
                holder.txtPercent.setText(nativeModel.getDownloaded() + " / "
                        + MemoryUtil.format(getContext(), model.size));
            }

            holder.progress.setProgress(nativeModel.getPercent());
            if (nativeModel.status == DownloadManager.STATUS_FAILED) {
                holder.txtError.setVisibility(View.VISIBLE);
                holder.txtError.setTag(model);
                holder.txtError.setText(getContext()
                        .getString(R.string.error_download_failed));

                holder.progress.setProgressDrawable(getContext().getResources()
                        .getDrawable(
                                R.drawable.custom_progress_bar_horizontal_red));
            } else {
                holder.txtError.setVisibility(View.GONE);

                holder.progress
                .setProgressDrawable(getContext()
                        .getResources()
                        .getDrawable(
                                R.drawable.custom_progress_bar_horizontal_green));
            }
        }

        holder.layoutCrossImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteClicked(model);
            }
        });
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.txtTitle = (TextView) convertView.findViewById(R.id.downloads_name);
        holder.txtDuration = (TextView) convertView
                .findViewById(R.id.download_time);
        holder.txtPercent = (TextView) convertView
                .findViewById(R.id.download_percentage);
        holder.txtError = (TextView) convertView
                .findViewById(R.id.txtDownloadFailed);
        holder.progress = (ProgressBar) convertView
                .findViewById(R.id.progressBar);
        holder.layoutCrossImage = (LinearLayout) convertView
                .findViewById(R.id.close_btn_layout);
        

        return holder;
    }

    private static class ViewHolder extends BaseViewHolder {
        TextView txtTitle;
        TextView txtDuration;
        TextView txtPercent;
        LinearLayout layoutCrossImage;
        TextView txtError;
        ProgressBar progress;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        DownloadEntry model = getItem(position);
        if(model!=null) onItemClicked(model);
    }

    public abstract void onItemClicked(DownloadEntry model);
    public abstract void onDownloadComplete(DownloadEntry model);
    public abstract void onDeleteClicked(DownloadEntry model);
}
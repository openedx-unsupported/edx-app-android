package org.edx.mobile.view.adapters;

import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.model.api.LectureModel;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.view.custom.ProgressWheel;

public abstract class LectureAdapter extends BaseListAdapter<LectureModel> {
    private long lastClickTime;
    private IDatabase dbStore;
    private IStorage storage;
    private String strEnrollmentId;

    public LectureAdapter(Context context) {
        super(context, R.layout.row_chapter_list);
        lastClickTime = 0;
    }

    public void setStore(IDatabase dbStore, IStorage storage, String enrollmentId) {
        this.storage = storage;
        this.dbStore = dbStore;
        this.strEnrollmentId = enrollmentId;
    }

    @Override
    public void render(BaseViewHolder tag, final LectureModel lectureData) {
        final ViewHolder holder = (ViewHolder) tag;

        holder.txtLectureTitle.setText(lectureData.name);
        if (lectureData.videos != null) {
            final int totalCount = lectureData.videos.size();
            holder.txtNoOfVideos.setVisibility(View.VISIBLE);
            holder.txtNoOfVideos.setText("" + totalCount);
            int inProcessCount = dbStore.getVideosCountBySection(strEnrollmentId,
                    lectureData.chapter.chapter, lectureData.name, null);
            int videoCount = totalCount - inProcessCount;
            if (videoCount > 0) {
                holder.layoutProgress.setVisibility(View.GONE);
                holder.layoutBulkDownloadVideos.setVisibility(View.VISIBLE);
                holder.layoutBulkDownloadVideos.setTag(lectureData);
                holder.layoutBulkDownloadVideos
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        download(lectureData);
                        holder.layoutProgress.setVisibility(View.VISIBLE);
                        holder.layoutBulkDownloadVideos.setVisibility(View.INVISIBLE);
                    }
                });
            } else {
                holder.layoutBulkDownloadVideos.setVisibility(View.INVISIBLE);
                if(dbStore.isVideoDownloadingInSection(strEnrollmentId,
                        lectureData.chapter.chapter, lectureData.name, null)){
                    holder.progressWheelDownload.setVisibility(View.VISIBLE);
                    try{
                        storage.getAverageDownloadProgressInSection
                        (strEnrollmentId, lectureData.chapter.chapter,
                                lectureData.name, new DataCallback<Integer>(true) {
                            @Override
                            public void onResult(Integer result) {
                                int percent = result;
                                if(percent>=0 && percent<100){
                                    holder.layoutProgress.setVisibility(View.VISIBLE);
                                    holder.progressWheelDownload.setProgressPercent(percent);
                                }else{
                                    holder.layoutProgress.setVisibility(View.GONE);
                                }
                            }
                            @Override
                            public void onFail(Exception ex) {
                                logger.error(ex);
                            }
                        });
                    }catch(Exception e){
                        logger.error(e);
                    }
                }else{
                    holder.layoutProgress.setVisibility(View.GONE);
                }
            }
        } else {
            holder.txtNoOfVideos.setVisibility(View.INVISIBLE);
            holder.layoutBulkDownloadVideos.setVisibility(View.INVISIBLE);
            holder.layoutProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.txtLectureTitle = (TextView) convertView
                .findViewById(R.id.chapter_name);
        holder.txtNoOfVideos = (TextView) convertView
                .findViewById(R.id.no_of_videos);
        holder.layoutBulkDownloadVideos = (LinearLayout) convertView
                .findViewById(R.id.bulk_download_layout);
        holder.progressWheelDownload = (ProgressWheel) convertView.
                findViewById(R.id.progress_wheel);
        holder.layoutProgress = (LinearLayout) convertView
                .findViewById(R.id.download_progress);
        return holder;
    }

    private static class ViewHolder extends BaseViewHolder {
        TextView txtLectureTitle;
        TextView txtNoOfVideos;
        LinearLayout layoutBulkDownloadVideos;
        ProgressWheel progressWheelDownload;
        LinearLayout layoutProgress;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
            long arg3) {
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
            lastClickTime = currentTime;
            LectureModel model = getItem(position);
            if(model!=null) onItemClicked(model);
        }
    }

    public abstract void onItemClicked(LectureModel model);

    public abstract void download(LectureModel lectureData);
}

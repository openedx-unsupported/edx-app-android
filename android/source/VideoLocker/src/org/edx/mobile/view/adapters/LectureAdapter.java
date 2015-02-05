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
    private String enrollmentId;

    public LectureAdapter(Context context) {
        super(context, R.layout.row_chapter_list);
        lastClickTime = 0;
    }

    public void setStore(IDatabase dbStore, IStorage storage, String enrollmentId) {
        this.storage = storage;
        this.dbStore = dbStore;
        this.enrollmentId = enrollmentId;
    }

    @Override
    public void render(BaseViewHolder tag, final LectureModel lectureData) {
        final ViewHolder holder = (ViewHolder) tag;

        holder.lectureTitle.setText(lectureData.name);
        if (lectureData.videos != null) {
            final int totalCount = lectureData.videos.size();
            holder.no_of_videos.setVisibility(View.VISIBLE);
            holder.no_of_videos.setText("" + totalCount);
            int inProcessCount = dbStore.getVideosCountBySection(enrollmentId, 
                    lectureData.chapter.chapter, lectureData.name, null);
            int videoCount = totalCount - inProcessCount;
            if (videoCount > 0) {
                holder.progresslayout.setVisibility(View.GONE);
                holder.bulk_download_videos_layout.setVisibility(View.VISIBLE);
                holder.bulk_download_videos_layout.setTag(lectureData);
                holder.bulk_download_videos_layout
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        download(lectureData);
                        holder.progresslayout.setVisibility(View.VISIBLE);
                        holder.bulk_download_videos_layout.setVisibility(View.INVISIBLE); 
                    }
                });
            } else {
                holder.bulk_download_videos_layout.setVisibility(View.INVISIBLE);
                if(dbStore.isVideoDownloadingInSection(enrollmentId, 
                        lectureData.chapter.chapter, lectureData.name, null)){
                    holder.download_pw.setVisibility(View.VISIBLE);
                    try{
                        storage.getAverageDownloadProgressInSection
                        (enrollmentId, lectureData.chapter.chapter, 
                                lectureData.name, new DataCallback<Integer>(true) {
                            @Override
                            public void onResult(Integer result) {
                                int percent = result;
                                if(percent>=0 && percent<100){
                                    holder.progresslayout.setVisibility(View.VISIBLE);
                                    holder.download_pw.setProgressPercent(percent);
                                }else{
                                    holder.progresslayout.setVisibility(View.GONE);
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
                    holder.progresslayout.setVisibility(View.GONE);
                }
            }
        } else {
            holder.no_of_videos.setVisibility(View.INVISIBLE);
            holder.bulk_download_videos_layout.setVisibility(View.INVISIBLE);
            holder.progresslayout.setVisibility(View.GONE);
        }
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.lectureTitle = (TextView) convertView
                .findViewById(R.id.chapter_name);
        holder.no_of_videos = (TextView) convertView
                .findViewById(R.id.no_of_videos);
        holder.bulk_download_videos_layout = (LinearLayout) convertView
                .findViewById(R.id.bulk_download_layout);
        holder.download_pw = (ProgressWheel) convertView.
                findViewById(R.id.progress_wheel);
        holder.progresslayout = (LinearLayout) convertView
                .findViewById(R.id.download_progress);
        return holder;
    }

    private static class ViewHolder extends BaseViewHolder {
        TextView lectureTitle;
        TextView no_of_videos;
        LinearLayout bulk_download_videos_layout;
        ProgressWheel download_pw;
        LinearLayout progresslayout;
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

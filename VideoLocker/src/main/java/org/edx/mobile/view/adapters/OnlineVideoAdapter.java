
package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.model.api.ChapterModel;
import org.edx.mobile.model.api.SectionItemModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.view.custom.ProgressWheel;
import org.edx.mobile.util.MemoryUtil;

public abstract class OnlineVideoAdapter extends VideoBaseAdapter<SectionItemInterface> {

    IDatabase dbStore;
    IStorage storage;
    public OnlineVideoAdapter(Context context, IDatabase dbStore, IStorage storage) {
        super(context, R.layout.row_video_list);
        this.dbStore = dbStore;
        this.storage = storage;
    }

    @Override
    public void render(BaseViewHolder tag, final SectionItemInterface sectionItem) {
        final ViewHolder holder = (ViewHolder) tag;

        if (sectionItem != null) {
            if (sectionItem.isChapter()) {
                holder.layoutVideo.setVisibility(View.GONE);
                ChapterModel c = (ChapterModel) sectionItem;
                holder.txtCourseTitle.setText(c.name);
                holder.txtCourseTitle.setVisibility(View.VISIBLE);
                holder.txtSectionTitle.setVisibility(View.GONE);
            }else if (sectionItem.isSection()) {
                holder.layoutVideo.setVisibility(View.GONE);
                SectionItemModel s = (SectionItemModel) sectionItem;
                holder.txtSectionTitle.setText(s.name);
                holder.txtSectionTitle.setVisibility(View.VISIBLE);
                holder.txtCourseTitle.setVisibility(View.GONE);
            }else {
                holder.txtCourseTitle.setVisibility(View.GONE);
                holder.txtSectionTitle.setVisibility(View.GONE);
                holder.layoutVideo.setVisibility(View.VISIBLE);

                final DownloadEntry videoData = (DownloadEntry) sectionItem;
                final String selectedVideoId = getVideoId();

                
                holder.txtVideoTitle.setText(videoData.title);
                holder.txtVideoSize.setText(MemoryUtil.format(getContext(), videoData.size));
                holder.txtVideoPlayingTime.setText(videoData.getDurationReadable());

                if (videoData.downloaded == DownloadEntry.DownloadedState.DOWNLOADING) {
                    // may be download in progress
                    holder.layoutProgress.setVisibility(View.VISIBLE);
                    holder.layoutVideoDownload.setVisibility(View.GONE);
                    NativeDownloadModel downloadModel = storage.
                            getNativeDownlaod(videoData.dmId);
                    if(downloadModel!=null){
                        int percent = downloadModel.getPercent();
                        if(percent>=0 && percent < 100){
                            holder.layoutProgress.setVisibility(View.VISIBLE);
                            holder.progressWheelDownload.setProgressPercent(percent);
                        }else{
                            holder.layoutProgress.setVisibility(View.GONE);
                        }
                    }
                }

                dbStore.getWatchedStateForVideoId(videoData.videoId,
                        new DataCallback<DownloadEntry.WatchedState>(true) {
                            @Override
                            public void onResult(DownloadEntry.WatchedState result) {
                                DownloadEntry.WatchedState ws = result;
                                if (ws == null || ws == DownloadEntry.WatchedState.UNWATCHED) {
                                    holder.imgVideoWatchedStatus.setImageResource(R.drawable.cyan_circle);
                                } else if (ws == DownloadEntry.WatchedState.PARTIALLY_WATCHED) {
                                    holder.imgVideoWatchedStatus.setImageResource(R.drawable.ic_partially_watched);
                                } else {
                                    holder.imgVideoWatchedStatus.setImageResource(R.drawable.grey_circle);
                                }
                            }

                            @Override
                            public void onFail(Exception ex) {
                                logger.error(ex);
                            }
                        });
                
                dbStore.getDownloadedStateForVideoId(videoData.videoId, 
                        new DataCallback<DownloadEntry.DownloadedState>(true) {
                    @Override
                    public void onResult(DownloadEntry.DownloadedState result) {
                        DownloadEntry.DownloadedState ds = result;
                        if(ds == null || ds == DownloadEntry.DownloadedState.ONLINE) {
                            // not yet downloaded
                            holder.layoutVideoDownload.setVisibility(View.VISIBLE);
                            holder.layoutProgress.setVisibility(View.GONE);
                            holder.layoutVideoDownload.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    holder.layoutProgress.setVisibility(View.VISIBLE);
                                    holder.layoutVideoDownload.setVisibility(View.GONE);
                                    logger.debug("Download Button Clicked");
                                    //notifyDataSetChanged();
                                    download(videoData, holder.progressWheelDownload);
                                }
                            });
                        } else if(ds == DownloadEntry.DownloadedState.DOWNLOADING) {
                            // may be download in progress
                            holder.layoutProgress.setVisibility(View.VISIBLE);
                            holder.layoutVideoDownload.setVisibility(View.GONE);
                            storage.getDownloadProgressByDmid(videoData.dmId, new DataCallback<Integer>(true) {
                                @Override
                                public void onResult(Integer result) {
                                    if(result>=0 && result < 100){
                                        holder.layoutProgress.setVisibility(View.VISIBLE);
                                        holder.progressWheelDownload.setProgressPercent(result);
                                    }else{
                                        holder.layoutProgress.setVisibility(View.GONE);
                                    }
                                }
                                @Override
                                public void onFail(Exception ex) {
                                    logger.error(ex);
                                    holder.layoutProgress.setVisibility(View.GONE);
                                    holder.layoutVideoDownload.setVisibility(View.VISIBLE);
                                }
                            });
                        } else if (ds == DownloadEntry.DownloadedState.DOWNLOADED) {
                            // downloaded
                            holder.layoutVideoDownload.setVisibility(View.GONE);
                            holder.layoutProgress.setVisibility(View.GONE);
                        }
                        
                        if(selectedVideoId!=null){
                            if (selectedVideoId.equalsIgnoreCase(videoData.videoId)) {
                                // mark this cell as selected and playing
                                holder.layoutVideo.setBackgroundResource(R.color.cyan_text_navigation_20);
                                if(isPlayerOn){
                                    holder.layoutVideoDownload.setVisibility(View.GONE);
                                }
                            } else {
                                // mark this cell as non-selected
                                holder.layoutVideo.setBackgroundResource(R.drawable.list_selector);
                            }
                        }else{
                            holder.layoutVideo.setBackgroundResource(R.drawable.list_selector);
                        }
                    }
                    @Override
                    public void onFail(Exception ex) {
                        logger.error(ex);
                        holder.layoutProgress.setVisibility(View.GONE);
                        holder.layoutVideoDownload.setVisibility(View.VISIBLE);
                    }
                });
                //Hiding the video size in Video Listing
                holder.txtVideoSize.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        final ViewHolder holder = new ViewHolder();
        holder.layoutVideoDownload = (LinearLayout) convertView
                .findViewById(R.id.video_download_layout);
        holder.progressWheelDownload = (ProgressWheel) convertView
                .findViewById(R.id.progress_wheel);
        holder.txtVideoTitle = (TextView) convertView
                .findViewById(R.id.video_title);
        holder.txtVideoPlayingTime = (TextView) convertView
                .findViewById(R.id.video_playing_time);
        holder.txtVideoSize = (TextView) convertView
                .findViewById(R.id.video_size);
        holder.imgVideoWatchedStatus = (ImageView) convertView
                .findViewById(R.id.video_watched_status);
        holder.txtCourseTitle = (TextView) convertView
                .findViewById(R.id.txt_course_title);
        holder.txtSectionTitle = (TextView) convertView
                .findViewById(R.id.txt_chapter_title);
        holder.layoutVideo = (RelativeLayout) convertView
                .findViewById(R.id.video_row_layout);
        holder.layoutProgress = (LinearLayout) convertView
                .findViewById(R.id.download_progress);
        return holder;
    }

    private static class ViewHolder extends BaseViewHolder {
        TextView txtVideoTitle;
        TextView txtVideoPlayingTime;
        TextView txtVideoSize;
        ImageView imgVideoWatchedStatus;
        LinearLayout layoutVideoDownload;
        LinearLayout layoutProgress;
        ProgressWheel progressWheelDownload;
        TextView txtCourseTitle;
        TextView txtSectionTitle;
        RelativeLayout layoutVideo;
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        selectedPosition = position;
        SectionItemInterface model = getItem(position);
        if(model!=null) onItemClicked(model, position);
    }

    public abstract void onItemClicked(SectionItemInterface model, int position);

    public abstract void download(DownloadEntry videoData, ProgressWheel progressWheel);
}


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
                holder.videolayout.setVisibility(View.GONE);
                ChapterModel c = (ChapterModel) sectionItem;
                holder.course_title.setText(c.name);
                holder.course_title.setVisibility(View.VISIBLE);
                holder.section_title.setVisibility(View.GONE);
            }else if (sectionItem.isSection()) {
                holder.videolayout.setVisibility(View.GONE);
                SectionItemModel s = (SectionItemModel) sectionItem;
                holder.section_title.setText(s.name);
                holder.section_title.setVisibility(View.VISIBLE);
                holder.course_title.setVisibility(View.GONE);
            }else {
                holder.course_title.setVisibility(View.GONE);
                holder.section_title.setVisibility(View.GONE);
                holder.videolayout.setVisibility(View.VISIBLE);

                final DownloadEntry videoData = (DownloadEntry) sectionItem;
                final String selectedVideoId = getVideoId();

                
                holder.videoTitle.setText(videoData.getTitle());
                holder.videoSize.setText(MemoryUtil.format(getContext(), videoData.size));
                holder.videoPlayingTime.setText(videoData.getDurationReadable());

                if (videoData.downloaded == DownloadEntry.DownloadedState.DOWNLOADING) {
                    // may be download in progress
                    holder.progresslayout.setVisibility(View.VISIBLE);
                    holder.video_download_layout.setVisibility(View.GONE);
                    NativeDownloadModel downloadModel = storage.
                            getNativeDownlaod(videoData.dmId);
                    if(downloadModel!=null){
                        int percent = downloadModel.getPercent();
                        if(percent>=0 && percent < 100){
                            holder.progresslayout.setVisibility(View.VISIBLE);
                            holder.download_pw.setProgressPercent(percent);
                        }else{
                            holder.progresslayout.setVisibility(View.GONE);
                        }
                    }
                }

                dbStore.getWatchedStateForVideoId(videoData.videoId, 
                        new DataCallback<DownloadEntry.WatchedState>(true) {
                    @Override
                    public void onResult(DownloadEntry.WatchedState result) {
                        DownloadEntry.WatchedState ws = result;
                        if(ws == null || ws == DownloadEntry.WatchedState.UNWATCHED) {
                            holder.video_watched_status.setImageResource(R.drawable.cyan_circle);
                        } else if(ws == DownloadEntry.WatchedState.PARTIALLY_WATCHED) {
                            holder.video_watched_status.setImageResource(R.drawable.ic_partially_watched);
                        } else {
                            holder.video_watched_status.setImageResource(R.drawable.grey_circle);
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
                            holder.video_download_layout.setVisibility(View.VISIBLE);
                            holder.progresslayout.setVisibility(View.GONE);
                            holder.video_download_layout.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    holder.progresslayout.setVisibility(View.VISIBLE);
                                    holder.video_download_layout.setVisibility(View.GONE);
                                    logger.debug("Download Button Clicked");
                                    //notifyDataSetChanged();
                                    download(videoData, holder.download_pw);
                                }
                            });
                        } else if(ds == DownloadEntry.DownloadedState.DOWNLOADING) {
                            // may be download in progress
                            holder.progresslayout.setVisibility(View.VISIBLE);
                            holder.video_download_layout.setVisibility(View.GONE);
                            storage.getDownloadProgressByDmid(videoData.dmId, new DataCallback<Integer>(true) {
                                @Override
                                public void onResult(Integer result) {
                                    if(result>=0 && result < 100){
                                        holder.progresslayout.setVisibility(View.VISIBLE);
                                        holder.download_pw.setProgressPercent(result);
                                    }else{
                                        holder.progresslayout.setVisibility(View.GONE);
                                    }
                                }
                                @Override
                                public void onFail(Exception ex) {
                                    logger.error(ex);
                                    holder.progresslayout.setVisibility(View.GONE);
                                    holder.video_download_layout.setVisibility(View.VISIBLE);
                                }
                            });
                        } else if (ds == DownloadEntry.DownloadedState.DOWNLOADED) {
                            // downloaded
                            holder.video_download_layout.setVisibility(View.GONE);
                            holder.progresslayout.setVisibility(View.GONE);
                        }
                        
                        if(selectedVideoId!=null){
                            if (selectedVideoId.equalsIgnoreCase(videoData.videoId)) {
                                // mark this cell as selected and playing
                                holder.videolayout.setBackgroundResource(R.color.cyan_text_navigation_20);
                                if(isPlayerOn){
                                    holder.video_download_layout.setVisibility(View.GONE);
                                }
                            } else {
                                // mark this cell as non-selected
                                holder.videolayout.setBackgroundResource(R.drawable.list_selector);
                            }
                        }else{
                            holder.videolayout.setBackgroundResource(R.drawable.list_selector);
                        }
                    }
                    @Override
                    public void onFail(Exception ex) {
                        logger.error(ex);
                        holder.progresslayout.setVisibility(View.GONE);
                        holder.video_download_layout.setVisibility(View.VISIBLE);
                    }
                });
                //Hiding the video size in Video Listing
                holder.videoSize.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        final ViewHolder holder = new ViewHolder();
        holder.video_download_layout = (LinearLayout) convertView
                .findViewById(R.id.video_download_layout);
        holder.download_pw = (ProgressWheel) convertView
                .findViewById(R.id.progress_wheel);
        holder.videoTitle = (TextView) convertView
                .findViewById(R.id.video_title);
        holder.videoPlayingTime = (TextView) convertView
                .findViewById(R.id.video_playing_time);
        holder.videoSize = (TextView) convertView
                .findViewById(R.id.video_size);
        holder.video_watched_status = (ImageView) convertView
                .findViewById(R.id.video_watched_status);
        holder.course_title = (TextView) convertView
                .findViewById(R.id.txt_course_title);
        holder.section_title = (TextView) convertView
                .findViewById(R.id.txt_chapter_title);
        holder.videolayout = (RelativeLayout) convertView
                .findViewById(R.id.video_row_layout);
        holder.progresslayout = (LinearLayout) convertView
                .findViewById(R.id.download_progress);
        return holder;
    }

    private static class ViewHolder extends BaseViewHolder {
        TextView videoTitle;
        TextView videoPlayingTime;
        TextView videoSize;
        ImageView video_watched_status;
        LinearLayout video_download_layout;
        LinearLayout progresslayout;
        ProgressWheel download_pw;
        TextView course_title;
        TextView section_title;
        RelativeLayout videolayout;
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

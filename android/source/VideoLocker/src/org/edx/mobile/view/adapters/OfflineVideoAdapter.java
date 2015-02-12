package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.model.api.ChapterModel;
import org.edx.mobile.model.api.SectionItemModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.view.custom.ProgressWheel;

public abstract class OfflineVideoAdapter extends VideoBaseAdapter<SectionItemInterface> {

    private IDatabase dbStore;
    public OfflineVideoAdapter(Context context, IDatabase dbStore) {
        super(context, R.layout.row_video_list);
        this.dbStore = dbStore;
    }

    @Override
    public void render(BaseViewHolder tag,
            final SectionItemInterface sectionItem) {
        final ViewHolder holder = (ViewHolder) tag;

        if (sectionItem != null) {
            if (sectionItem.isChapter()) {
                holder.videolayout.setVisibility(View.GONE);
                ChapterModel c = (ChapterModel) sectionItem;
                holder.course_title.setText(c.name);
                holder.course_title.setVisibility(View.VISIBLE);
                holder.section_title.setVisibility(View.GONE);
            } else if (sectionItem.isSection()) {
                holder.videolayout.setVisibility(View.GONE);
                holder.section_title.setVisibility(View.VISIBLE);
                holder.course_title.setVisibility(View.GONE);
                SectionItemModel s = (SectionItemModel) sectionItem;
                holder.section_title.setText(s.name);
            } else {
                holder.course_title.setVisibility(View.GONE);
                holder.section_title.setVisibility(View.GONE);
                holder.videolayout.setVisibility(View.VISIBLE);

                DownloadEntry videoData = (DownloadEntry) sectionItem;

                holder.videoTitle.setText(videoData.getTitle());
                holder.videoSize.setText(MemoryUtil.format(getContext(),videoData.size));
                holder.videoPlayingTime.setText(videoData.getDurationReadable());

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

                holder.download_pw.setVisibility(View.GONE);
                holder.video_download_layout.setVisibility(View.GONE);

                String selectedVideoId = getVideoId();

                //TODO : Need to check if this call to the db is required if we can get it from the above DS
                //if(store.isDownloaded(videoData.videoId)) {
                if(videoData.isDownloaded()){
                    holder.videoTitle.setTextColor(getContext().getResources()
                            .getColor(R.color.grey_text_mycourse));
                    holder.videoSize.setTextColor(getContext().getResources()
                            .getColor(R.color.grey_video_size_text));
                    holder.videoPlayingTime.setTextColor(getContext().getResources()
                            .getColor(R.color.grey_video_size_text));

                    if(selectedVideoId!=null){
                        if (selectedVideoId.equalsIgnoreCase(videoData.videoId)) {
                            // mark this cell as selected and playing
                            holder.videolayout.setBackgroundResource
                            (R.color.cyan_text_navigation_20);
                        } else {
                            // mark this cell as non-selected
                            holder.videolayout.setBackgroundResource
                            (R.drawable.list_selector);
                        }
                    }else{
                        holder.videolayout.setBackgroundResource
                        (R.drawable.list_selector);
                    }

                    if(AppConstants.videoListDeleteMode){
                        holder.delete_checkbox.setVisibility(View.VISIBLE);
                        holder.delete_checkbox.setChecked(isSelected(holder.position));
                        holder.delete_checkbox.setTag(videoData);
                    } else {
                        holder.delete_checkbox.setVisibility(View.GONE);
                    }
                } else {
                    holder.videolayout.setBackgroundResource(R.color.disabled_chapter_list);
                    holder.delete_checkbox.setVisibility(View.GONE);
                    holder.videoTitle.setTextColor(getContext().getResources()
                            .getColor(R.color.light_gray));
                    holder.videoSize.setTextColor(getContext().getResources()
                            .getColor(R.color.light_gray));
                    holder.videoPlayingTime.setTextColor(getContext().getResources()
                            .getColor(R.color.light_gray));
                }
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
        holder.delete_checkbox = (CheckBox) convertView
                .findViewById(R.id.video_select_checkbox);
        holder.delete_checkbox
        .setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                if (isChecked) {
                    select(holder.position);
                    onSelectItem();
                } else {
                    unselect(holder.position);
                    onSelectItem();
                }
            }
        });
        return holder;
    }

    private static class ViewHolder extends BaseViewHolder {
        TextView videoTitle;
        TextView videoPlayingTime;
        TextView videoSize;
        ImageView video_watched_status;
        LinearLayout video_download_layout;
        ProgressWheel download_pw;
        CheckBox delete_checkbox;
        TextView course_title;
        TextView section_title;
        RelativeLayout videolayout;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
            long arg3) {
        SectionItemInterface model = getItem(position);
        if(model!=null) {
            if (model.isDownload()) {
                DownloadEntry downloadEntry = (DownloadEntry) model;
                if (downloadEntry.isDownloaded()) {
                    selectedPosition = position;
                }
            }
            if (!AppConstants.videoListDeleteMode) {
                onItemClicked(model, position);
            }
        }
    }

    public abstract void onItemClicked(SectionItemInterface model, int position);

    public abstract void onSelectItem();

}

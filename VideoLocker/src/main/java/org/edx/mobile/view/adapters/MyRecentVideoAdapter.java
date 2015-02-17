package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.MemoryUtil;

public abstract class MyRecentVideoAdapter extends VideoBaseAdapter<SectionItemInterface> {

    private IDatabase dbStore;

    public MyRecentVideoAdapter(Context context, IDatabase dbStore) {
        super(context, R.layout.row_video_list);
        this.dbStore = dbStore;
    }

    @Override
    public void render(BaseViewHolder tag, SectionItemInterface sectionItem) {
        final ViewHolder holder = (ViewHolder) tag;

        if (sectionItem != null) {
            if(sectionItem.isCourse()){
                EnrolledCoursesResponse enrollment = (EnrolledCoursesResponse) sectionItem;
                holder.section_title.setText(enrollment.getCourse().getName());
                holder.section_title.setVisibility(View.VISIBLE);
                holder.videolayout.setVisibility(View.GONE);
            }else if(sectionItem.isDownload()){
                holder.section_title.setVisibility(View.GONE);
                holder.videolayout.setVisibility(View.VISIBLE);

                DownloadEntry videoData = (DownloadEntry) sectionItem;
                holder.videoTitle.setText(videoData.getTitle());
                holder.videoSize.setText(MemoryUtil.format(getContext(), videoData.size));
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
                if(videoData.isDownloaded()){
                    if (holder.position == selectedPosition) {
                        // mark this cell as selected and playing
                        holder.videolayout.setBackgroundResource(R.color.cyan_text_navigation_20);
                    } else {
                        // mark this cell as non-selected
                        holder.videolayout.setBackgroundResource(R.drawable.list_selector);
                    }

                    if(AppConstants.myVideosDeleteMode){
                        holder.delete_checkbox.setVisibility(View.VISIBLE);
                        holder.delete_checkbox.setChecked(isSelected(holder.position));
                    }else{
                        holder.delete_checkbox.setVisibility(View.GONE);
                    }
                }else{
                    holder.videolayout.setBackgroundResource(R.color.transparent_white);
                    holder.delete_checkbox.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        final ViewHolder holder = new ViewHolder();
        holder.videoTitle = (TextView) convertView
                .findViewById(R.id.video_title);
        holder.videoPlayingTime = (TextView) convertView
                .findViewById(R.id.video_playing_time);
        holder.videoSize = (TextView) convertView
                .findViewById(R.id.video_size);
        holder.video_watched_status = (ImageView) convertView
                .findViewById(R.id.video_watched_status);
        holder.section_title = (TextView) convertView
                .findViewById(R.id.txt_chapter_title);
        holder.videolayout = (RelativeLayout) convertView
                .findViewById(R.id.video_row_layout);
        holder.delete_checkbox  = (CheckBox) convertView
                .findViewById(R.id.video_select_checkbox);
        holder.delete_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
        CheckBox delete_checkbox;
        TextView section_title;
        RelativeLayout videolayout;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        selectedPosition=position;
        SectionItemInterface model = getItem(position);
        if(model!=null) onItemClicked(model, position);
    }

    public abstract void onItemClicked(SectionItemInterface model, int position);
    public abstract void onSelectItem();
}

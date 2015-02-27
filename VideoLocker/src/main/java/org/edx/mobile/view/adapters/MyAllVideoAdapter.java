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
import org.edx.mobile.model.api.ChapterModel;
import org.edx.mobile.model.api.SectionItemModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.db.DownloadEntry.WatchedState;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.MemoryUtil;

public abstract class MyAllVideoAdapter extends VideoBaseAdapter<SectionItemInterface> {

    IDatabase dbStore;
    public MyAllVideoAdapter(Context context, IDatabase dbStore) {
        super(context, R.layout.row_video_list);
        this.dbStore = dbStore;
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
            }
            else if (sectionItem.isSection()) {
                holder.layoutVideo.setVisibility(View.GONE);
                SectionItemModel s = (SectionItemModel) sectionItem;
                holder.txtSectionTitle.setText(s.name);
                holder.txtSectionTitle.setVisibility(View.VISIBLE);
                holder.txtCourseTitle.setVisibility(View.GONE);
            }
            else {
                holder.txtCourseTitle.setVisibility(View.GONE);
                holder.txtSectionTitle.setVisibility(View.GONE);
                holder.layoutVideo.setVisibility(View.VISIBLE);
                
                DownloadEntry videoData = (DownloadEntry) sectionItem;
                holder.txtVideoTitle.setText(videoData.getTitle());
                
                holder.txtVideoSize.setText(MemoryUtil.format(getContext(), videoData.size));
                holder.txtVideoPlayingTime.setText(videoData.getDurationReadable());

                dbStore.getWatchedStateForVideoId(videoData.videoId, 
                        new DataCallback<DownloadEntry.WatchedState>(true) {
                    @Override
                    public void onResult(WatchedState result) {
                        WatchedState ws = result;
                        if(ws == null || ws == WatchedState.UNWATCHED) {
                            holder.imgVideoWatchedStatus.setImageResource(R.drawable.cyan_circle);
                        } else if(ws == WatchedState.PARTIALLY_WATCHED) {
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
                
                if(videoData.isDownloaded()){
                    if (holder.position == selectedPosition) {
                        // mark this cell as selected and playing
                        holder.layoutVideo.setBackgroundResource(R.color.cyan_text_navigation_20);
                    } else {
                        // mark this cell as non-selected
                        holder.layoutVideo.setBackgroundResource(R.drawable.list_selector);
                    }

                    if(AppConstants.myVideosDeleteMode){
                        holder.checkBoxDelete.setVisibility(View.VISIBLE);
                        holder.checkBoxDelete.setChecked(isSelected(holder.position));
                    }else{
                        holder.checkBoxDelete.setVisibility(View.GONE);
                    }
                }else{
                    holder.layoutVideo.setBackgroundResource(R.color.transparent_white);
                    holder.checkBoxDelete.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        final ViewHolder holder = new ViewHolder();
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
        holder.checkBoxDelete = (CheckBox) convertView
                .findViewById(R.id.video_select_checkbox);
        holder.checkBoxDelete.setOnCheckedChangeListener(
                new OnCheckedChangeListener() {
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
        TextView txtVideoTitle;
        TextView txtVideoPlayingTime;
        TextView txtVideoSize;
        ImageView imgVideoWatchedStatus;
        CheckBox checkBoxDelete;
        TextView txtCourseTitle;
        TextView txtSectionTitle;
        RelativeLayout layoutVideo;
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

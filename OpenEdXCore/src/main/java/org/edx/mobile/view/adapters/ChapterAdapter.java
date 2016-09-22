package org.edx.mobile.view.adapters;

import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.custom.ProgressWheel;

public abstract class ChapterAdapter extends BaseListAdapter<SectionEntry> {

    private String courseId;
    private long lastClickTime;


    public ChapterAdapter(Context context, String courseId, IEdxEnvironment environment) {
        super(context, R.layout.row_chapter_list, environment);
        this.courseId = courseId;
        lastClickTime = 0;
    }



    @Override
    public void render(BaseViewHolder tag, final SectionEntry model) {
        final ViewHolder holder = (ViewHolder) tag;

        holder.chapterName.setText(model.chapter);
        final int totalCount = model.getVideoCount();
        holder.no_of_videos.setVisibility(View.VISIBLE);
        holder.no_of_videos.setText("" + totalCount);
        int inProcessCount = environment.getDatabase().getVideosCountByChapter(courseId, model.chapter, null);
        int webOnlyCount = environment.getDatabase().getWebOnlyVideosCountByChapter(courseId,model.chapter,null);
        int videoCount = totalCount - inProcessCount - webOnlyCount;
        if (videoCount > 0) {
            holder.progresslayout.setVisibility(View.INVISIBLE);
            holder.bulk_download_videos.setVisibility(View.VISIBLE);
            holder.bulk_download_videos
            .setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View downloadView) {
                    download(model);
                }
            });
        } else {
            holder.bulk_download_videos.setVisibility(View.INVISIBLE);
            if(environment.getDatabase().isVideoDownloadingInChapter(courseId, model.chapter, null)){
                holder.download_pw.setVisibility(View.VISIBLE);
                environment.getStorage().getAverageDownloadProgressInChapter(
                        courseId, model.chapter, new DataCallback<Integer>(true) {
                            @Override
                            public void onResult(Integer result) {
                                int percent = result;
                                if(percent>=0 && percent<100){
                                    holder.progresslayout.setVisibility(View.VISIBLE);
                                    holder.download_pw.setProgressPercent(percent);
                                }else{
                                    holder.progresslayout.setVisibility(View.INVISIBLE);
                                }
                            }
                            @Override
                            public void onFail(Exception ex) {
                                logger.error(ex);
                            }
                        });
            }else{
                holder.progresslayout.setVisibility(View.INVISIBLE);
            }
        }

        final Context context = getContext();
        if (!NetworkUtil.isConnected(context)) {
            holder.progresslayout.setVisibility(View.INVISIBLE);
            holder.no_of_videos.setVisibility(View.GONE);
            holder.bulk_download_videos.setVisibility(View.GONE);
            holder.download_pw.setVisibility(View.INVISIBLE);
            holder.next_arrow.setVisibility(View.VISIBLE);
            boolean isVideoDownloaded = environment.getDatabase().isVideoDownloadedInChapter
                    (courseId, model.chapter, null);
            if(isVideoDownloaded)
            {
                holder.next_arrow.setBackgroundResource(R.drawable.ic_next_default_mirrored);
                holder.chapterLayout.setBackgroundResource(R.drawable.list_item_overlay_selector);
                holder.chapterName.setTextColor(context.getResources()
                        .getColor(R.color.grey_text_mycourse));
            }else{
                holder.next_arrow
                .setBackgroundResource(R.drawable.ic_next_deactive_mirrored);
                holder.chapterLayout
                        .setBackgroundResource(R.color.disabled_chapter_list);
                holder.chapterName.setTextColor(context.getResources()
                        .getColor(R.color.light_gray));
            }

        } else {
            holder.chapterLayout.setBackgroundResource(R.drawable.list_item_overlay_selector);
            holder.next_arrow.setVisibility(View.GONE);
            holder.chapterName.setTextColor(context.getResources().getColor(
                    R.color.grey_text_mycourse));
        }
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.chapterName = (TextView) convertView
                .findViewById(R.id.chapter_name);
        holder.no_of_videos = (TextView) convertView
                .findViewById(R.id.no_of_videos);
        holder.bulk_download_videos = (LinearLayout) convertView
                .findViewById(R.id.bulk_download_layout);
        holder.next_arrow = (ImageView) convertView
                .findViewById(R.id.next_arrow);
        holder.chapterLayout = (RelativeLayout) convertView
                .findViewById(R.id.chapter_row_layout);
        holder.download_pw = (ProgressWheel) convertView.
                findViewById(R.id.progress_wheel);
        holder.progresslayout = (LinearLayout) convertView
                .findViewById(R.id.download_progress);
        return holder;
    }

    private static class ViewHolder extends BaseViewHolder {
        TextView chapterName;
        TextView no_of_videos;
        LinearLayout bulk_download_videos;
        ImageView next_arrow;
        RelativeLayout chapterLayout;
        ProgressWheel download_pw;
        LinearLayout progresslayout;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
            long arg3) {
        //This has been used so that if user clicks continuously on the screen,
        //two activities should not be opened
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
            lastClickTime = currentTime;
            SectionEntry model = getItem(position);
            if(model!=null) onItemClicked(model);
        }
    }

    public abstract void onItemClicked(SectionEntry model);
    public abstract void download(SectionEntry model);
}

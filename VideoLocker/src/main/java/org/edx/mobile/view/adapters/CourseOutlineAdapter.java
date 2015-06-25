package org.edx.mobile.view.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.event.DownloadEvent;
import org.edx.mobile.model.course.BlockPath;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.IBlock;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.util.AppConstants;

import de.greenrobot.event.EventBus;

/**
 * Used for pinned behavior.
 */
public abstract  class CourseOutlineAdapter extends CourseBaseAdapter  {

    private boolean currentVideoMode;

    public CourseOutlineAdapter(Context context, IDatabase dbStore, IStorage storage) {
        super(context, dbStore, storage);
    }

    /**
     * component can be null.
     * @IComponent component should be ICourse
     */
    public void setData(CourseComponent component){
        if (component != null &&  !component.isContainer())
            return;//
        this.rootComponent = component;
        mData.clear();
        if ( rootComponent != null ) {
            PrefManager.UserPrefManager userPrefManager = new PrefManager.UserPrefManager(MainApplication.instance());
            currentVideoMode = userPrefManager.isUserPrefVideoModel();

            for(IBlock block : rootComponent.getChildren()){
                CourseComponent comp = (CourseComponent)block;
                if ( currentVideoMode && comp.getBlockCount().videoCount == 0 )
                    continue;

                if ( comp.isContainer() ){
                    SectionRow header = new SectionRow(SectionRow.SECTION, comp );
                    mData.add( header );
                    for( IBlock childBlock : comp.getChildren() ){
                        CourseComponent child = (CourseComponent)childBlock;
                        if ( currentVideoMode && child.getBlockCount().videoCount == 0 )
                            continue;
                        SectionRow row = new SectionRow(SectionRow.ITEM, false, child );
                        mData.add( row );
                    }
                } else {
                    SectionRow row = new SectionRow(SectionRow.ITEM, true, comp );
                    mData.add( row );
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public View getRowView(int position, View convertView, ViewGroup parent) {
        final SectionRow row = this.getItem(position);
        final CourseComponent component = row.component;
        if (selectedRow != null && selectedRow.component.equals( row.component )){
            convertView.setBackgroundResource(R.color.edx_brand_primary_x_light);
        } else {
            convertView.setBackgroundResource(R.color.white);
        }
        final ViewHolder viewHolder = (ViewHolder)convertView.getTag();
        if( row.component.isLastChild() ){
            viewHolder.halfSeparator.setVisibility(View.GONE);
        } else {
            viewHolder.halfSeparator.setVisibility(View.VISIBLE);
        }

        viewHolder.rowType.setVisibility(View.GONE);
        viewHolder.rowSubtitleIcon.setVisibility(View.GONE);
        viewHolder.rowSubtitle.setVisibility(View.GONE);
        viewHolder.rowSubtitlePanel.setVisibility(View.GONE);
        viewHolder.noOfVideos.setVisibility(View.INVISIBLE);
        viewHolder.bulkDownloadVideos.setVisibility(View.GONE);

        if (component.isContainer()) {
            return getRowViewForContainer(position, convertView, parent, row);
        } else {
            return getRowViewForLeaf(position, convertView, parent, row);
        }
    }

    private  View getRowViewForLeaf(int position, View convertView, ViewGroup parent, final SectionRow row) {

        final ViewHolder viewHolder = (ViewHolder)convertView.getTag();

        CourseComponent unit =  row.component;
        viewHolder.rowType.setVisibility(View.VISIBLE);
        viewHolder.rowSubtitleIcon.setVisibility(View.GONE);
        viewHolder.rowSubtitle.setVisibility(View.GONE);
        viewHolder.rowSubtitlePanel.setVisibility(View.GONE);
        viewHolder.bulkDownload.setVisibility(View.INVISIBLE);

        if ( !unit.isResponsiveUI() && unit.getType() != BlockType.VIDEO){
            viewHolder.bulkDownload.setVisibility(View.INVISIBLE);
            viewHolder.rowSubtitlePanel.setVisibility(View.VISIBLE);
            viewHolder.rowType.setIcon(Iconify.IconValue.fa_laptop);
            viewHolder.rowSubtitle.setVisibility(View.VISIBLE);
            viewHolder.rowSubtitle.setText(unit.getType().name());
            if (unit.isGraded()) {
                viewHolder.rowSubtitleIcon.setVisibility(View.VISIBLE);
            } else {
                viewHolder.rowSubtitleIcon.setVisibility(View.INVISIBLE);
            }
            checkAccessStatus(viewHolder, unit);

        } else if (row.component instanceof VideoBlockModel){
            updateUIForVideo(position, convertView, viewHolder, row);
        } else {
            viewHolder.bulkDownload.setVisibility(View.INVISIBLE);
            if( unit.getType() == BlockType.PROBLEM ) {
                viewHolder.rowType.setIcon(Iconify.IconValue.fa_list);
            } else if( unit.getType() == BlockType.DISCUSSION ) {
                viewHolder.rowType.setIcon(Iconify.IconValue.fa_comments_o);
            } else {
                viewHolder.rowType.setIcon(Iconify.IconValue.fa_file_o);
            }
            checkAccessStatus(viewHolder, unit);
        }
        String title = unit.getDisplayName();
        if (TextUtils.isEmpty(title)) {
            //TODO - wait for production decision
            viewHolder.rowTitle.setText("Untitled Unit");
        } else {
            viewHolder.rowTitle.setText(unit.getDisplayName());
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowClicked(row);
            }
        });
        return convertView;
    }

    private void checkAccessStatus(final ViewHolder viewHolder, final CourseComponent unit){
        dbStore.isUnitAccessed(new DataCallback<Boolean>(true){
            @Override
            public void onResult(Boolean accessed) {
                if( accessed ) {
                    viewHolder.rowType.setIconColor(context.getResources().getColor(R.color.grey_3));
                } else {
                    viewHolder.rowType.setIconColor(context.getResources().getColor(R.color.cyan_3));
                }
            }
            @Override
            public void onFail(Exception ex) {
                logger.error(ex);
            }
        }, unit.getId());
    }

    private void updateUIForVideo(int position, View convertView, final ViewHolder viewHolder, final SectionRow row ){
        VideoBlockModel unit = (VideoBlockModel) row.component;

        viewHolder.rowType.setIcon(Iconify.IconValue.fa_film);
        viewHolder.bulkDownload.setVisibility(View.VISIBLE);

        final DownloadEntry videoData =  unit.getDownloadEntry(storage);

        viewHolder.rowSubtitlePanel.setVisibility(View.VISIBLE);
        viewHolder.rowSubtitle.setVisibility(View.VISIBLE);
        viewHolder.rowSubtitle.setText(videoData.getDurationReadable());

        if (videoData.downloaded == DownloadEntry.DownloadedState.DOWNLOADING) {

            NativeDownloadModel downloadModel = storage.
                getNativeDownlaod(videoData.dmId);
            if(downloadModel!=null){
                int percent = downloadModel.getPercent();
                if(percent>=0 && percent < 100){
                    EventBus.getDefault().post(new DownloadEvent(DownloadEvent.DownloadStatus.STARTED));
                }
            }
        }

        dbStore.getWatchedStateForVideoId(videoData.videoId,
            new DataCallback<DownloadEntry.WatchedState>(true) {
                @Override
                public void onResult(DownloadEntry.WatchedState result) {
                    DownloadEntry.WatchedState ws = result;
                    if(ws == null || ws == DownloadEntry.WatchedState.UNWATCHED) {
                        viewHolder.rowType.setIconColor(context.getResources().getColor(R.color.cyan_3));
                    } else if(ws == DownloadEntry.WatchedState.PARTIALLY_WATCHED) {
                        viewHolder.rowType.setIconColor(context.getResources().getColor(R.color.cyan_2));
                    } else {
                        viewHolder.rowType.setIconColor(context.getResources().getColor(R.color.grey_3));
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
                        viewHolder.bulkDownloadVideos.setVisibility(View.VISIBLE);
                        viewHolder.bulkDownloadVideos.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EventBus.getDefault().post(new DownloadEvent(DownloadEvent.DownloadStatus.STARTED));
                                logger.debug("Download Button Clicked");
                                //notifyDataSetChanged();
                                download(videoData);
                            }
                        });
                    } else if(ds == DownloadEntry.DownloadedState.DOWNLOADING) {
                        // may be download in progress
                        EventBus.getDefault().post(new DownloadEvent(DownloadEvent.DownloadStatus.STARTED));
                        viewHolder.bulkDownloadVideos.setVisibility(View.GONE);
                        storage.getDownloadProgressByDmid(videoData.dmId, new DataCallback<Integer>(true) {
                            @Override
                            public void onResult(Integer result) {
                                if(result>=0 && result < 100){
                                    EventBus.getDefault().post(new DownloadEvent(DownloadEvent.DownloadStatus.STARTED));
                                } else if ( result == 100 ){
                                    EventBus.getDefault().post(new DownloadEvent(DownloadEvent.DownloadStatus.COMPLETED));
                                }
                            }
                            @Override
                            public void onFail(Exception ex) {
                                logger.error(ex);
                                viewHolder.bulkDownloadVideos.setVisibility(View.VISIBLE);
                            }
                        });
                    } else if (ds == DownloadEntry.DownloadedState.DOWNLOADED) {
                        // downloaded
                        viewHolder.bulkDownloadVideos.setVisibility(View.GONE);
                    }

                }
                @Override
                public void onFail(Exception ex) {
                    logger.error(ex);
                    viewHolder.bulkDownloadVideos.setVisibility(View.VISIBLE);
                }
            });

    }


    private  View getRowViewForContainer(int position, View convertView, ViewGroup parent, final SectionRow row) {
        final CourseComponent component = row.component;
        String courseId = component.getCourseId();
        BlockPath path = component.getPath();
        //FIXME - we should add a new column in database - pathinfo.
        //then do the string match to get the record
        String chapterId = path.get(1) == null ? "" : path.get(1).getDisplayName();
        String sequentialId =  path.get(2) == null ? "" : path.get(2).getDisplayName();

        ViewHolder holder = (ViewHolder)convertView.getTag();

        holder.rowTitle.setText(component.getDisplayName());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowClicked(row);
            }
        });

        if ( component.isGraded() ){
            holder.bulkDownload.setVisibility(View.INVISIBLE);
            holder.rowSubtitlePanel.setVisibility(View.VISIBLE);
            holder.rowSubtitleIcon.setVisibility(View.VISIBLE);
            holder.rowSubtitle.setVisibility(View.VISIBLE);
            holder.rowSubtitle.setText(component.getFormat());
        }


        //support video download for video type
        final int totalCount = component.getBlockCount().videoCount;
        if (totalCount == 0 ){
            holder.noOfVideos.setVisibility(View.INVISIBLE);
            holder.bulkDownloadVideos.setVisibility(View.GONE);
        } else {
            holder.noOfVideos.setVisibility(View.VISIBLE);
            holder.noOfVideos.setText("" + totalCount);

            if (  row.numOfVideoNotDownloaded == 0 ){
                holder.bulkDownloadVideos.setVisibility(View.GONE);
            } else {
                int inProcessCount = dbStore.getVideosCountBySection(courseId, chapterId, sequentialId, null);
                int webOnlyCount = dbStore.getWebOnlyVideosCountBySection(courseId, chapterId, sequentialId, null);
                row.numOfVideoNotDownloaded = totalCount - inProcessCount - webOnlyCount;
                if (row.numOfVideoNotDownloaded > 0) {
                    holder.bulkDownloadVideos.setVisibility(View.VISIBLE);
                    holder.bulkDownloadVideos
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View downloadView) {
                                download(component.getVideos());
                            }
                        });
                } else {
                    holder.bulkDownloadVideos.setVisibility(View.GONE);
                }
            }
        }

        if (AppConstants.offline_flag) {
            holder.noOfVideos.setVisibility(View.GONE);
            holder.bulkDownloadVideos.setVisibility(View.GONE);
            boolean isVideoDownloaded = dbStore.isVideoDownloadedInSection
                (courseId, chapterId, sequentialId, null);
            if(isVideoDownloaded)
            {
                //TODO - any UI update
            }else{
                //TODO - any UI update
            }

        } else {
           //TOOD - any UI update?
        }

        return convertView;
    }

    public  View getHeaderView(int position, View convertView, ViewGroup parent){
        final SectionRow row = this.getItem(position);
        ((TextView)convertView).setText(row.component.getDisplayName());
        return convertView;
    }

    /**
     *
     * @return <code>true</code> if we rebuild the list due to the change of mode preference
     */
    public boolean checkModeChange(){
        PrefManager.UserPrefManager userPrefManager = new PrefManager.UserPrefManager(MainApplication.instance());
        boolean modeInConfiguration = userPrefManager.isUserPrefVideoModel();
        if ( modeInConfiguration != currentVideoMode ){
            setData(rootComponent);
            return true;
        }  else {
            return false;
        }
    }
}

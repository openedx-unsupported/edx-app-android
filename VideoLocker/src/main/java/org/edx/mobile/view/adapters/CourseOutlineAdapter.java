package org.edx.mobile.view.adapters;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.internal.Animation;
import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.course.BlockPath;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.course.IBlock;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.storage.IStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for pinned behavior.
 */
public class CourseOutlineAdapter extends BaseAdapter{

    private final Logger logger = new Logger(getClass().getName());

    public interface DownloadListener {
        void download(List<HasDownloadEntry> models);
        void download(DownloadEntry videoData);
        void viewDownloadsStatus();
    }

    private CourseComponent rootComponent;
    private LayoutInflater mInflater;
    private List<SectionRow> mData;

    private IDatabase dbStore;
    private IStorage storage;
    private DownloadListener mDownloadListener;
    private Context context;

    private boolean currentVideoMode;
    private int numOfTotalUnits;

    public CourseOutlineAdapter(Context context, IDatabase dbStore, IStorage storage, DownloadListener listener) {
        this.context = context;
        this.dbStore = dbStore;
        this.storage = storage;
        this.mDownloadListener = listener;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = new ArrayList();
    }

    @Override public int getItemViewType(int position) {
        return getItem(position).type;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public SectionRow getItem(int position) {
        if ( position < 0 || position >= mData.size() )
            return null;
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == SectionRow.ITEM;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);

        // FIXME: Re-enable row recycling in favor of better DB communication [MA-1640]
        //if (convertView == null) {
            switch (type) {
                case SectionRow.ITEM: {
                    convertView = mInflater.inflate(R.layout.row_course_outline_list, parent, false);
                    // apply a tag to this list row
                    ViewHolder tag = getTag(convertView);
                    convertView.setTag(tag);
                    break;
                }
                default: {//SectionRow.SECTION:
                    convertView = mInflater.inflate(R.layout.row_section_header, parent, false);
                    break;
                }
            }
        //}

        switch (type) {
            case SectionRow.ITEM:
                return  getRowView(position, convertView, parent);
            default : //SectionRow.SECTION:
                return  getHeaderView(position, convertView, parent);
        }
    }

    /**
     * component can be null.
     * @IComponent component should be ICourse
     */
    public void setData(CourseComponent component){
        if (component != null &&  !component.isContainer())
            return;//
        this.rootComponent = component;
        this.numOfTotalUnits = 0;
        mData.clear();
        if ( rootComponent != null ) {
            PrefManager.UserPrefManager userPrefManager = new PrefManager.UserPrefManager(MainApplication.instance());
            currentVideoMode = userPrefManager.isUserPrefVideoModel();
            List<IBlock> children = rootComponent.getChildren();
            this.numOfTotalUnits = children.size();
            for(IBlock block : children){
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

    public void reloadData(){
        if (  this.rootComponent != null )
             setData(this.rootComponent);
    }

    public View getRowView(int position, View convertView, ViewGroup parent) {
        final SectionRow row = this.getItem(position);
        final SectionRow nextRow = this.getItem(position+1);
        final CourseComponent component = row.component;
        final ViewHolder viewHolder = (ViewHolder)convertView.getTag();

        if ( nextRow == null ){
            viewHolder.halfSeparator.setVisibility(View.GONE);
            viewHolder.wholeSeparator.setVisibility(View.VISIBLE);
        } else {
            viewHolder.wholeSeparator.setVisibility(View.GONE);
            boolean isLastChildInBlock = !row.component.getParent().getId().equals( nextRow.component.getParent().getId());
            if( isLastChildInBlock ){
                viewHolder.halfSeparator.setVisibility(View.GONE);
            } else {
                viewHolder.halfSeparator.setVisibility(View.VISIBLE);
            }
        }

        viewHolder.rowType.setVisibility(View.GONE);
        viewHolder.rowSubtitleIcon.setVisibility(View.GONE);
        viewHolder.rowSubtitle.setVisibility(View.GONE);
        viewHolder.rowSubtitlePanel.setVisibility(View.GONE);
        viewHolder.numOfVideoAndDownloadArea.setVisibility(View.GONE);

        if (component.isContainer()) {
            return getRowViewForContainer(position, convertView, parent, row);
        } else {
            return getRowViewForLeaf(position, convertView, parent, row);
        }
    }

    private  View getRowViewForLeaf(int position, View convertView, ViewGroup parent,
                                    final SectionRow row) {
        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        CourseComponent unit = row.component;
        viewHolder.rowType.setVisibility(View.VISIBLE);
        viewHolder.rowSubtitleIcon.setVisibility(View.GONE);
        viewHolder.rowSubtitle.setVisibility(View.GONE);
        viewHolder.rowSubtitlePanel.setVisibility(View.GONE);
        viewHolder.bulkDownload.setVisibility(View.INVISIBLE);

        if (row.component instanceof VideoBlockModel) {
            updateUIForVideo(position, convertView, viewHolder, row);
        } else if (!unit.isMultiDevice()) {
            // If we reach here & the type is VIDEO, it means the video is webOnly
            viewHolder.bulkDownload.setVisibility(View.INVISIBLE);
            viewHolder.rowType.setIcon(FontAwesomeIcons.fa_laptop);
            viewHolder.rowType.setIconColorResource(R.color.edx_grayscale_neutral_base);
        } else {
            viewHolder.bulkDownload.setVisibility(View.INVISIBLE);
            if (unit.getType() == BlockType.PROBLEM) {
                viewHolder.rowType.setIcon(FontAwesomeIcons.fa_list);
            } else if (unit.getType() == BlockType.DISCUSSION) {
                viewHolder.rowType.setIcon(FontAwesomeIcons.fa_comments_o);
            } else {
                viewHolder.rowType.setIcon(FontAwesomeIcons.fa_file_o);
            }
            checkAccessStatus(viewHolder, unit);
        }
        viewHolder.rowTitle.setText(unit.getDisplayName());
        return convertView;
    }

    private void checkAccessStatus(final ViewHolder viewHolder, final CourseComponent unit) {
        dbStore.isUnitAccessed(new DataCallback<Boolean>(true) {
            @Override
            public void onResult(Boolean accessed) {
                if (accessed) {
                    viewHolder.rowType.setIconColorResource(R.color.edx_grayscale_neutral_base);
                } else {
                    viewHolder.rowType.setIconColorResource(R.color.edx_brand_primary_base);
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

        viewHolder.rowType.setIcon(FontAwesomeIcons.fa_film);
        viewHolder.numOfVideoAndDownloadArea.setVisibility(View.VISIBLE);
        viewHolder.bulkDownload.setVisibility(View.VISIBLE);

        final DownloadEntry videoData =  unit.getDownloadEntry(storage);

        viewHolder.rowSubtitlePanel.setVisibility(View.VISIBLE);
        viewHolder.rowSubtitle.setVisibility(View.VISIBLE);
        viewHolder.rowSubtitle.setText(videoData.getDurationReadable());

        dbStore.getWatchedStateForVideoId(videoData.videoId,
                new DataCallback<DownloadEntry.WatchedState>(true) {
                    @Override
                    public void onResult(DownloadEntry.WatchedState result) {
                        if (result != null && result == DownloadEntry.WatchedState.WATCHED) {
                            viewHolder.rowType.setIconColorResource(R.color.edx_grayscale_neutral_base);
                        } else {
                            viewHolder.rowType.setIconColorResource(R.color.edx_brand_primary_base);
                        }
                    }

                    @Override
                    public void onFail(Exception ex) {
                        logger.error(ex);
                    }
                });

        if (videoData.isVideoForWebOnly()) {
            viewHolder.numOfVideoAndDownloadArea.setVisibility(View.GONE);
        }
        else {
            viewHolder.numOfVideoAndDownloadArea.setVisibility(View.VISIBLE);
            dbStore.getDownloadedStateForVideoId(videoData.videoId,
                    new DataCallback<DownloadEntry.DownloadedState>(true) {
                        @Override
                        public void onResult(DownloadEntry.DownloadedState state) {
                            if (state == null || state == DownloadEntry.DownloadedState.ONLINE) {
                                // not yet downloaded
                                setRowStateOnDownload(viewHolder, DownloadEntry.DownloadedState.ONLINE,
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mDownloadListener.download(videoData);
                                            }
                                        });
                            } else if (state == DownloadEntry.DownloadedState.DOWNLOADING) {
                                // may be download in progress
                                setRowStateOnDownload(viewHolder, DownloadEntry.DownloadedState.DOWNLOADING,
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mDownloadListener.viewDownloadsStatus();
                                            }
                                        });
                            } else if (state == DownloadEntry.DownloadedState.DOWNLOADED) {
                                setRowStateOnDownload(viewHolder, DownloadEntry.DownloadedState.DOWNLOADED, null);
                            }
                        }

                        @Override
                        public void onFail(Exception ex) {
                            logger.error(ex);
                            viewHolder.bulkDownload.setVisibility(View.VISIBLE);
                        }
                    });
        }

    }

    private View getRowViewForContainer(int position, View convertView, ViewGroup parent,
                                        final SectionRow row) {
        final CourseComponent component = row.component;
        String courseId = component.getCourseId();
        BlockPath path = component.getPath();
        //FIXME - we should add a new column in database - pathinfo.
        //then do the string match to get the record
        String chapterId = path.get(1) == null ? "" : path.get(1).getDisplayName();
        String sequentialId = path.get(2) == null ? "" : path.get(2).getDisplayName();

        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.rowTitle.setText(component.getDisplayName());
        holder.numOfVideoAndDownloadArea.setVisibility(View.VISIBLE);
        if (component.isGraded()) {
            holder.bulkDownload.setVisibility(View.INVISIBLE);
            holder.rowSubtitlePanel.setVisibility(View.VISIBLE);
            holder.rowSubtitleIcon.setVisibility(View.VISIBLE);
            holder.rowSubtitle.setVisibility(View.VISIBLE);
            holder.rowSubtitle.setText(component.getFormat());
        }

        final int totalDownloadableVideos = component.getDownloadableVideosCount(storage);
        // support video download for video type excluding the ones only viewable on web
        if (totalDownloadableVideos == 0) {
            holder.numOfVideoAndDownloadArea.setVisibility(View.GONE);
        } else {
            holder.bulkDownload.setVisibility(View.VISIBLE);
            holder.noOfVideos.setVisibility(View.VISIBLE);
            holder.noOfVideos.setText("" + totalDownloadableVideos);

            Integer downloadedCount = dbStore.getDownloadedVideosCountForSection(courseId,
                    chapterId, sequentialId, null);

            if (downloadedCount == totalDownloadableVideos) {
                holder.noOfVideos.setVisibility(View.VISIBLE);
                setRowStateOnDownload(holder, DownloadEntry.DownloadedState.DOWNLOADED, null);
            } else if (dbStore.getDownloadingVideosCountForSection(courseId, chapterId,
                    sequentialId, null) + downloadedCount == totalDownloadableVideos) {
                holder.noOfVideos.setVisibility(View.GONE);
                setRowStateOnDownload(holder, DownloadEntry.DownloadedState.DOWNLOADING,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View downloadView) {
                                mDownloadListener.viewDownloadsStatus();
                            }
                        });
            } else {
                holder.noOfVideos.setVisibility(View.VISIBLE);
                setRowStateOnDownload(holder, DownloadEntry.DownloadedState.ONLINE,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View downloadView) {
                                mDownloadListener.download(component.getVideos());
                            }
                        });
            }
        }

        return convertView;
    }

    /**
     * Makes various changes to the row based on a video element's download status
     *
     * @param row      ViewHolder of the row view
     * @param state    current state of video download
     * @param listener the listener to attach to the video download button
     */
    private void setRowStateOnDownload(ViewHolder row, DownloadEntry.DownloadedState state
            , View.OnClickListener listener) {
        switch (state) {
            case DOWNLOADING:
                row.bulkDownload.setIcon(FontAwesomeIcons.fa_spinner);
                row.bulkDownload.setIconAnimation(Animation.PULSE);
                row.bulkDownload.setIconColorResource(R.color.edx_brand_primary_base);
                break;
            case DOWNLOADED:
                row.bulkDownload.setIcon(FontAwesomeIcons.fa_check);
                row.bulkDownload.setIconAnimation(Animation.NONE);
                row.bulkDownload.setIconColorResource(R.color.edx_grayscale_neutral_base);
                break;
            case ONLINE:
                row.bulkDownload.setIcon(FontAwesomeIcons.fa_arrow_down);
                row.bulkDownload.setIconAnimation(Animation.NONE);
                row.bulkDownload.setIconColorResource(R.color.edx_grayscale_neutral_base);
                break;
        }
        row.numOfVideoAndDownloadArea.setOnClickListener(listener);
    }

    public  View getHeaderView(int position, View convertView, ViewGroup parent){
        final SectionRow row = this.getItem(position);
        TextView titleView = (TextView)convertView.findViewById(R.id.row_header);
        View separator = convertView.findViewById(R.id.row_separator);
        titleView.setText(row.component.getDisplayName());
        if ( position == 0) {
            separator.setVisibility(View.GONE);
        } else {
            separator.setVisibility(View.VISIBLE);
        }
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

    /**
     * if the app is in the video-only mode, some unit will not show up
     */
    public boolean hasFilteredUnits(){
        return this.numOfTotalUnits > mData.size();
    }

    public ViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.rowType = (IconImageView) convertView
                .findViewById(R.id.row_type);
        holder.rowTitle = (TextView) convertView
                .findViewById(R.id.row_title);
        holder.rowSubtitle = (TextView) convertView
                .findViewById(R.id.row_subtitle);
        holder.rowSubtitleIcon = (IconImageView) convertView
                .findViewById(R.id.row_subtitle_icon);
        holder.rowSubtitleIcon.setIconColorResource(R.color.edx_grayscale_neutral_light);
        holder.noOfVideos = (TextView) convertView
                .findViewById(R.id.no_of_videos);
        holder.bulkDownload = (IconImageView) convertView
                .findViewById(R.id.bulk_download);
        holder.bulkDownload.setIconColorResource(R.color.edx_grayscale_neutral_base);
        holder.numOfVideoAndDownloadArea = (LinearLayout) convertView
                .findViewById(R.id.bulk_download_layout);
        holder.rowSubtitlePanel =convertView.findViewById(R.id.row_subtitle_panel);
        holder.halfSeparator = convertView.findViewById(R.id.row_half_separator);
        holder.wholeSeparator = convertView.findViewById(R.id.row_whole_separator);

        // Accessibility
        ViewCompat.setImportantForAccessibility(holder.rowSubtitle, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);

        return holder;
    }

    public static class ViewHolder {
        IconImageView rowType;
        TextView rowTitle;
        TextView rowSubtitle;
        IconImageView rowSubtitleIcon;
        IconImageView bulkDownload;
        TextView noOfVideos;
        LinearLayout numOfVideoAndDownloadArea;
        View rowSubtitlePanel;
        View halfSeparator;
        View wholeSeparator;
    }

    public static class SectionRow {
        public static final int ITEM = 0;
        public static final int SECTION = 1;

        public final int type;
        public final boolean topComponent;
        public final CourseComponent component;

        public SectionRow(int type, CourseComponent component) {
            this(type, false, component);
        }

        public SectionRow(int type, boolean topComponent, CourseComponent component) {
            this.type = type;
            this.topComponent = topComponent;
            this.component = component;
        }
    }

    public int getPositionByItemId(String itemId){
        int size = getCount();
        for (int i=0; i<size; i++) {
            if (getItem(i).component.getId().equals(itemId))
                return i;
        }
        return -1;
    }
}

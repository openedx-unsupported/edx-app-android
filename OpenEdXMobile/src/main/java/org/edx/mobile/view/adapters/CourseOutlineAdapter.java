package org.edx.mobile.view.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.vipulasri.timelineview.LineType;
import com.github.vipulasri.timelineview.TimelineView;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.internal.Animation;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.course.AudioBlockModel;
import org.edx.mobile.model.course.BlockPath;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.DiscussionBlockModel;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.course.HtmlBlockModel;
import org.edx.mobile.model.course.IBlock;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.TimeZoneUtils;
import org.edx.mobile.view.custom.IconImageViewXml;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Used for pinned behavior.
 */
public class CourseOutlineAdapter extends BaseAdapter {

    public static final String DOWNLOAD_TAG = "downloaded";
    private final Logger logger = new Logger(getClass().getName());

    public interface DownloadListener {
        void download(List<CourseComponent> models);

        void download(DownloadEntry videoData);

        void viewDownloadsStatus();
    }

    private Context context;
    private CourseComponent rootComponent;
    private LayoutInflater mInflater;
    private List<SectionRow> mData;

    private IDatabase dbStore;
    private IStorage storage;
    private DownloadListener mDownloadListener;
    private Config config;

    private int lastAccessedUnitPosition = -1;
    public Integer selectedItemPosition;

    public CourseOutlineAdapter(Context context, Config config, IDatabase dbStore, IStorage storage,
                                DownloadListener listener) {
        this.context = context;
        this.config = config;
        this.dbStore = dbStore;
        this.storage = storage;
        this.mDownloadListener = listener;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = new ArrayList();
        selectedItemPosition = -1;
    }

    @Override
    public int getItemViewType(int position) {
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
        if (position < 0 || position >= mData.size())
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
                convertView = mInflater.inflate(R.layout.item_section_detail, parent, false);
                // apply a tag to this list row
                ViewHolder tag = getTag(convertView);
                convertView.setTag(tag);
                break;
            }
            case SectionRow.SECTION: {
                convertView = mInflater.inflate(R.layout.item_section_header, parent, false);
                break;
            }
            default: {
                throw new IllegalArgumentException(String.valueOf(type));
            }
        }
        //}

        switch (type) {
            case SectionRow.ITEM: {
                return getRowView(position, convertView);
            }
            case SectionRow.SECTION: {
                return getHeaderView(position, convertView);
            }
            default: {
                throw new IllegalArgumentException(String.valueOf(type));
            }
        }
    }

    /**
     * component can be null.
     *
     * @IComponent component should be ICourse
     */
    public void setData(CourseComponent component) {
        if (component != null && !component.isContainer())
            return;
        this.rootComponent = component;
        mData.clear();
        if (rootComponent != null) {
            for (IBlock block : rootComponent.getChildren()) {
                CourseComponent comp = (CourseComponent) block;
                if (comp.isContainer()) {
                    SectionRow header = new SectionRow(SectionRow.SECTION, comp);
                    mData.add(header);
                    for (IBlock childBlock : comp.getChildren()) {
                        CourseComponent child = (CourseComponent) childBlock;
                        SectionRow row = new SectionRow(SectionRow.ITEM, false, child);
                        mData.add(row);
                    }
                } else {
                    SectionRow row = new SectionRow(SectionRow.ITEM, true, comp);
                    mData.add(row);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void reloadData() {
        if (this.rootComponent != null)
            setData(this.rootComponent);
    }

    public View getRowView(int position, View convertView) {
        final SectionRow row = this.getItem(position);
        final CourseComponent c = row.component;
        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.subSectionTitleTV.setText(c.getDisplayName());

        if (!c.isContainer()) {
            getRowViewForLeaf(viewHolder, row, position);

        } else {
            getRowViewForContainer(viewHolder, row, position);
        }

        //Todo check content availability type and set download/downloaded icons accordingly
        return convertView;
    }

    private void getRowViewForLeaf(ViewHolder viewHolder,
                                   final SectionRow row, final int position) {
        final CourseComponent unit = row.component;
        viewHolder.subSectionDescriptionTV.setVisibility(View.GONE);
        viewHolder.timelineViewMarker.setVisibility(View.GONE);
        viewHolder.blockTypeIcon.setVisibility(View.VISIBLE);
        if (selectedItemPosition == position) {
            viewHolder.subSectionTitleTV.setTextColor(ContextCompat.getColor(context, R.color.white));
            viewHolder.rowCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.philu_primary));
        } else {
            viewHolder.subSectionTitleTV.setTextColor(ContextCompat.getColor(context, R.color.philu_primary));
        }

        if (row.component instanceof VideoBlockModel) {
            viewHolder.blockTypeIcon.setImageResource(R.drawable.ic_video_media);
            final DownloadEntry videoData = ((VideoBlockModel) row.component).getDownloadEntry(storage);
            if (null != videoData) {
                updateUIForDownloadableMedia(viewHolder, videoData);
            } else {
                viewHolder.courseAvailabilityStatusIcon.setVisibility(View.GONE);
            }
        } else if (row.component instanceof AudioBlockModel) {
            viewHolder.blockTypeIcon.setIcon(FontAwesomeIcons.fa_volume_up);
            final DownloadEntry audioData = ((AudioBlockModel) row.component).getDownloadEntry(storage);
            if (null != audioData) {
                updateUIForDownloadableMedia(viewHolder, audioData);
            } else {
                viewHolder.courseAvailabilityStatusIcon.setVisibility(View.GONE);
            }
        } else if (row.component instanceof HtmlBlockModel) {
            viewHolder.blockTypeIcon.setImageResource(R.drawable.ic_text_media);
            viewHolder.courseAvailabilityStatusIcon.setVisibility(View.GONE);
        } else if (config.isDiscussionsEnabled() && row.component instanceof DiscussionBlockModel) {
            viewHolder.blockTypeIcon.setIcon(FontAwesomeIcons.fa_comments_o);
            checkAccessStatus(viewHolder, unit);
        } else if (!unit.isMultiDevice()) {
            // If we reach here & the type is VIDEO, it means the video is webOnly
            viewHolder.courseAvailabilityStatusIcon.setVisibility(View.INVISIBLE);
            viewHolder.blockTypeIcon.setIcon(FontAwesomeIcons.fa_laptop);
        } else {
            viewHolder.courseAvailabilityStatusIcon.setVisibility(View.INVISIBLE);
            if (unit.getType() == BlockType.PROBLEM) {
                viewHolder.blockTypeIcon.setIcon(FontAwesomeIcons.fa_list);
            } else {
                viewHolder.blockTypeIcon.setIcon(FontAwesomeIcons.fa_file_o);
            }
            checkAccessStatus(viewHolder, unit);
        }

        addRequiredPadding(viewHolder.cardViewContainer, position);
    }

    private void addRequiredPadding(FrameLayout view, int position) {
        int padding = (int) context.getResources().getDimension(R.dimen.widget_margin);
        view.setPadding(view.getPaddingLeft(),
                shouldAddTopPadding(position) ? padding : 0,
                view.getPaddingRight(),
                shouldAddBottomPadding(position) ? padding : 0);
    }

    private boolean shouldAddBottomPadding(int position) {
        return position < mData.size() &&
                (position == mData.size() - 1 || getItemViewType(position + 1) == SectionRow.SECTION);
    }

    private boolean shouldAddTopPadding(int position) {
        return position > 0 && getItemViewType(position - 1) == SectionRow.SECTION;
    }

    private void checkAccessStatus(final ViewHolder viewHolder, final CourseComponent unit) {
        dbStore.isUnitAccessed(new DataCallback<Boolean>(true) {
            @Override
            public void onResult(Boolean accessed) {
                if (accessed) {
                    viewHolder.blockTypeIcon.setIconColorResource(R.color.edx_brand_gray_accent);
                } else {
                    viewHolder.blockTypeIcon.setIconColorResource(R.color.edx_brand_primary_base);
                }
            }

            @Override
            public void onFail(Exception ex) {
                logger.error(ex);
            }
        }, unit.getId());
    }

    private void updateUIForDownloadableMedia(@NonNull final ViewHolder viewHolder, @NonNull final DownloadEntry downloadEntry) {
        if (downloadEntry.getDuration() > 0L) {
            viewHolder.subSectionDescriptionTV.setVisibility(View.VISIBLE);
            viewHolder.subSectionDescriptionTV.setText(downloadEntry.getDurationReadable());
        }
        if (downloadEntry.getSize() > 0L) {
            viewHolder.subSectionDescriptionTV.setVisibility(View.VISIBLE);
            viewHolder.subSectionDescriptionTV.append(String.format(Locale.getDefault(), " | %s",
                    MemoryUtil.format(context, downloadEntry.getSize())));
        }

        dbStore.getWatchedStateForVideoId(downloadEntry.blockId,
                new DataCallback<DownloadEntry.WatchedState>(true) {
                    @Override
                    public void onResult(DownloadEntry.WatchedState result) {
                        if (result != null && result == DownloadEntry.WatchedState.WATCHED) {
                            viewHolder.blockTypeIcon.setIconColorResource(R.color.edx_brand_gray_accent);
                        } else {
                            viewHolder.blockTypeIcon.setIconColorResource(R.color.edx_brand_primary_base);
                        }
                    }

                    @Override
                    public void onFail(Exception ex) {
                        logger.error(ex);
                    }
                });

        if (downloadEntry.isVideoForWebOnly()) {
            viewHolder.courseAvailabilityStatusIcon.setVisibility(View.GONE);
        } else {
            viewHolder.courseAvailabilityStatusIcon.setVisibility(View.VISIBLE);
            dbStore.getDownloadedStateForVideoId(downloadEntry.blockId,
                    new DataCallback<DownloadEntry.DownloadedState>(true) {
                        @Override
                        public void onResult(DownloadEntry.DownloadedState state) {
                            if (state == null || state == DownloadEntry.DownloadedState.ONLINE) {
                                // not yet downloaded
                                setRowStateOnDownload(viewHolder, DownloadEntry.DownloadedState.ONLINE,
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mDownloadListener.download(downloadEntry);
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
                            viewHolder.courseAvailabilityStatusIcon.setVisibility(View.VISIBLE);
                        }
                    });
        }

    }

    private void getRowViewForContainer(ViewHolder viewHolder,
                                        final SectionRow row, final int position) {
        final CourseComponent currentCourseComponent = row.component;
        String courseId = currentCourseComponent.getCourseId();
        BlockPath path = currentCourseComponent.getPath();
        //FIXME - we should add a new column in database - pathinfo.
        //then do the string match to get the record
        String chapterId = path.get(1) == null ? "" : path.get(1).getDisplayName();
        String sequentialId = path.get(2) == null ? "" : path.get(2).getDisplayName();

        viewHolder.blockTypeIcon.setVisibility(View.GONE);
        List<IBlock> blocks = currentCourseComponent.getChildren();
        viewHolder.courseAvailabilityStatusIcon.setImageResource(R.drawable.ic_download_media);

        //This block will check and set Sub topics
        if (blocks != null && blocks.size() > 0) {
            viewHolder.subSectionTitleTV.setText(blocks.get(0).getDisplayName());
            if (blocks.size() == 1) {
                viewHolder.subSectionDescriptionTV.setVisibility(View.GONE);
                viewHolder.multipleItemsCV.setVisibility(View.GONE);
            } else {
                viewHolder.subSectionDescriptionTV.append(String.format(Locale.getDefault(), " + %d %s",
                        (blocks.size() - 1), context.getString(R.string.sub_topics_text)));
                viewHolder.multipleItemsCV.setVisibility(View.VISIBLE);
            }
        }

        //This block is used to handle timeline marker and row title text color for items before last accessed on base of last accessed item

        //This block is used to handle timeline marker and row title text color if current item is last accessed
        if (lastAccessedUnitPosition > position) {
            viewHolder.subSectionTitleTV.setTextColor(ContextCompat.getColor(context, R.color.philu_primary));
            viewHolder.timelineViewMarker.setMarkerSize((int) context.getResources().getDimension(R.dimen.timeline_marker_size_small));
        } else if (lastAccessedUnitPosition == position) {
            viewHolder.timelineViewMarker.setMarkerSize((int) context.getResources().getDimension(R.dimen.timeline_marker_size_large));
            viewHolder.subSectionTitleTV.setTextColor(ContextCompat.getColor(context, R.color.philu_primary));
            viewHolder.subSectionTitleTV.setTypeface(null, Typeface.BOLD);
        }

        viewHolder.timelineViewMarker.setMarker(ContextCompat.getDrawable(context, R.drawable.ic_timeline_marker_filled));

        //Manage if the line should follow towards next row or not (NO for the last row) on base of obtained marker type
        int markerType = getTypeForTimelineMarker(position);
        viewHolder.timelineViewMarker.initLine(markerType);

        // This check will check if item is selected through long item click on list and mark view changes
        if (selectedItemPosition == position) {
            viewHolder.subSectionTitleTV.setTextColor(ContextCompat.getColor(context, R.color.white));
            viewHolder.rowCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.philu_primary));
        }

        final int totalDownloadableMedia = currentCourseComponent.getDownloadableMediaCount();
        // support video download for video type excluding the ones only viewable on web
        if (totalDownloadableMedia == 0) {
            viewHolder.courseAvailabilityStatusIcon.setVisibility(View.GONE);
        } else {
            viewHolder.courseAvailabilityStatusIcon.setVisibility(View.VISIBLE);

            Integer downloadedCount = dbStore.getDownloadedMediaCountForSection(courseId,
                    chapterId, sequentialId, null);

            if (downloadedCount == totalDownloadableMedia) {
                viewHolder.courseAvailabilityStatusIcon.setVisibility(View.VISIBLE);
                //                holder.noOfVideos.setVisibility(View.VISIBLE);
                setRowStateOnDownload(viewHolder, DownloadEntry.DownloadedState.DOWNLOADED, null);
            } else if (dbStore.getDownloadingVideosCountForSection(courseId, chapterId,
                    sequentialId, null) + downloadedCount == totalDownloadableMedia) {
                setRowStateOnDownload(viewHolder, DownloadEntry.DownloadedState.DOWNLOADING,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View downloadView) {
                                mDownloadListener.viewDownloadsStatus();
                            }
                        });
            } else {
                setRowStateOnDownload(viewHolder, DownloadEntry.DownloadedState.ONLINE,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View downloadView) {
                                mDownloadListener.download(currentCourseComponent.getDownloadableMedia());
                            }
                        });
            }
        }

        addRequiredPadding(viewHolder.cardViewContainer, position);
    }

    //This function will tell what marker type should be used
    private int getTypeForTimelineMarker(int position) {
        final SectionRow row = this.getItem(position);

        int typeToReturn;

        SectionRow previousRow = this.getItem(position - 1);
        SectionRow nextSectionRow = this.getItem(position + 1);
        //no previous item
        if (previousRow == null) {
            return 0;
        }
        //list has reached its end and we don't need to show end line
        if (nextSectionRow == null) {
            typeToReturn = LineType.END;
        }
        //list is continuous so we should show start and end line of the timeline marker
        else {
            typeToReturn = LineType.NORMAL;
        }

        return typeToReturn;
    }

    private String getFormattedDueDate(final String date) throws IllegalArgumentException {
        final SimpleDateFormat dateFormat;
        final Date dueDate = DateUtil.convertToDate(date);
        if (android.text.format.DateUtils.isToday(dueDate.getTime())) {
            dateFormat = new SimpleDateFormat("HH:mm");
            String formattedDate = ResourceUtil.getFormattedString(context.getResources(), R.string.due_date_today,
                    "due_date", dateFormat.format(dueDate)).toString();
            formattedDate += " " + TimeZoneUtils.getTimeZoneAbbreviation(TimeZone.getDefault());
            return formattedDate;
        } else {
            dateFormat = new SimpleDateFormat("MMM dd, yyyy");
            return ResourceUtil.getFormattedString(context.getResources(), R.string.due_date_past_future,
                    "due_date", dateFormat.format(dueDate)).toString();
        }
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
                row.courseAvailabilityStatusIcon.setIcon(FontAwesomeIcons.fa_spinner);
                row.courseAvailabilityStatusIcon.setIconAnimation(Animation.PULSE);
                row.courseAvailabilityStatusIcon.setIconColorResource(R.color.white);
                break;
            case DOWNLOADED:
                row.courseAvailabilityStatusIcon.setImageResource(R.drawable.ic_done);
                row.courseAvailabilityStatusIcon.setIconAnimation(Animation.NONE);
                row.courseAvailabilityStatusIcon.setIconColorResource(R.color.white);
                row.courseAvailabilityStatusIcon.setBackgroundColor(context.getResources().getColor(R.color.philu_grey_bg));
                row.courseAvailabilityStatusIcon.setTag(DOWNLOAD_TAG);
                break;
            case ONLINE:
                row.courseAvailabilityStatusIcon.setImageResource(R.drawable.ic_download_media);
                row.courseAvailabilityStatusIcon.setBackgroundColor(context.getResources().getColor(R.color.philu_blue_bg));
                row.courseAvailabilityStatusIcon.setIconAnimation(Animation.NONE);
                break;
        }
        row.courseAvailabilityStatusIcon.setOnClickListener(listener);
        if (listener == null) {
            row.courseAvailabilityStatusIcon.setClickable(false);
        }
    }

    public View getHeaderView(int position, View convertView) {
        final SectionRow row = this.getItem(position);
        ((TextView) convertView).setText(row.component.getDisplayName());

        return convertView;
    }

    public ViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.sectionTitleTV = (TextView) convertView.findViewById(R.id.section_title);
        holder.subSectionTitleTV = (TextView) convertView.findViewById(R.id.subsection_title_tv);
        holder.subSectionDescriptionTV = (TextView) convertView.findViewById(R.id.subsection_description);
        holder.timelineViewMarker = (TimelineView) convertView.findViewById(R.id.subsection_timeline_marker);
        holder.courseAvailabilityStatusIcon = (IconImageViewXml) convertView.findViewById(R.id.course_availability_status_icon);
        holder.multipleItemsCV = (CardView) convertView.findViewById(R.id.multiple_items_cv);
        holder.blockTypeIcon = (IconImageViewXml) convertView.findViewById(R.id.block_type_icon);
        holder.cardViewContainer = (FrameLayout) convertView.findViewById(R.id.card_holder);
        holder.rowCardView = (CardView) convertView.findViewById(R.id.subsection_row_cv);

        return holder;
    }

    public static class ViewHolder {
        TextView subSectionTitleTV, sectionTitleTV, subSectionDescriptionTV;
        TimelineView timelineViewMarker;
        IconImageViewXml courseAvailabilityStatusIcon, blockTypeIcon;
        CardView multipleItemsCV;
        FrameLayout cardViewContainer;
        CardView rowCardView;
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

    public int getPositionByItemId(String itemId) {
        int size = getCount();
        for (int i = 0; i < size; i++) {
            if (getItem(i).component.getId().equals(itemId))
                return i;
        }
        return -1;
    }

    public void setLastAccessedId(String lastAccessedId) {
        updateLastAccessedUnitPosition(lastAccessedId);
        notifyDataSetChanged();
    }

    private void updateLastAccessedUnitPosition(String lastAccessedUnitId) {
        if (!android.text.TextUtils.isEmpty(lastAccessedUnitId)) {
            for (SectionRow row : mData) {
                if (row.component.getId().equals(lastAccessedUnitId)) {
                    lastAccessedUnitPosition = mData.indexOf(row);
                }
            }
        }
    }

    /**
     * This function will be used to add the selected items to be marked on ListView item long click(Only downloaded media/Videos)
     *
     * @param position
     */
    public void selectItemAtPosition(int position) {
        selectedItemPosition = position;
        notifyDataSetChanged();
    }

    /**
     * This function will remove all selected items to be unmarked/unselected on ListView (on ActionItemView dismissal)
     */
    public void clearSelectedItemPosition() {
        selectedItemPosition = -1;
        notifyDataSetChanged();

    }
}

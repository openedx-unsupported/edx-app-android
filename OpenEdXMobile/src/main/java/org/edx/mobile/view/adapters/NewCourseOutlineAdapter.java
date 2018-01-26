package org.edx.mobile.view.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.internal.Animation;
import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.BlockPath;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.DiscussionBlockModel;
import org.edx.mobile.model.course.HasDownloadEntry;
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
import org.edx.mobile.util.images.TopAnchorFillWidthTransformation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class NewCourseOutlineAdapter extends BaseAdapter {

    private final Logger logger = new Logger(getClass().getName());

    public interface DownloadListener {
        void download(List<CourseComponent> models);

        void download(DownloadEntry videoData);

        void viewDownloadsStatus();
    }

    private Context context;
    private CourseComponent rootComponent;
    private LayoutInflater inflater;
    private List<SectionRow> adapterData;

    private IEdxEnvironment environment;
    private Config config;
    private IDatabase dbStore;
    private IStorage storage;
    private EnrolledCoursesResponse courseData;
    private DownloadListener downloadListener;

    public NewCourseOutlineAdapter(Context context, EnrolledCoursesResponse courseData,
                                   IEdxEnvironment environment, NewCourseOutlineAdapter.DownloadListener listener,
                                   boolean isOnCourseOutline) {
        this.context = context;
        this.environment = environment;
        this.config = environment.getConfig();
        this.dbStore = environment.getDatabase();
        this.storage = environment.getStorage();
        this.courseData = courseData;
        this.downloadListener = listener;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        adapterData = new ArrayList();
        if (isOnCourseOutline) {
            adapterData.add(new SectionRow(SectionRow.COURSE_CARD, null));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).type;
    }

    @Override
    public int getViewTypeCount() {
        return SectionRow.NUM_OF_SECTION_ROWS;
    }

    @Override
    public int getCount() {
        return adapterData.size();
    }

    @Override
    public SectionRow getItem(int position) {
        if (position < 0 || position >= adapterData.size())
            return null;
        return adapterData.get(position);
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
        final int type = getItemViewType(position);

        // FIXME: Re-enable row recycling in favor of better DB communication [MA-1640]
        //if (convertView == null) {
        switch (type) {
            case SectionRow.ITEM: {
                convertView = inflater.inflate(R.layout.row_course_outline_list, parent, false);
                // apply a tag to this list row
                ViewHolder tag = getTag(convertView);
                convertView.setTag(tag);
                return getRowView(position, convertView);
            }
            case SectionRow.SECTION: {
                convertView = inflater.inflate(R.layout.row_section_header, parent, false);
                return getHeaderView(position, convertView);
            }
            case SectionRow.COURSE_CARD: {
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.row_course_card, parent, false);
                }
                return getCardView(convertView);
            }
            case SectionRow.LAST_ACCESSED_ITEM: {
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.row_last_accessed, parent, false);
                }
                return getLastAccessedView(position, convertView);
            }
            default: {
                throw new IllegalArgumentException(String.valueOf(type));
            }
        }
        //}
    }

    /**
     * Set the data for adapter to populate the listview.
     *
     * @param component The CourseComponent to extract data from.
     */
    public void setData(@Nullable CourseComponent component) {
        if (component != null && !component.isContainer())
            return;//
        this.rootComponent = component;
        clearCourseOutlineData();
        if (rootComponent != null) {
            List<IBlock> children = rootComponent.getChildren();
            for (IBlock block : children) {
                CourseComponent comp = (CourseComponent) block;
                if (comp.isContainer()) {
                    SectionRow header = new SectionRow(SectionRow.SECTION, comp);
                    adapterData.add(header);
                    for (IBlock childBlock : comp.getChildren()) {
                        CourseComponent child = (CourseComponent) childBlock;
                        SectionRow row = new SectionRow(SectionRow.ITEM, false, child);
                        adapterData.add(row);
                    }
                } else {
                    SectionRow row = new SectionRow(SectionRow.ITEM, true, comp);
                    adapterData.add(row);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Clear all the course outline rows.
     */
    private void clearCourseOutlineData() {
        if (adapterData.isEmpty()) return;

        int startIndex = 0;
        // If data for course card is available on the first index, we don't want that cleared
        if (adapterData.size() >= 1 && adapterData.get(0).type == SectionRow.COURSE_CARD) {
            startIndex++;
        }
        // If data for last accessed component is available on the second index, we don't want that cleared
        if (adapterData.size() >= 2 && adapterData.get(1).type == SectionRow.LAST_ACCESSED_ITEM) {
            startIndex++;
        }
        // Selectively clear adapter's data from a specific index onwards.
        adapterData.subList(startIndex, adapterData.size()).clear();
    }

    /**
     * Tells if the adapter has any items related to the courseware.
     *
     * @return <code>true</code> if there are course items, <code>false</code> otherwise.
     */
    public boolean hasCourseData() {
        if (adapterData.isEmpty()) return false;

        int rowsToIgnore = 0;
        // If data for course card is available on the first index, we need to skip it
        if (adapterData.size() >= 1 && adapterData.get(0).type == SectionRow.COURSE_CARD) {
            rowsToIgnore++;
        }
        // If data for last accessed component is available on the second index, we need to skip it
        if (adapterData.size() >= 2 && adapterData.get(1).type == SectionRow.LAST_ACCESSED_ITEM) {
            rowsToIgnore++;
        }
        // Means we have atleast 1 courseware item other than the course card and last accessed item
        return adapterData.size() > rowsToIgnore;
    }

    public void reloadData() {
        if (this.rootComponent != null)
            setData(this.rootComponent);
    }

    public View getRowView(int position, View convertView) {
        final SectionRow row = this.getItem(position);
        final SectionRow nextRow = this.getItem(position + 1);
        final CourseComponent component = row.component;
        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        if (nextRow == null) {
            viewHolder.halfSeparator.setVisibility(View.GONE);
            viewHolder.wholeSeparator.setVisibility(View.VISIBLE);
        } else {
            viewHolder.wholeSeparator.setVisibility(View.GONE);
            boolean isLastChildInBlock = !row.component.getParent().getId().equals(nextRow.component.getParent().getId());
            if (isLastChildInBlock) {
                viewHolder.halfSeparator.setVisibility(View.GONE);
            } else {
                viewHolder.halfSeparator.setVisibility(View.VISIBLE);
            }
        }

        viewHolder.rowType.setVisibility(View.GONE);
        viewHolder.rowSubtitleIcon.setVisibility(View.GONE);
        viewHolder.rowSubtitle.setVisibility(View.GONE);
        viewHolder.rowSubtitleDueDate.setVisibility(View.GONE);
        viewHolder.rowSubtitlePanel.setVisibility(View.GONE);
        viewHolder.numOfVideoAndDownloadArea.setVisibility(View.GONE);

        if (component.isContainer()) {
            getRowViewForContainer(viewHolder, row);
        } else {
            getRowViewForLeaf(viewHolder, row);
        }
        return convertView;
    }

    private void getRowViewForLeaf(ViewHolder viewHolder,
                                   final SectionRow row) {
        final CourseComponent unit = row.component;
        viewHolder.rowType.setVisibility(View.VISIBLE);
        viewHolder.rowSubtitleIcon.setVisibility(View.GONE);
        viewHolder.rowSubtitleDueDate.setVisibility(View.GONE);
        viewHolder.rowSubtitle.setVisibility(View.GONE);
        viewHolder.rowSubtitlePanel.setVisibility(View.GONE);
        viewHolder.bulkDownload.setVisibility(View.INVISIBLE);
        viewHolder.rowTitle.setText(unit.getDisplayName());

        if (row.component instanceof VideoBlockModel) {
            final DownloadEntry videoData = ((VideoBlockModel) row.component).getDownloadEntry(storage);
            if (null != videoData) {
                updateUIForVideo(viewHolder, videoData);
                return;
            }
        }
        if (config.isDiscussionsEnabled() && row.component instanceof DiscussionBlockModel) {
            viewHolder.rowType.setIcon(FontAwesomeIcons.fa_comments_o);
            checkAccessStatus(viewHolder, unit);
        } else if (!unit.isMultiDevice()) {
            // If we reach here & the type is VIDEO, it means the video is webOnly
            viewHolder.bulkDownload.setVisibility(View.INVISIBLE);
            viewHolder.rowType.setIcon(FontAwesomeIcons.fa_laptop);
            viewHolder.rowType.setIconColorResource(R.color.edx_brand_gray_accent);
        } else {
            viewHolder.bulkDownload.setVisibility(View.INVISIBLE);
            if (unit.getType() == BlockType.PROBLEM) {
                viewHolder.rowType.setIcon(FontAwesomeIcons.fa_list);
            } else {
                viewHolder.rowType.setIcon(FontAwesomeIcons.fa_file_o);
            }
            checkAccessStatus(viewHolder, unit);
        }
    }

    private void checkAccessStatus(final ViewHolder viewHolder, final CourseComponent unit) {
        dbStore.isUnitAccessed(new DataCallback<Boolean>(true) {
            @Override
            public void onResult(Boolean accessed) {
                if (accessed) {
                    viewHolder.rowType.setIconColorResource(R.color.edx_brand_gray_accent);
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

    private void updateUIForVideo(@NonNull final ViewHolder viewHolder, @NonNull final DownloadEntry videoData) {
        viewHolder.rowType.setIcon(FontAwesomeIcons.fa_film);
        viewHolder.numOfVideoAndDownloadArea.setVisibility(View.VISIBLE);
        viewHolder.bulkDownload.setVisibility(View.VISIBLE);
        viewHolder.rowSubtitlePanel.setVisibility(View.VISIBLE);
        if (videoData.getDuration() > 0L) {
            viewHolder.rowSubtitle.setVisibility(View.VISIBLE);
            viewHolder.rowSubtitle.setText(videoData.getDurationReadable());
        }
        if (videoData.getSize() > 0L) {
            viewHolder.rowSubtitleDueDate.setVisibility(View.VISIBLE);
            viewHolder.rowSubtitleDueDate.setText(MemoryUtil.format(context, videoData.getSize()));
            // Set appropriate right margin of subtitle
            final int rightMargin = (int) context.getResources().getDimension(R.dimen.widget_margin_double);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                    viewHolder.rowSubtitle.getLayoutParams();
            params.setMargins(0, 0, rightMargin, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                params.setMarginEnd(rightMargin);
            }
        }

        dbStore.getWatchedStateForVideoId(videoData.blockId,
                new DataCallback<DownloadEntry.WatchedState>(true) {
                    @Override
                    public void onResult(DownloadEntry.WatchedState result) {
                        if (result != null && result == DownloadEntry.WatchedState.WATCHED) {
                            viewHolder.rowType.setIconColorResource(R.color.edx_brand_gray_accent);
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
        } else {
            viewHolder.numOfVideoAndDownloadArea.setVisibility(View.VISIBLE);
            dbStore.getDownloadedStateForVideoId(videoData.blockId,
                    new DataCallback<DownloadEntry.DownloadedState>(true) {
                        @Override
                        public void onResult(DownloadEntry.DownloadedState state) {
                            if (state == null || state == DownloadEntry.DownloadedState.ONLINE) {
                                // not yet downloaded
                                setRowStateOnDownload(viewHolder, DownloadEntry.DownloadedState.ONLINE,
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                downloadListener.download(videoData);
                                            }
                                        });
                            } else if (state == DownloadEntry.DownloadedState.DOWNLOADING) {
                                // may be download in progress
                                setRowStateOnDownload(viewHolder, DownloadEntry.DownloadedState.DOWNLOADING,
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                downloadListener.viewDownloadsStatus();
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

    private void getRowViewForContainer(ViewHolder holder,
                                        final SectionRow row) {
        final CourseComponent component = row.component;
        String courseId = component.getCourseId();
        BlockPath path = component.getPath();
        //FIXME - we should add a new column in database - pathinfo.
        //then do the string match to get the record
        String chapterId = path.get(1) == null ? "" : path.get(1).getDisplayName();
        String sequentialId = path.get(2) == null ? "" : path.get(2).getDisplayName();

        holder.rowTitle.setText(component.getDisplayName());
        holder.numOfVideoAndDownloadArea.setVisibility(View.VISIBLE);
        if (component.isGraded()) {
            holder.bulkDownload.setVisibility(View.INVISIBLE);
            holder.rowSubtitlePanel.setVisibility(View.VISIBLE);
            holder.rowSubtitleIcon.setVisibility(View.VISIBLE);
            holder.rowSubtitle.setVisibility(View.VISIBLE);
            holder.rowSubtitle.setText(component.getFormat());
            holder.rowSubtitle.setTypeface(holder.rowSubtitle.getTypeface(), Typeface.BOLD);
            holder.rowSubtitle.setTextColor(ContextCompat.getColor(context,
                    R.color.edx_brand_gray_dark));
            if (!TextUtils.isEmpty(component.getDueDate())) {
                try {
                    holder.rowSubtitleDueDate.setText(getFormattedDueDate(component.getDueDate()));
                    holder.rowSubtitleDueDate.setVisibility(View.VISIBLE);
                } catch (IllegalArgumentException e) {
                    logger.error(e);
                }
            }
        }

        final int totalDownloadableVideos = component.getDownloadableMediaCount();
        // support video download for video type excluding the ones only viewable on web
        if (totalDownloadableVideos == 0) {
            holder.numOfVideoAndDownloadArea.setVisibility(View.GONE);
        } else {
            holder.bulkDownload.setVisibility(View.VISIBLE);
            holder.noOfVideos.setVisibility(View.VISIBLE);
            holder.noOfVideos.setText("" + totalDownloadableVideos);

            Integer downloadedCount = dbStore.getDownloadedMediaCountForSection(courseId,
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
                                downloadListener.viewDownloadsStatus();
                            }
                        });
            } else {
                holder.noOfVideos.setVisibility(View.VISIBLE);
                setRowStateOnDownload(holder, DownloadEntry.DownloadedState.ONLINE,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View downloadView) {
                                downloadListener.download(component.getVideos());
                            }
                        });
            }
        }
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
                row.bulkDownload.setIcon(FontAwesomeIcons.fa_spinner);
                row.bulkDownload.setIconAnimation(Animation.PULSE);
                row.bulkDownload.setIconColorResource(R.color.edx_brand_primary_base);
                break;
            case DOWNLOADED:
                row.bulkDownload.setIcon(FontAwesomeIcons.fa_check);
                row.bulkDownload.setIconAnimation(Animation.NONE);
                row.bulkDownload.setIconColorResource(R.color.edx_brand_gray_accent);
                break;
            case ONLINE:
                row.bulkDownload.setIcon(FontAwesomeIcons.fa_download);
                row.bulkDownload.setIconAnimation(Animation.NONE);
                row.bulkDownload.setIconColorResource(R.color.edx_brand_gray_accent);
                break;
        }
        row.numOfVideoAndDownloadArea.setOnClickListener(listener);
        if (listener == null) {
            row.numOfVideoAndDownloadArea.setClickable(false);
        }
    }

    public View getHeaderView(int position, View convertView) {
        final SectionRow row = this.getItem(position);
        TextView titleView = (TextView) convertView.findViewById(R.id.row_header);
        View separator = convertView.findViewById(R.id.row_separator);
        titleView.setText(row.component.getDisplayName());
        if (position == 0) {
            separator.setVisibility(View.GONE);
        } else {
            separator.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    public View getCardView(View view) {
        final TextView courseTextName = (TextView) view.findViewById(R.id.course_detail_name);
        final TextView courseTextDetails = (TextView) view.findViewById(R.id.course_detail_extras);
        final ImageView headerImageView = (ImageView) view.findViewById(R.id.header_image_view);

        // Full course name should appear on the course's dashboard screen.
        courseTextName.setEllipsize(null);
        courseTextName.setSingleLine(false);

        final String headerImageUrl = courseData.getCourse().getCourse_image(environment.getConfig().getApiHostURL());
        Glide.with(context)
                .load(headerImageUrl)
                .placeholder(R.drawable.placeholder_course_card_image)
                .transform(new TopAnchorFillWidthTransformation(context))
                .into(headerImageView);

        courseTextName.setText(courseData.getCourse().getName());
        CourseEntry course = courseData.getCourse();
        courseTextDetails.setText(course.getDescriptionWithStartDate(context));

        return view;
    }

    private View getLastAccessedView(int position, View convertView) {
        final SectionRow sectionRow = getItem(position);
        final TextView lastAccessTextView = (TextView) convertView.findViewById(R.id.last_accessed_text);
        final View viewButton = convertView.findViewById(R.id.last_accessed_button);
        lastAccessTextView.setText(sectionRow.component.getDisplayName());
        viewButton.setOnClickListener(sectionRow.clickListener);
        return convertView;
    }

    /**
     * Adds last accessed course item view in the ListView.
     *
     * @param lastAccessedComponent The last accessed component.
     * @param onClickListener       The listener to invoke when the `view` button is pressed.
     */
    public void addLastAccessedView(CourseComponent lastAccessedComponent, View.OnClickListener onClickListener) {
        // Update the last accessed item, if its already there in the list
        if (adapterData.size() >= 2 && adapterData.get(1).type == SectionRow.LAST_ACCESSED_ITEM) {
            adapterData.set(1, new SectionRow(SectionRow.LAST_ACCESSED_ITEM, lastAccessedComponent, onClickListener));
        } else {
            // Add it otherwise
            adapterData.add(1, new SectionRow(SectionRow.LAST_ACCESSED_ITEM, lastAccessedComponent, onClickListener));
        }
        notifyDataSetChanged();
    }

    public ViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.rowType = (IconImageView) convertView
                .findViewById(R.id.row_type);
        holder.rowTitle = (TextView) convertView
                .findViewById(R.id.row_title);
        holder.rowSubtitle = (TextView) convertView
                .findViewById(R.id.row_subtitle);
        holder.rowSubtitleDueDate = (TextView) convertView
                .findViewById(R.id.row_subtitle_due_date);
        holder.rowSubtitleIcon = (IconImageView) convertView
                .findViewById(R.id.row_subtitle_icon);
        holder.rowSubtitleIcon.setIconColorResource(R.color.edx_brand_primary_base);
        holder.noOfVideos = (TextView) convertView
                .findViewById(R.id.no_of_videos);
        holder.bulkDownload = (IconImageView) convertView
                .findViewById(R.id.bulk_download);
        holder.bulkDownload.setIconColorResource(R.color.edx_brand_gray_accent);
        holder.numOfVideoAndDownloadArea = (LinearLayout) convertView
                .findViewById(R.id.bulk_download_layout);
        holder.rowSubtitlePanel = convertView.findViewById(R.id.row_subtitle_panel);
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
        TextView rowSubtitleDueDate;
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
        public static final int COURSE_CARD = 2;
        public static final int LAST_ACCESSED_ITEM = 3;
        // Update this count according to the section types mentioned above
        public static final int NUM_OF_SECTION_ROWS = 4;

        public final int type;
        public final boolean topComponent;
        public final CourseComponent component;
        public final View.OnClickListener clickListener;

        public SectionRow(int type, CourseComponent component) {
            this(type, false, component, null);
        }

        public SectionRow(int type, boolean topComponent, CourseComponent component) {
            this(type, topComponent, component, null);
        }

        public SectionRow(int type, CourseComponent component, View.OnClickListener listener) {
            this(type, false, component, listener);
        }

        public SectionRow(int type, boolean topComponent, CourseComponent component, View.OnClickListener listener) {
            this.type = type;
            this.topComponent = topComponent;
            this.component = component;
            this.clickListener = listener;
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
}

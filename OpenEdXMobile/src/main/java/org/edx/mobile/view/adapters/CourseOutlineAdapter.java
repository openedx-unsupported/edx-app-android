package org.edx.mobile.view.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.AuthorizationDenialReason;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.BlockPath;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.DiscussionBlockModel;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.course.IBlock;
import org.edx.mobile.model.course.SectionRow;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.storage.DownloadedVideoDeletedEvent;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.FileUtil;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.util.UiUtils;
import org.edx.mobile.util.VideoUtil;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CourseOutlineAdapter extends BaseAdapter {

    private final Logger logger = new Logger(getClass().getName());

    public interface DownloadListener {
        void download(List<? extends HasDownloadEntry> models);

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
    private boolean isVideoMode;
    private Long lastValuePropClickTime = 0L;

    public CourseOutlineAdapter(final Context context, final EnrolledCoursesResponse courseData,
                                final IEdxEnvironment environment, DownloadListener listener,
                                boolean isVideoMode, boolean isOnCourseOutline) {
        this.context = context;
        this.environment = environment;
        this.config = environment.getConfig();
        this.dbStore = environment.getDatabase();
        this.storage = environment.getStorage();
        this.courseData = courseData;
        this.downloadListener = listener;
        this.isVideoMode = isVideoMode;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        adapterData = new ArrayList();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
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
        return getItemViewType(position) == SectionRow.SUB_SECTION;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        final int type = getItemViewType(position);

        // FIXME: Revisit better DB communication and code improvements in [MA-1640]
        // INITIALIZATION
        if (convertView == null) {
            switch (type) {
                case SectionRow.SUB_SECTION: {
                    convertView = inflater.inflate(R.layout.row_course_outline_list, parent, false);
                    // apply a tag to this list row
                    ViewHolder tag = getTag(convertView);
                    convertView.setTag(tag);
                    break;
                }
                case SectionRow.SECTION: {
                    convertView = inflater.inflate(R.layout.row_section_header, parent, false);
                    break;
                }
                default: {
                    throw new IllegalArgumentException(String.valueOf(type));
                }
            }
        }

        // POPULATION
        switch (type) {
            case SectionRow.SUB_SECTION: {
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
                if (isVideoMode && comp.getVideos().size() == 0)
                    continue;
                if (comp.isContainer()) {
                    SectionRow header = new SectionRow(SectionRow.SECTION, comp);
                    adapterData.add(header);
                    for (IBlock childBlock : comp.getChildren()) {
                        CourseComponent child = (CourseComponent) childBlock;
                        if (isVideoMode && child.getVideos().size() == 0)
                            continue;
                        SectionRow row = new SectionRow(SectionRow.SUB_SECTION, child);
                        adapterData.add(row);
                    }
                } else {
                    SectionRow row = new SectionRow(SectionRow.SUB_SECTION, comp);
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
        if (adapterData.isEmpty()) {
            return;
        }
        // Get index of first courseware row
        int firstCoursewareRowIndex = -1;
        int i = 0;
        for (SectionRow sectionRow : adapterData) {
            if (sectionRow.isCoursewareRow()) {
                firstCoursewareRowIndex = i;
                break;
            }
            i++;
        }
        if (firstCoursewareRowIndex >= 0) {
            // Selectively clear adapter's data from a specific index onwards.
            adapterData.subList(firstCoursewareRowIndex, adapterData.size()).clear();
        }
    }

    /**
     * Tells if the adapter has any items related to the courseware.
     *
     * @return <code>true</code> if there are course items, <code>false</code> otherwise.
     */
    public boolean hasCourseData() {
        if (adapterData.isEmpty()) {
            return false;
        }
        for (SectionRow sectionRow : adapterData) {
            if (sectionRow.isCoursewareRow()) {
                return true;
            }
        }
        return false;
    }

    public void reloadData() {
        if (this.rootComponent != null)
            setData(this.rootComponent);
    }

    public View getRowView(int position, View convertView) {
        final SectionRow row = this.getItem(position);
        final SectionRow nextRow = this.getItem(position + 1);
        final CourseComponent component = row.getComponent();
        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        if (nextRow == null) {
            viewHolder.wholeSeparator.setVisibility(View.VISIBLE);
        } else {
            viewHolder.wholeSeparator.setVisibility(View.GONE);
            boolean isLastChildInBlock = !row.getComponent().getParent().getId().equals(nextRow.getComponent().getParent().getId());
            if (!isLastChildInBlock) {
                viewHolder.wholeSeparator.setVisibility(View.VISIBLE);
            }
        }

        viewHolder.rowSubtitleIcon.setVisibility(View.GONE);
        viewHolder.rowSubtitle.setVisibility(View.GONE);
        viewHolder.rowSubtitleVideoSize.setVisibility(View.GONE);
        viewHolder.rowSubtitlePanel.setVisibility(View.GONE);
        viewHolder.numOfVideoAndDownloadArea.setVisibility(View.GONE);

        if (component.isContainer()) {
            getRowViewForContainer(viewHolder, row);
        } else {
            getRowViewForLeaf(viewHolder, row);
        }
        return convertView;
    }

    private void getRowViewForLeaf(ViewHolder viewHolder, final SectionRow row) {
        final CourseComponent unit = row.getComponent();
        viewHolder.rowSubtitleIcon.setVisibility(View.GONE);
        viewHolder.rowSubtitleVideoSize.setVisibility(View.GONE);
        viewHolder.rowSubtitle.setVisibility(View.GONE);
        viewHolder.rowSubtitlePanel.setVisibility(View.GONE);
        viewHolder.lockedContent.setVisibility(View.GONE);
        viewHolder.bulkDownload.setVisibility(View.INVISIBLE);
        viewHolder.rowTitle.setText(unit.getDisplayName());
        viewHolder.rowContainer.setBackgroundResource(R.drawable.activated_item_selector);
        viewHolder.rowCompleted.setVisibility(View.INVISIBLE);
        viewHolder.wholeSeparator.setBackgroundColor(ContextCompat.getColor(context, R.color.neutralDark));

        boolean isDenialFeatureBasedEnrolments =
                row.getComponent().getAuthorizationDenialReason() == AuthorizationDenialReason.FEATURE_BASED_ENROLLMENTS;

        if (row.getComponent() instanceof VideoBlockModel) {
            final VideoBlockModel videoBlockModel = (VideoBlockModel) row.getComponent();
            final DownloadEntry videoData = videoBlockModel.getDownloadEntry(storage);
            if (null != videoData) {
                updateUIForVideo(viewHolder, videoData, videoBlockModel);
            } else if (videoBlockModel.getData().encodedVideos.getYoutubeVideoInfo() != null) {
                final boolean isYoutubePlayerEnabled = config.getYoutubePlayerConfig().isYoutubePlayerEnabled();
                UiUtils.INSTANCE.setTextViewDrawableEnd(context, viewHolder.rowTitle,
                        isYoutubePlayerEnabled ? R.drawable.ic_youtube_play : R.drawable.ic_laptop, R.dimen.small_icon_size);
            }
        } else if (config.isDiscussionsEnabled() && row.getComponent() instanceof DiscussionBlockModel) {
            UiUtils.INSTANCE.setTextViewDrawableEnd(context, viewHolder.rowTitle,
                    R.drawable.ic_forum, R.dimen.small_icon_size);
        } else if (!unit.isMultiDevice()) {
            // If we reach here & the type is VIDEO, it means the video is webOnly
            viewHolder.bulkDownload.setVisibility(View.INVISIBLE);
            UiUtils.INSTANCE.setTextViewDrawableEnd(context, viewHolder.rowTitle,
                    R.drawable.ic_laptop, R.dimen.small_icon_size);
        } else {
            viewHolder.bulkDownload.setVisibility(View.INVISIBLE);
            UiUtils.INSTANCE.setTextViewDrawableEnd(context, viewHolder.rowTitle,
                    unit.getType() == BlockType.PROBLEM ? R.drawable.ic_summarize : R.drawable.ic_article, R.dimen.small_icon_size);
        }
        if (unit.getType() == BlockType.OPENASSESSMENT) {
            UiUtils.INSTANCE.setTextViewDrawableEnd(context, viewHolder.rowTitle,
                    R.drawable.ic_fact_check, R.dimen.small_icon_size);
        }

        if (isDenialFeatureBasedEnrolments) {
            if (courseData.isUpgradeable() && environment.getFeaturesPrefs().isValuePropEnabled()) {
                viewHolder.rowSubtitle.setText(org.edx.mobile.util.TextUtils.underline(
                        context,
                        R.string.course_modal_unlock_graded_assignment
                ));
                viewHolder.lockedContent.setVisibility(View.VISIBLE);
            } else {
                viewHolder.rowSubtitle.setText(R.string.not_available_on_mobile);
            }
            viewHolder.rowSubtitlePanel.setVisibility(View.VISIBLE);
            viewHolder.rowSubtitle.setVisibility(View.VISIBLE);
        }
        viewHolder.wholeSeparator.setVisibility(View.VISIBLE);
        if (unit.isCompleted() || (isVideoMode && unit.isCompletedForVideos())) {
            viewHolder.rowContainer.setBackgroundResource(R.drawable.activated_item_success_selector);
            viewHolder.wholeSeparator.setBackgroundColor(ContextCompat.getColor(context, R.color.successXLight));
            viewHolder.rowCompleted.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_green_check));
            viewHolder.rowCompleted.setVisibility(View.VISIBLE);
        }
    }

    private void updateUIForVideo(@NonNull final ViewHolder viewHolder, @NonNull final DownloadEntry videoData,
                                  @NonNull final VideoBlockModel videoBlockModel) {
        UiUtils.INSTANCE.setTextViewDrawableEnd(context, viewHolder.rowTitle,
                VideoUtil.isCourseUnitVideo(environment, videoBlockModel)
                        ? R.drawable.ic_videocam : R.drawable.ic_youtube_play,
                R.dimen.small_icon_size);
        viewHolder.numOfVideoAndDownloadArea.setVisibility(View.VISIBLE);
        viewHolder.bulkDownload.setVisibility(View.VISIBLE);
        viewHolder.rowSubtitlePanel.setVisibility(View.VISIBLE);
        if (videoData.getDuration() > 0L) {
            viewHolder.rowSubtitle.setVisibility(View.VISIBLE);
            org.edx.mobile.util.TextUtils.setTextAppearance(context, viewHolder.rowSubtitle, R.style.semibold_text);
            viewHolder.rowSubtitle.setText(org.edx.mobile.util.TextUtils.getVideoDurationString(context, videoData.duration));
        }
        if (videoData.getSize() > 0L) {
            viewHolder.rowSubtitleVideoSize.setVisibility(View.VISIBLE);
            viewHolder.rowSubtitleVideoSize.setText(MemoryUtil.format(context, videoData.getSize()));
        }
        if (!VideoUtil.isVideoDownloadable(videoBlockModel.getData())) {
            viewHolder.numOfVideoAndDownloadArea.setVisibility(View.GONE);
        } else {
            viewHolder.numOfVideoAndDownloadArea.setVisibility(View.VISIBLE);
            dbStore.getDownloadedStateForVideoId(videoData.videoId,
                    new DataCallback<DownloadEntry.DownloadedState>(true) {
                        @Override
                        public void onResult(DownloadEntry.DownloadedState state) {
                            if (state == null || state == DownloadEntry.DownloadedState.ONLINE) {
                                // not yet downloaded
                                setRowStateOnDownload(viewHolder, DownloadEntry.DownloadedState.ONLINE,
                                        getBulkDownloadListener(videoBlockModel, videoData));
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
                                if (!FileUtil.isVideoFileExists(context, videoData.filepath)) {
                                    state = DownloadEntry.DownloadedState.ONLINE;
                                    // Update video state in DB
                                    VideoUtil.updateVideoDownloadState(dbStore, videoData,
                                            state.ordinal());
                                    // Delete corrupt file from storage if exists
                                    FileUtil.deleteRecursive(new File(videoData.filepath));
                                    // Broadcast video deletion event
                                    EventBus.getDefault().post(new DownloadedVideoDeletedEvent());
                                }
                                setRowStateOnDownload(viewHolder, state,
                                        state == DownloadEntry.DownloadedState.ONLINE ?
                                                getBulkDownloadListener(videoBlockModel, videoData) : null);
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

    private View.OnClickListener getBulkDownloadListener(@NonNull VideoBlockModel videoBlockModel,
                                                         @NonNull DownloadEntry videoData) {
        return v -> {
            /*
             * Assign preferred downloadable url to {@link DownloadEntry#url}
             * to use this url to download. After downloading
             * only downloaded video path will be used for streaming.
             */
            videoData.url = VideoUtil.getPreferredVideoUrlForDownloading(videoBlockModel.getData());
            downloadListener.download(videoData);
        };
    }

    private void getRowViewForContainer(ViewHolder holder,
                                        final SectionRow row) {
        final CourseComponent component = row.getComponent();
        String courseId = component.getCourseId();
        BlockPath path = component.getPath();
        //FIXME - we should add a new column in database - pathinfo.
        //then do the string match to get the record
        String chapterId = path.get(1) == null ? "" : path.get(1).getDisplayName();
        String sequentialId = path.get(2) == null ? "" : path.get(2).getDisplayName();

        holder.rowContainer.setBackgroundResource(R.drawable.activated_item_selector);
        holder.rowCompleted.setVisibility(View.INVISIBLE);
        holder.wholeSeparator.setVisibility(View.VISIBLE);
        holder.wholeSeparator.setBackgroundColor(ContextCompat.getColor(context, R.color.neutralDark));

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
                    R.color.neutralBlack));
            if (!TextUtils.isEmpty(component.getDueDate())) {
                try {
                    holder.rowSubtitle.setText(String.format("%s %s", holder.rowSubtitle.getText().toString(),
                            DateUtil.getFormattedDueDate(holder.rowSubtitle.getContext(), component.getDueDate())));
                } catch (IllegalArgumentException e) {
                    logger.error(e);
                }
            }
        }

        final int totalDownloadableVideos = component.getDownloadableVideosCount();
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
                holder.noOfVideos.setVisibility(View.INVISIBLE);
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
                                final List<VideoBlockModel> downloadableVideos = (List<VideoBlockModel>) (List) component.getVideos(true);
                                for (VideoBlockModel videoBlockModel : downloadableVideos) {
                                    /**
                                     * Assign preferred downloadable url to {@link VideoBlockModel#downloadUrl},
                                     * to use this url to download. After downloading only downloaded
                                     * video path will be used for streaming.
                                     */
                                    videoBlockModel.setDownloadUrl(VideoUtil.getPreferredVideoUrlForDownloading(videoBlockModel.getData()));
                                }
                                downloadListener.download(downloadableVideos);
                            }
                        });
            }
        }
        if (component.isCompleted() || (isVideoMode && component.isCompletedForVideos())) {
            holder.rowContainer.setBackgroundResource(R.drawable.activated_item_success_selector);
            holder.rowCompleted.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_green_check));
            holder.wholeSeparator.setBackgroundColor(ContextCompat.getColor(context, R.color.successXLight));
            holder.rowCompleted.setVisibility(View.VISIBLE);
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
                row.bulkDownloadLoading.setVisibility(View.VISIBLE);
                row.bulkDownloadLoading.setTag(DownloadEntry.DownloadedState.DOWNLOADING);
                row.bulkDownload.setVisibility(View.GONE);
                row.bulkDownloadLoading.setContentDescription(state.toString());
                break;
            case DOWNLOADED:
                row.bulkDownloadLoading.setVisibility(View.GONE);
                row.bulkDownload.setVisibility(View.VISIBLE);
                row.bulkDownload.setImageResource(R.drawable.download_done_selector);
                row.bulkDownload.setTag(R.drawable.ic_download_done);
                break;
            case ONLINE:
                row.bulkDownloadLoading.setVisibility(View.GONE);
                row.bulkDownload.setVisibility(View.VISIBLE);
                row.bulkDownload.setImageDrawable(UiUtils.INSTANCE.getDrawable(context,
                        R.drawable.ic_download));
                row.bulkDownload.setTag(R.drawable.ic_download);
                break;
        }
        row.bulkDownload.setContentDescription(state.toString());
        row.numOfVideoAndDownloadArea.setOnClickListener(listener);
        if (listener == null) {
            row.numOfVideoAndDownloadArea.setClickable(false);
        }
    }

    public View getHeaderView(int position, View convertView) {
        final SectionRow row = this.getItem(position);
        TextView titleView = (TextView) convertView.findViewById(R.id.row_header);
        titleView.setText(row.getComponent().getDisplayName());
        if (row.getComponent().isCompleted() || (isVideoMode && row.getComponent().isCompletedForVideos())) {
            titleView.setBackgroundColor(ContextCompat.getColor(context, R.color.successXXLight));
        } else {
            titleView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }
        return convertView;
    }

    /**
     * Tells if specified non-courseware item exists in the adapter's list or not.
     *
     * @param sectionType A non-courseware section type whose existence needs to be checked.
     * @return <code>true</code> if specified non-courseware item exist in adapter list,
     * <code>false</code> otherwise.
     */
    public boolean isNonCourseWareItemExist(int sectionType) {
        return getNonCourseWareItemPlace(sectionType) >= 0;
    }

    /**
     * Tells the place of a non-courseware item which exists in adapter list.
     *
     * @param sectionType A non-courseware section type whose place needs to be identified.
     * @return List index (non-negative number) of a specified non-courseware item, -1 in case item
     * doesn't exist.
     */
    public int getNonCourseWareItemPlace(int sectionType) {
        if (adapterData.isEmpty()) {
            return -1;
        }
        SectionRow sectionRow;
        for (int i = 0; i < adapterData.size(); i++) {
            sectionRow = adapterData.get(i);
            // return on finding first courseware item
            if (sectionRow.isCoursewareRow()) {
                break;
            }
            if (sectionRow.getType() == sectionType) {
                return i;
            }
        }
        return -1;
    }

    public ViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.rowContainer = (LinearLayout) convertView
                .findViewById(R.id.chapter_row_container);
        holder.rowCompleted = (AppCompatImageView) convertView
                .findViewById(R.id.completed);
        holder.rowTitle = (TextView) convertView
                .findViewById(R.id.row_title);
        holder.rowSubtitle = (TextView) convertView
                .findViewById(R.id.row_subtitle);
        holder.rowSubtitleVideoSize = (TextView) convertView
                .findViewById(R.id.row_subtitle_video_size);
        holder.rowSubtitleIcon = (AppCompatImageView) convertView
                .findViewById(R.id.row_subtitle_icon);
        holder.lockedContent = (AppCompatImageView) convertView
                .findViewById(R.id.locked_content);
        holder.rowSubtitleIcon.setColorFilter(ContextCompat.
                getColor(context, R.color.primaryBaseColor));
        holder.noOfVideos = (TextView) convertView
                .findViewById(R.id.no_of_videos);
        holder.bulkDownload = (AppCompatImageView) convertView
                .findViewById(R.id.bulk_download);
        holder.bulkDownloadLoading = (CircularProgressIndicator) convertView
                .findViewById(R.id.loading_indicator);
        holder.numOfVideoAndDownloadArea = (LinearLayout) convertView
                .findViewById(R.id.bulk_download_layout);
        holder.rowSubtitlePanel = convertView.findViewById(R.id.row_subtitle_panel);
        holder.wholeSeparator = convertView.findViewById(R.id.row_whole_separator);

        // Accessibility
        ViewCompat.setImportantForAccessibility(holder.rowSubtitle, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);

        return holder;
    }

    public static class ViewHolder {
        LinearLayout rowContainer;
        AppCompatImageView rowCompleted;
        TextView rowTitle;
        TextView rowSubtitle;
        TextView rowSubtitleVideoSize;
        AppCompatImageView rowSubtitleIcon;
        AppCompatImageView bulkDownload;
        CircularProgressIndicator bulkDownloadLoading;
        AppCompatImageView lockedContent;
        TextView noOfVideos;
        LinearLayout numOfVideoAndDownloadArea;
        View rowSubtitlePanel;
        View wholeSeparator;
    }

    public int getPositionByItemId(String itemId) {
        int size = getCount();
        for (int i = 0; i < size; i++) {
            // Some items might not have a component assigned to them e.g. Bulk Download item
            if (getItem(i).getComponent().getId().equals(itemId)) {
                return i;
            }
        }
        return -1;
    }
}

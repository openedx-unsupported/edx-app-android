package org.humana.mobile.view.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.inject.Inject;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.internal.Animation;
import com.joanzapata.iconify.widget.IconImageView;

import org.humana.mobile.BuildConfig;
import org.humana.mobile.R;
import org.humana.mobile.core.IEdxEnvironment;
import org.humana.mobile.logger.Logger;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.model.course.BlockPath;
import org.humana.mobile.model.course.BlockType;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.model.course.DiscussionBlockModel;
import org.humana.mobile.model.course.HasDownloadEntry;
import org.humana.mobile.model.course.IBlock;
import org.humana.mobile.model.course.VideoBlockModel;
import org.humana.mobile.model.db.DownloadEntry;
import org.humana.mobile.module.db.DataCallback;
import org.humana.mobile.module.db.IDatabase;
import org.humana.mobile.module.prefs.LoginPrefs;
import org.humana.mobile.module.storage.IStorage;
import org.humana.mobile.services.VideoDownloadHelper;
import org.humana.mobile.tta.analytics.Analytic;
import org.humana.mobile.tta.analytics.analytics_enums.Action;
import org.humana.mobile.tta.analytics.analytics_enums.Source;
import org.humana.mobile.tta.data.DataManager;
import org.humana.mobile.tta.data.enums.FilePathValueForXblock;
import org.humana.mobile.tta.scorm.ScormBlockModel;
import org.humana.mobile.tta.scorm.ScormManager;
import org.humana.mobile.tta.utils.ActivityUtil;
import org.humana.mobile.util.Config;
import org.humana.mobile.util.DateUtil;
import org.humana.mobile.util.MemoryUtil;
import org.humana.mobile.util.ResourceUtil;
import org.humana.mobile.util.TimeZoneUtils;
import org.humana.mobile.util.UiUtil;
import org.humana.mobile.util.VideoUtil;
import org.humana.mobile.util.images.CourseCardUtils;
import org.humana.mobile.util.images.TopAnchorFillWidthTransformation;
import org.humana.mobile.view.BulkDownloadFragment;
import org.humana.mobile.view.CourseOutlineActivity;
import org.humana.mobile.view.ProgressDialog_TA.ProgressDialog_TA;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CourseOutlineAdapter extends BaseAdapter {

    private final Logger logger = new Logger(getClass().getName());

    public interface DownloadListener {
        void download(List<? extends HasDownloadEntry> models);

        void download(DownloadEntry videoData);

        void viewDownloadsStatus();
    }

    private Context context;
    private Fragment parentFragment;
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
    private boolean isOnCourseOutline;
    public CourseComponent selectedUnit;
    @Inject
    @NonNull
    ScormManager scormManager;
    private ProgressDialog_TA progressDialog;

    private Analytic aHelper;

    private LoginPrefs loginPrefs;
    boolean cancel = false;

    private DataManager dataManager;

    public CourseOutlineAdapter(final Context context, ScormManager mScormMgr, Fragment fragment, final EnrolledCoursesResponse courseData,
                                final IEdxEnvironment environment, DownloadListener listener,
                                boolean isVideoMode, boolean isOnCourseOutline) {
        this.context = context;
        this.parentFragment = fragment;
        this.scormManager = mScormMgr;
        this.environment = environment;
        this.config = environment.getConfig();
        this.dbStore = environment.getDatabase();
        this.storage = environment.getStorage();
        this.courseData = courseData;
        this.downloadListener = listener;
        this.isVideoMode = isVideoMode;
        this.isOnCourseOutline = isOnCourseOutline;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        adapterData = new ArrayList();
        aHelper= new Analytic(context);
        loginPrefs = environment.getLoginPrefs();

        dataManager = DataManager.getInstance(this.context);

        if (isOnCourseOutline && !isVideoMode) {
            // Add course card item
            adapterData.add(new SectionRow(SectionRow.COURSE_CARD, null));
            // Add certificate item
            if (courseData.isCertificateEarned() && environment.getConfig().areCertificateLinksEnabled()) {
                adapterData.add(new SectionRow(SectionRow.COURSE_CERTIFICATE, null,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                environment.getRouter().showCertificate(context, courseData);
                            }
                        }));
            }
        }
        if (isVideoMode) {
            // Add bulk video download item
            adapterData.add(new SectionRow(SectionRow.BULK_DOWNLOAD, null));
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

        // FIXME: Revisit better DB communication and code improvements in [MA-1640]
        // INITIALIZATION
        if (convertView == null) {
            switch (type) {
                case SectionRow.ITEM: {
                    convertView = inflater.inflate(R.layout.row_course_outline_list, parent, false);
                    // apply a tag to this list row
                    ViewHolder holder = getItemViewHolder(convertView);

                   /* if(getItem(position).component.getChildren().size()==1)
                    {
                        if(getItem(position).component.getChildren().get(0).getType()==BlockType.SCORM||
                                getItem(position).component.getChildren().get(0).getType()==BlockType.PDF)
                        {

                            //show play/download scrom  layout visible
//                            tag.mx_downloadorView_layout.setVisibility(View.VISIBLE);

                            if (getItem(position).component.getChildren().get(0).getType() == BlockType.PDF) {
                                if (scormManager.hasPdf(getItem(position).component.getChildren().get(0).getId())) {
                                    addViewButton(tag, position);
                                } else {
                                    addDownloadButton(tag);
                                }
                            } else if (getItem(position).component.getChildren().get(0).getType() == BlockType.SCORM) {
                                if (scormManager.has(getItem(position).component.getChildren().get(0).getId())) {
                                    addViewButton(tag, position);
                                } else {
                                    addDownloadButton(tag);
                                }
                            }


                            //click for download /view button
                            tag.mxScromDownloadorView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    selectedUnit = (CourseComponent) getItem(position).component.getChildren().get(0);

                                    if (tag.mxScromDownloadorView.getContentDescription().toString().equals("download")) {
                                environment.getRouter().showCourseUnitDetail(fragment,
                                        REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, selectedUnit.getId());

                                        doDownload(selectedUnit, tag);

                                        //update UI after download
                                    } else if (tag.mxScromDownloadorView.getContentDescription().toString().equals("view")) {
                                        if (selectedUnit.getType() == BlockType.PDF) {
                                            MXPDFManager manager = new MXPDFManager();
                                            manager.viewPDF(context, scormManager.getPdf(selectedUnit.getId()));
                                        } else if (selectedUnit.getType() == BlockType.SCORM) {
                                            doShow(scormManager.get(selectedUnit.getId()),selectedUnit);
                                        }

                                        //analytics hit for view scorm
                                        if (selectedUnit != null) {
                                            //for analytics update
                                            CourseOutlineActivity activity = (CourseOutlineActivity) context;

                                            aHelper.addMxAnalytics_db(loginPrefs.getUsername()
                                                    , selectedUnit.getDisplayName(), Action.ViewUnit,
                                                    selectedUnit.getRoot().getDisplayName(), Source.Mobile);
                                        }
                                    }
                                }
                            });
                        }
                    }
                    else if(getItem(position).component.getChildren().size()>1 && getItem(position).component.getType()==BlockType.SCORM||
                            getItem(position).component.getType()==BlockType.PDF)
                    {
                        //show play/download scrom  layout visible
                        tag.mx_downloadorView_layout.setVisibility(View.VISIBLE);

                        if(getItem(position).component.getType()==BlockType.PDF)
                        {
                            if(scormManager.hasPdf(getItem(position).component.getId()))
                            {
                                addViewButton(tag,position);
                            }
                            else
                            {
                                addDownloadButton(tag);
                            }
                        }
                        else if(getItem(position).component.getType()==BlockType.SCORM)
                        {
                            if(scormManager.has(getItem(position).component.getId()))
                            {
                                addViewButton(tag,position);
                            }
                            else
                            {
                                addDownloadButton(tag);
                            }
                        }

                        //click for download /view button
                        tag.mxScromDownloadorView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                selectedUnit=(CourseComponent)getItem(position).component;

                                if(tag.mxScromDownloadorView.getContentDescription().toString().equals("download")) {
                                environment.getRouter().showCourseUnitDetail(fragment,
                                        REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, selectedUnit.getId());

                                    doDownload(selectedUnit,tag);

                                    //update UI after download
                                }
                                else if(tag.mxScromDownloadorView.getContentDescription().toString().equals("view"))
                                {
                                    if(selectedUnit.getType()==BlockType.PDF)
                                    {
                                        MXPDFManager manager=new MXPDFManager();
                                        manager.viewPDF(context,scormManager.getPdf(selectedUnit.getId()));
                                    }
                                    else if(selectedUnit.getType()==BlockType.SCORM)
                                    {
                                        doShow(scormManager.get(selectedUnit.getId()),selectedUnit);
                                    }

                                    //analytics hit for view scorm
                                    if(selectedUnit!=null) {
                                        //for analytics update
                                        CourseOutlineActivity activity=(CourseOutlineActivity)context;

                                        aHelper.addMxAnalytics_db(loginPrefs.getUsername()
                                                ,selectedUnit.getDisplayName() , Action.ViewUnit,
                                                selectedUnit.getRoot().getDisplayName() , Source.Mobile);
                                    }
                                }
                            }
                        });

                    }
                    else if(getItem(position).component.getType()==BlockType.SCORM || getItem(position).component.getType()==BlockType.PDF)
                    {
                        //show play/download scrom  layout visible
                        tag.mx_downloadorView_layout.setVisibility(View.VISIBLE);

                        if(getItem(position).component.getType()==BlockType.PDF)
                        {
                            if(scormManager.hasPdf(getItem(position).component.getId()))
                            {
                                addViewButton(tag,position);
                            }
                            else
                            {
                                addDownloadButton(tag);
                            }
                        }
                        else if(getItem(position).component.getType()==BlockType.SCORM)
                        {
                            if(scormManager.has(getItem(position).component.getId()))
                            {
                                addViewButton(tag,position);
                            }
                            else
                            {
                                addDownloadButton(tag);
                            }
                        }

                        //click for download /view button
                        tag.mxScromDownloadorView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                selectedUnit=(CourseComponent)getItem(position).component;

                                if(tag.mxScromDownloadorView.getContentDescription().toString().equals("download")) {
                                environment.getRouter().showCourseUnitDetail(fragment,
                                        REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, selectedUnit.getId());

                                    doDownload(selectedUnit,tag);

                                    //update UI after download
                                }
                                else if(tag.mxScromDownloadorView.getContentDescription().toString().equals("view"))
                                {
                                    if(selectedUnit.getType()==BlockType.PDF)
                                    {
                                        MXPDFManager manager=new MXPDFManager();
                                        manager.viewPDF(context,scormManager.getPdf(selectedUnit.getId()));
                                    }
                                    else if(selectedUnit.getType()==BlockType.SCORM)
                                    {
                                        doShow(scormManager.get(selectedUnit.getId()),selectedUnit);
                                    }

                                    //analytics hit for view scorm
                                    if(selectedUnit!=null) {
                                        //for analytics update
                                        CourseOutlineActivity activity=(CourseOutlineActivity)context;

                                        aHelper.addMxAnalytics_db(loginPrefs.getUsername()
                                                ,selectedUnit.getDisplayName() , Action.ViewUnit,
                                                selectedUnit.getRoot().getDisplayName() , Source.Mobile);
                                    }
                                }
                            }
                        });
                    }*/
                  /*  tag.mxScromDownloadorView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectedUnit=(CourseComponent)getItem(position).component;

                            if(tag.mxScromDownloadorView.getContentDescription().toString().equals("download")) {
//                                environment.getRouter().showCourseUnitDetail(fragment,
//                                REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, selectedUnit.getId());

//                                doDownload(selectedUnit,tag);

                                //update UI after download
                            }
                            else if(tag.mxScromDownloadorView.getContentDescription().toString().equals("view"))
                            {
                                if(selectedUnit.getType()==BlockType.PDF)
                                {
                                    MXPDFManager manager=new MXPDFManager();
                                    manager.viewPDF(context,scormManager.getPdf(selectedUnit.getId()));
                                }
                                else if(selectedUnit.getType()==BlockType.SCORM)
                                {
                                    doShow(scormManager.get(selectedUnit.getId()),selectedUnit);
                                }

                                //analytics hit for view scorm
                                if(selectedUnit!=null) {
                                    //for analytics update
                                    CourseOutlineActivity activity=(CourseOutlineActivity)context;

                                    aHelper.addMxAnalytics_db(loginPrefs.getUsername()
                                            ,selectedUnit.getDisplayName() , Action.ViewUnit,
                                            selectedUnit.getRoot().getDisplayName() , Source.Mobile);
                                }
                            }
                        }
                    });*/
//                    CourseComponent unit;
//                    unit = getItem(position).component;

//                        if (getItem(position).component.getType().equals(BlockType.PDF) &&
//                                getItem(position).component.getDisplayName().equals("PDF")) {
//
//                            ScormBlockModel mDownloadbleUnit = new ScormBlockModel(getItem(position).component.getBlockModel(),
//                                    getItem(position).component.getParent());
//
//                            tag.mx_downloadorView_layout.setVisibility(View.VISIBLE);
//
//                            switch (dataManager.getScormStatus(mDownloadbleUnit)) {
//                                case downloaded:
//                                    tag.mx_downloadorView_layout.setVisibility(View.GONE);
//                                    tag.mProgressLayout.setVisibility(View.GONE);
//                                    tag.mx_Scrom_delet_layout.setVisibility(View.VISIBLE);
//
//                                    tag.mx_scrom_open_layout.setVisibility(View.VISIBLE);
//                                    tag.mx_scrom_open_layout.setOnClickListener(new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//                                            String filePath = mDownloadbleUnit.getDownloadEntry(
//                                                    dataManager.getEdxEnvironment().getStorage()).getFilePath();
//                                            ActivityUtil.viewPDF(context, new File(filePath));
//                                        }
//                                    });
//                                    break;
//                            }
//                            tag.mx_Scrom_delet_layout.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    tag.mx_Scrom_delet_layout.setVisibility(View.GONE);
//                                    tag.mx_downloadorView_layout.setVisibility(View.VISIBLE);
//                                    tag.mx_scrom_open_layout.setVisibility(View.GONE);
//                                    tag.mxScromDownloadorView.setVisibility(View.VISIBLE);
//                                    dataManager.deleteScorm(mDownloadbleUnit);
//
//                                }
//                            });
//                        }


                    //endregion
                    convertView.setTag(holder);
                    break;
                }
                case SectionRow.SECTION: {
                    convertView = inflater.inflate(R.layout.row_section_header, parent, false);
                    break;
                }
                case SectionRow.COURSE_CARD: {
                    convertView = inflater.inflate(R.layout.row_course_card, parent, false);
                    break;
                }
                case SectionRow.COURSE_CERTIFICATE:
                    convertView = inflater.inflate(R.layout.row_course_dashboard_cert, parent, false);
                    break;
                case SectionRow.LAST_ACCESSED_ITEM: {
                    convertView = inflater.inflate(R.layout.row_last_accessed, parent, false);
                    break;
                }
                case SectionRow.BULK_DOWNLOAD: {
                    final FrameLayout layout = new FrameLayout(parentFragment.getContext());
                    final int id = UiUtil.generateViewId();
                    layout.setId(id);

                    final BulkDownloadFragment fragment = new BulkDownloadFragment(downloadListener, environment);
                    parentFragment.getChildFragmentManager().
                            beginTransaction().replace(id, fragment).commit();
                    convertView = layout;
                    convertView.setTag(fragment);
                    break;
                }
                default: {
                    throw new IllegalArgumentException(String.valueOf(type));
                }
            }
        }

        // POPULATION
        switch (type) {
            case SectionRow.ITEM: {
                return getRowView(position, convertView);
            }
            case SectionRow.SECTION: {
                return getHeaderView(position, convertView);
            }
            case SectionRow.COURSE_CARD: {
                return getCardView(convertView);
            }
            case SectionRow.COURSE_CERTIFICATE:
                return getCertificateView(position, convertView);
            case SectionRow.LAST_ACCESSED_ITEM: {
                return getLastAccessedView(position, convertView);
            }
            case SectionRow.BULK_DOWNLOAD: {
                if (rootComponent != null) {
                    final BulkDownloadFragment fragment = (BulkDownloadFragment) convertView.getTag();
                    fragment.populateViewHolder(
                            isOnCourseOutline ? rootComponent.getCourseId() : rootComponent.getId(),
                            rootComponent.getVideos(true));
                }
                return convertView;
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
                        SectionRow row = new SectionRow(SectionRow.ITEM, false, child);
                        adapterData.add(row);
                    }
                } else {
                    SectionRow row = new SectionRow(SectionRow.ITEM, true, comp);
                    adapterData.add(row);
                }
            }

            if (isVideoMode && rootComponent.getDownloadableVideosCount() == 0) {
                // Remove bulk video download row if the course has NO downloadable videos
                if (adapterData.size() > 0 && adapterData.get(0).type == SectionRow.BULK_DOWNLOAD) {
                    adapterData.remove(0);
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


        if (unit.getType().equals(BlockType.PDF)){

            viewHolder.mx_downloadorView_layout.setVisibility(View.GONE);
            viewHolder.mx_Scrom_delet_layout.setVisibility(View.GONE);
//            viewHolder.mxScromDownloadorView.setVisibility(View.GONE);
            viewHolder.mx_scrom_open_layout.setVisibility(View.GONE);
            viewHolder.mProgressLayout.setVisibility(View.GONE);

             List<ScormBlockModel> models= unit.getScorms();
             if (models.size()>0)
            switch (dataManager.getScormStatus(models.get(0))){
                case not_downloaded:
                    viewHolder.mx_downloadorView_layout.setVisibility(View.VISIBLE);
                    break;
                case downloading:
                    viewHolder.mProgressLayout.setVisibility(View.VISIBLE);
                    break;
                case downloaded:
                    viewHolder.mx_Scrom_delet_layout.setVisibility(View.VISIBLE);
                    viewHolder.mx_scrom_open_layout.setVisibility(View.VISIBLE);
                    viewHolder.mx_Scrom_delet_layout.setOnClickListener(v -> {
                        dataManager.deleteScorm(models.get(0));
                        notifyDataSetChanged();
                    });
                  viewHolder.mx_scrom_open_layout.setOnClickListener(v->{
                      String filePath = models.get(0).getDownloadEntry(
                              dataManager.getEdxEnvironment().getStorage()).getFilePath();
                      ActivityUtil.viewPDF(context, new File(filePath));
                  });
                    break;
                case watched:
                    break;
                case watching:
                    break;

            }

        }else{
            viewHolder.mx_downloadorView_layout.setVisibility(View.GONE);
            viewHolder.mx_Scrom_delet_layout.setVisibility(View.GONE);
            viewHolder.mxScromDownloadorView.setVisibility(View.GONE);
            viewHolder.mx_scrom_open_layout.setVisibility(View.GONE);
            viewHolder.mProgressLayout.setVisibility(View.GONE);
        }

//            if (unit.getType().equals(BlockType.PDF)) {
//                ScormBlockModel mDownloadbleUnit = new ScormBlockModel(unit.getBlockModel(),
//                        unit.getParent());
//
//                viewHolder.mx_downloadorView_layout.setVisibility(View.VISIBLE);
//                switch (dataManager.getScormStatus(mDownloadbleUnit)) {
//                    case downloaded:
//                        viewHolder.mProgressLayout.setVisibility(View.GONE);
//                        viewHolder.mx_Scrom_delet_layout.setVisibility(View.VISIBLE);
//
//                        viewHolder.mx_Scrom_delet_layout.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                dataManager.deleteScorm(mDownloadbleUnit);
//                                viewHolder.mx_downloadorView_layout.setVisibility(View.VISIBLE);
//                                viewHolder.mx_Scrom_delet_layout.setVisibility(View.GONE);
//                                viewHolder.mxScromDownloadorView.setVisibility(View.VISIBLE);
//                                viewHolder.mx_scrom_open_layout.setVisibility(View.GONE);
//                            }
//                        });
//                        viewHolder.mx_scrom_open_layout.setVisibility(View.VISIBLE);
//                        viewHolder.mx_scrom_open_layout.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                String filePath = mDownloadbleUnit.getDownloadEntry(
//                                        dataManager.getEdxEnvironment().getStorage()).getFilePath();
//                                ActivityUtil.viewPDF(context, new File(filePath));
//                            }
//                        });
//                        break;
//                }
//            }


        if (row.component instanceof VideoBlockModel) {
            final VideoBlockModel videoBlockModel = (VideoBlockModel) row.component;
            final DownloadEntry videoData = videoBlockModel.getDownloadEntry(storage);
            if (null != videoData) {
                updateUIForVideo(viewHolder, videoData, videoBlockModel);
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
                viewHolder.rowType.setIcon(FontAwesomeIcons.fa_book);
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

    private void updateUIForVideo(@NonNull final ViewHolder viewHolder, @NonNull final DownloadEntry videoData,
                                  @NonNull final VideoBlockModel videoBlockModel) {
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

        dbStore.getWatchedStateForVideoId(videoData.videoId,
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
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                /**
                                                 * Assign preferred downloadable url to {@link DownloadEntry#url}
                                                 * to use this url to download. After downloading
                                                 * only downloaded video path will be used for streaming.
                                                 */
                                                videoData.url = VideoUtil.getPreferredVideoUrlForDownloading(videoBlockModel.getData());
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


       /* if (component.getType().equals(BlockType.PDF) &&
                component.getDisplayName().equals("PDF")) {

            ScormBlockModel mDownloadbleUnit = new ScormBlockModel(component.getBlockModel(),
                   component.getParent());

            holder.mx_downloadorView_layout.setVisibility(View.VISIBLE);

            switch (dataManager.getScormStatus(mDownloadbleUnit)) {
                case downloaded:
                    holder.mx_downloadorView_layout.setVisibility(View.GONE);
                    holder.mProgressLayout.setVisibility(View.GONE);
                    holder.mx_Scrom_delet_layout.setVisibility(View.VISIBLE);

                    holder.mx_scrom_open_layout.setVisibility(View.VISIBLE);
                    holder.mx_scrom_open_layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String filePath = mDownloadbleUnit.getDownloadEntry(
                                    dataManager.getEdxEnvironment().getStorage()).getFilePath();
                            ActivityUtil.viewPDF(context, new File(filePath));
                        }
                    });
                    break;
            }
            holder.mx_Scrom_delet_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.mx_Scrom_delet_layout.setVisibility(View.GONE);
                    holder.mx_downloadorView_layout.setVisibility(View.VISIBLE);
                    holder.mx_scrom_open_layout.setVisibility(View.GONE);
                    holder.mxScromDownloadorView.setVisibility(View.VISIBLE);
                    dataManager.deleteScorm(mDownloadbleUnit);

                }
            });
        }*/


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
                // TODO: Animation.PULSE causes lag when a spinner stays on screen for a while. Fix in LEARNER-5053
                row.bulkDownload.setIconAnimation(Animation.SPIN);
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
        courseTextDetails.setText(CourseCardUtils.getFormattedDate(context, courseData.getCourse()));

        return view;
    }

    private View getCertificateView(int position, View convertView) {
        final SectionRow sectionRow = getItem(position);
        convertView.setOnClickListener(sectionRow.clickListener);
        return convertView;
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
        final int lastAccessedItemPlace = getNonCourseWareItemPlace(SectionRow.LAST_ACCESSED_ITEM);
        // Update the last accessed item, if its already there in the list
        if (lastAccessedItemPlace >= 0) {
            adapterData.set(lastAccessedItemPlace, new SectionRow(SectionRow.LAST_ACCESSED_ITEM, lastAccessedComponent, onClickListener));
        } else {
            // Add it otherwise
            adapterData.add(getLastAccessedItemPlace(), new SectionRow(SectionRow.LAST_ACCESSED_ITEM, lastAccessedComponent, onClickListener));
        }
        notifyDataSetChanged();
    }

    /**
     * Tells the appropriate place for a {@link SectionRow#LAST_ACCESSED_ITEM} to put in the adapter's list.
     *
     * @return List index (non-negative number) for a {@link SectionRow#LAST_ACCESSED_ITEM}.
     */
    public int getLastAccessedItemPlace() {
        return isNonCourseWareItemExist(SectionRow.COURSE_CERTIFICATE) ? 2 : 1;
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
            if (sectionRow.type == sectionType) {
                return i;
            }
        }
        return -1;
    }

    public ViewHolder getItemViewHolder(View convertView) {
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

        //Download file
        holder.mxScromDownloadorView = (ImageView) convertView
                .findViewById(R.id.mx_scrom_download);


        holder.mx_downloadorView_layout = (LinearLayout) convertView
                .findViewById(R.id.mx_download_layout);

        holder.mxScromDownloadorView = (ImageView) convertView
                .findViewById(R.id.mx_scrom_download);

        holder.mx_open_pdf = (ImageView) convertView
                .findViewById(R.id.mx_open_pdf);

        holder.mProgressLayout = convertView
                .findViewById(R.id.mx_scrom_progress_layout);

        holder.mProgressBar = convertView
                .findViewById(R.id.mx_progress_bar);

        holder.mx_Scrom_delet_layout = (LinearLayout) convertView
                .findViewById(R.id.mx_scrom_delet_layout);

        holder.mx_scrom_open_layout = (LinearLayout) convertView
                .findViewById(R.id.mx_scrom_open_layout);

        holder.mxScromDelete = (ImageView) convertView
                .findViewById(R.id.mx_scrom_delet);


//        Download file end

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

        public ImageView mxScromDownloadorView,mx_open_pdf;
        public ImageView mxScromDelete;
        public ProgressBar mProgressBar;
        public LinearLayout mProgressLayout;
        public LinearLayout  mx_downloadorView_layout;
        public LinearLayout  mx_Scrom_delet_layout, mx_scrom_open_layout;

    }

    public static class SectionRow {
        public static final int COURSE_CARD = 0;
        public static final int COURSE_CERTIFICATE = 1;
        public static final int LAST_ACCESSED_ITEM = 2;
        public static final int SECTION = 3;
        public static final int ITEM = 4;
        public static final int BULK_DOWNLOAD = 5;

        // Update this count according to the section types mentioned above
        public static final int NUM_OF_SECTION_ROWS = 6;

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

        public boolean isCoursewareRow() {
            return this.type == ITEM ||
                    this.type == SECTION;
        }
    }

    public int getPositionByItemId(String itemId) {
        int size = getCount();
        for (int i = 0; i < size; i++) {
            // Some items might not have a component assigned to them e.g. Bulk Download item
            if (getItem(i).component == null) continue;
            if (getItem(i).component.getId().equals(itemId))
                return i;
        }
        return -1;
    }


    // Add pdf download option


    private void updateAnalyticsforScromDownload(CourseComponent unit,boolean isCompleted)
    {
        if(unit!=null) {
            //for analytics update

            if (isCompleted) {
                aHelper.addMxAnalytics_db(loginPrefs.getUsername()
                        , unit.getDisplayName(), Action.ScromDownloadCompleted,
                        unit.getRoot().getDisplayName(), Source.Mobile);
            }
            else {
                aHelper.addMxAnalytics_db(loginPrefs.getUsername()
                        , unit.getDisplayName(), Action.ScormDownloadIncomplete,
                        unit.getRoot().getDisplayName(), Source.Mobile);
            }

        }
    }

    public void doShow(String folder,CourseComponent unit) {
        CourseOutlineActivity activity=(CourseOutlineActivity)context;
        environment.getRouter().getScromActivity(activity,context,folder,unit.getRoot().getDisplayName(),unit.getCourseId(),unit.getId());
    }

    public  void setProgressDialogStatus(String message)
    {
        if (progressDialog != null) {
            progressDialog.setProgress(Integer.parseInt(message));
        }
    }

    private DownloadEntry getDownloadEntery_db(CourseComponent mUnit)
    {
        if(mUnit!=null && mUnit.getChildren()!=null&& mUnit.getChildren().size()>0)
        {
            mUnit= (CourseComponent) mUnit.getChildren().get(0);
        }
        String downladedFilePath="";

        if(mUnit.getType()==BlockType.SCORM)
        {
            downladedFilePath= String.valueOf(FilePathValueForXblock.Scrom);
        }
        else if((mUnit.getType()==BlockType.PDF))
        {
            downladedFilePath=String.valueOf(FilePathValueForXblock.Pdf);
        }

        //set download url to null because are downloading file from scrom/pdf manager not from edx vedio download manager.
        DownloadEntry model=new DownloadEntry();
        model.setDownloadEntryForScrom(loginPrefs.getUsername(),mUnit.getDisplayName(),downladedFilePath,mUnit.getId(),"",mUnit.getRoot().getCourseId()
                ,mUnit.getParent().getDisplayName()
                ,mUnit.getRoot().getDisplayName(),System.currentTimeMillis());

        return model;
    }

    private  void addViewButton(final ViewHolder tag, final int position )
    {
        tag.mxScromDownloadorView.setImageDrawable(context.getResources().getDrawable(R.drawable.t_icon_play_green));
        tag.mxScromDownloadorView.setContentDescription("view");

        //view delete button
        tag.mx_Scrom_delet_layout.setVisibility(View.VISIBLE);
//        tag.mxScromDelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                CourseOutlineActivity activity=(CourseOutlineActivity)context;
//
//                AlertDialog.Builder deleteConfirmDialog = new AlertDialog.Builder(activity);
//
//                deleteConfirmDialog
//                        .setTitle("Delete")
//                        .setMessage("Are you sure to delete this item?")
//                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
//                        {
//                            public void onClick(DialogInterface dialog, int id)
//                            {
//
//                                if(getItem(position).component.getChildren().size()==0) {
//                                    selectedUnit = (CourseComponent) getItem(position).component;
//                                    if (scormManager.has(selectedUnit.getId()) || scormManager.hasPdf(selectedUnit.getId())) {
//                                        String path;
//                                        path = scormManager.get(selectedUnit.getId());
//
//                                        //remove item from android db first
////                                        environment.getStorage().removeDownloadedScromEntry(selectedUnit.getId());
//
//                                        //remove item from local storage
////                                        scormManager.deleteUnit(path);
//
//                                        //update UI after delete Unit
//                                        tag.mxScromDownloadorView.setImageDrawable(context.getResources().getDrawable(R.drawable.download));
//                                        tag.mxScromDownloadorView.setContentDescription("download");
//
//                                        //analytic update for count update
////                                        aHelper.addScromCountAnalytic_db(context);
//
//                                        tag.mx_Scrom_delet_layout.setVisibility(View.GONE);
//
//                                    }
//                                }
//                                else
//                                {
//                                    selectedUnit=(CourseComponent)getItem(position).component.getChildren().get(0);
//
//                                    if(scormManager.has(selectedUnit.getId()) || scormManager.hasPdf(selectedUnit.getId()))
//                                    {
//                                        String path;
////                                        path = scormManager.get(selectedUnit.getId());
//
//                                        //remove item from android db first
////                                        environment.getStorage().removeDownloadedScromEntry(selectedUnit.getId());
//
//                                        //remove item from local storage
////                                        scormManager.deleteUnit(path);
//
//                                        //update UI after delete Unit
//                                        tag.mxScromDownloadorView.setImageDrawable(context.getResources().getDrawable(R.drawable.download));
//                                        tag.mxScromDownloadorView.setContentDescription("download");
//
//                                        //analytic update for count update
////                                        aHelper.addScromCountAnalytic_db(context);
//                                        //analytic update for count update
////                                        aHelper.addScromCountAnalytic_db(context);
//
//                                        tag.mx_Scrom_delet_layout.setVisibility(View.GONE);
//                                    }
//                                }
//                                dialog.cancel();
//                            }
//                        })
//                        .setCancelable(false)
//                        .setNegativeButton("No", new DialogInterface.OnClickListener()
//                        {
//                            public void onClick(DialogInterface dialog, int id)
//                            {
//                                dialog.cancel();
//                            }
//                        });
//
//                AlertDialog alert = deleteConfirmDialog.create();
//                alert.show();
//            }
//        });

    }

    private  void addDownloadButton(ViewHolder tag)
    {
        tag.mxScromDownloadorView.setImageDrawable(context.getResources().getDrawable(R.drawable.download));
        tag.mxScromDownloadorView.setContentDescription("download");

    }

    public void doDownload(CourseComponent unit, final ViewHolder tag) {

        final CourseOutlineActivity activity=(CourseOutlineActivity)context;

        if(selectedUnit!=null) {
            //for analytics update scorm download start
            aHelper.addMxAnalytics_db(loginPrefs.getUsername(),
                    unit.getDisplayName() , Action.StartScormDownload,
                    unit.getRoot().getDisplayName() , Source.Mobile);
        }

        loginPrefs.storeCurrentDownloadingScromInfo(selectedUnit.getDisplayName() +
                "::" + selectedUnit.getRoot().getDisplayName() + "::" +String.valueOf(Action.StartScormDownload));
        ScormBlockModel mDownloadbleUnit= new ScormBlockModel(unit.getBlockModel(), unit.getParent());

        switch (dataManager.getScormStatus(mDownloadbleUnit)){
            case downloaded:
                tag.mx_Scrom_delet_layout.setVisibility(View.VISIBLE);
                tag.mx_scrom_open_layout.setVisibility(View.VISIBLE);
                tag.mProgressLayout.setVisibility(View.GONE);

                break;
            case not_downloaded:
//                showWaitingDialog();
                dataManager.downloadSingle(mDownloadbleUnit, parentFragment.getActivity(),
                        new VideoDownloadHelper.DownloadManagerCallback() {
                    @Override
                    public void onDownloadStarted(Long result) {
                        Log.d("--> Download State", "onDownloadStarted");
                        //progress bar show
                        tag.mProgressLayout.setVisibility(View.VISIBLE);
                        tag.mx_downloadorView_layout.setVisibility(View.GONE);
                        tag.mxScromDownloadorView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onDownloadFailedToStart() {
                        Log.d("--> Download State", "onDownloadFailedToStart");
                    }

                    @Override
                    public void showProgressDialog(int numDownloads) {
                        Log.d("--> Download State", "showProgressDialog");
//                        progressDialog.setProgress(numDownloads);
                    }

                    @Override
                    public void updateListUI() {
                        Log.d("--> Download State", "updateListUI");
                    }

                    @Override
                    public boolean showInfoMessage(String message) {
                        Log.d("--> Download State", "showInfoMessage");
                        return false;
                    }
                });
        }

        /*scormManager.startScormDownload(mDownloadbleUnit, new ScormManager.DownloadListener() {
            @Override
            public void handle(final Exception ex) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissWaitingDialog();
                        reloadData();

                        if(selectedUnit.getType()==BlockType.PDF)
                        {
                            String path = scormManager.get(selectedUnit.getId());
                            //remove item from local storage
                            scormManager.deleteUnit(path);
                        }

                        //do analytics hit on server
                        updateAnalyticsforScromDownload(selectedUnit,false);

                        //to tackle missing scrom event i.e in case of user close app during download or net got disconnected.
                        //store it in "meta::page::action" format
                        //remove we log it to server no need to save in cache
                        if (NetworkUtil.isConnected(context))
                            loginPrefs.removeCurrentDownloadingScromInfo();
                        else {

                            loginPrefs.storeCurrentDownloadingScromInfo(selectedUnit.getDisplayName() +
                                    "::" + selectedUnit.getRoot().getDisplayName() + "::" +String.valueOf(Action.ScormDownloadIncomplete));
                        }

                        //Toast.makeText(context,"Unable to download file",Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onDownloadComplete(String response) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissWaitingDialog();

                        //first do count update then update local db
                        aHelper.addScromDownload_db(context,getDownloadEntery_db(selectedUnit));

                        //do entry to database //local android_db
                        //doDownloadEntery_db(selectedUnit);

                        //do analytics hit on server
                        updateAnalyticsforScromDownload(selectedUnit,true);

                        //to tackle missing scrom event i.e in case of user close app during download or net got disconnected.
                        //store it in "meta::page::action" format
                        //remove we log it to server no need to save in cache
                        if (NetworkUtil.isConnected(context))
                            loginPrefs.removeCurrentDownloadingScromInfo();
                        else
                        {
                            loginPrefs.storeCurrentDownloadingScromInfo(selectedUnit.getDisplayName() + "::" + selectedUnit.getRoot().getDisplayName() +
                                    "::" +String.valueOf(Action.ScromDownloadCompleted));
                        }

                        reloadData();
                    }
                });
            }
        });*/
    }

    public void showWaitingDialog() {
        final CourseOutlineActivity activity=(CourseOutlineActivity)context;
//        if (progressDialog == null) {
        progressDialog = new ProgressDialog_TA(activity);
//        }
        progressDialog.setMessage("Downloading file. Please wait...");
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.setCancelable(true);
                cancel = true;

//                HttpManager.isCanceled=true;

                Intent intent = new Intent(BuildConfig.APPLICATION_ID);
                LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
                dialog.dismiss();
                reloadData();

                //disable cancel button when user click for cancel and show 'Cancelling Message' untill thread completely stop.
//                progressDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
                //set message
//                progressDialog.setMessage("Cancelling download. Please wait...");
//                cancel = true;

//                Intent intent = new Intent("org.humana.mobile.scormCancel");
//                LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
            }
        });

        if (progressDialog != null && !progressDialog.isShowing() && !activity.isFinishing()) {
            try {
                progressDialog.show();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }


    public void dismissWaitingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            //progressDialog.dismiss();

            progressDialog.dismissManually();
        }
    }

    public void notifyItem(DownloadEntry de){

        notifyDataSetChanged();

        /*for (SectionRow row: adapterData){
            if (row.component != null && row.component.getId().equals(de.videoId)){



                Log.d("notifyItem", row.component.getDisplayName());
                break;
            }
        }*/
    }
}

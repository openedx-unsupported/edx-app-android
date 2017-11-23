package org.edx.mobile.view;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.inject.Inject;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.services.LastAccessManager;
import org.edx.mobile.services.LastAccessManager.LastAccessManagerCallback;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.images.ShareUtils;
import org.edx.mobile.util.images.TopAnchorFillWidthTransformation;

public class CourseDashboardFragment extends BaseFragment {
    static public String TAG = CourseHandoutFragment.class.getCanonicalName();
    static public String CourseData = TAG + ".course_data";
    protected final Logger logger = new Logger(getClass().getName());
    @Inject
    IEdxEnvironment environment;
    private EnrolledCoursesResponse courseData;
    private boolean isCoursewareAccessible = true;
    private TextView courseTextName;
    private TextView courseTextDetails;
    private ImageView headerImageView;
    private LinearLayout parent;
    private TextView errorText;
    private ImageButton shareButton;

    @Inject
    private AnalyticsRegistry analyticsRegistry;

    @Inject
    private LastAccessManager lastAccessManager;

    @Inject
    CourseManager courseManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        courseData = (EnrolledCoursesResponse) args.getSerializable(CourseData);
        if (courseData != null) {
            isCoursewareAccessible = courseData.getCourse().getCoursewareAccess().hasAccess();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        if (isCoursewareAccessible) {
            view = inflater.inflate(R.layout.fragment_course_dashboard, container, false);
            courseTextName = (TextView) view.findViewById(R.id.course_detail_name);
            courseTextDetails = (TextView) view.findViewById(R.id.course_detail_extras);
            headerImageView = (ImageView) view.findViewById(R.id.header_image_view);
            parent = (LinearLayout) view.findViewById(R.id.dashboard_detail);
            shareButton = (ImageButton) view.findViewById(R.id.course_detail_share); //invisible by default

            // Full course name should appear on the course's dashboard screen.
            courseTextName.setEllipsize(null);
            courseTextName.setSingleLine(false);
        } else {
            view = inflater.inflate(R.layout.fragment_course_dashboard_disabled, container, false);
            errorText = (TextView) view.findViewById(R.id.error_msg);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isCoursewareAccessible) {
            final LayoutInflater inflater = LayoutInflater.from(getActivity());

            if (courseData.isCertificateEarned() && environment.getConfig().areCertificateLinksEnabled()) {
                final View child = inflater.inflate(R.layout.row_course_dashboard_cert, parent, false);
                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        environment.getRouter().showCertificate(getActivity(), courseData);
                    }
                });
                parent.addView(child);
            }

            //Implementation Note: - we can create a list view and populate the list.
            //but as number of rows are fixed and each row is different. the only common
            //thing is UI layout. so we reuse the same UI layout programmatically here.
            ViewHolder holder = createViewHolder(inflater, parent);

            holder.typeView.setIcon(FontAwesomeIcons.fa_list_alt);
            holder.titleView.setText(R.string.courseware_title);
            holder.subtitleView.setText(R.string.courseware_subtitle);
            holder.rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                environment.getRouter().showCourseContainerOutline(getActivity(), courseData);

                if(environment.getConfig().isJumpToLastAccessedModuleEnabled()) {
                    lastAccessManager.fetchLastAccessed(new LastAccessManagerCallback() {
                        private boolean isFetchingLastAccessed;

                        @Override
                        public boolean isFetchingLastAccessed() {
                            return isFetchingLastAccessed;
                        }

                        @Override
                        public void showLastAccessedView(String lastAccessedSubSectionId, String courseId, View view) {
                            if (courseId != null && lastAccessedSubSectionId != null) {
                                CourseComponent lastAccessComponent = courseManager.getComponentById(courseId, lastAccessedSubSectionId);
                                if(lastAccessComponent != null) {
                                    if (lastAccessComponent.getParent().isVertical()) {
                                        if (lastAccessComponent.getParent().getParent().isSequential()) {
                                            environment.getRouter().showCourseContainerOutline(
                                                    getActivity(), courseData, lastAccessComponent.getParent().getParent().getId());
                                        }
                                    }

                                    if (lastAccessComponent.isContainer()) {
                                        environment.getRouter().showCourseContainerOutline(
                                                getActivity(), courseData, lastAccessComponent.getId());
                                    } else {
                                        environment.getRouter().showCourseUnitDetail(
                                                CourseDashboardFragment.this, 0, courseData, lastAccessComponent.getId(), false);
                                    }
                                }
                            }
                        }

                        @Override
                        public void setFetchingLastAccessed(boolean accessed) {
                            this.isFetchingLastAccessed = accessed;
                        }
                    }, courseData.getCourse().getId());
                }
                }
            });

            if (environment.getConfig().isCourseVideosEnabled()) {
                holder = createViewHolder(inflater, parent);

                holder.typeView.setIcon(FontAwesomeIcons.fa_film);
                holder.titleView.setText(R.string.videos_title);
                holder.subtitleView.setText(R.string.videos_subtitle);
                holder.rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        environment.getRouter().showCourseContainerOutline(getActivity(),
                                courseData, true);
                    }
                });
            }

            if (courseData != null
                    && !TextUtils.isEmpty(courseData.getCourse().getDiscussionUrl())
                    && environment.getConfig().isDiscussionsEnabled()) {
                holder = createViewHolder(inflater, parent);
                holder.typeView.setIcon(FontAwesomeIcons.fa_comments_o);
                holder.titleView.setText(R.string.discussion_title);
                holder.subtitleView.setText(R.string.discussion_subtitle);
                holder.rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        environment.getRouter().showCourseDiscussionTopics(getActivity(), courseData);
                    }
                });
            }

            if (courseData != null
                    && !TextUtils.isEmpty(courseData.getCourse().getCourse_handouts())) {
                holder = createViewHolder(inflater, parent);
                holder.typeView.setIcon(FontAwesomeIcons.fa_file_text_o);
                holder.titleView.setText(R.string.handouts_title);
                holder.subtitleView.setText(R.string.handouts_subtitle);
                holder.rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (courseData != null)
                            environment.getRouter().showHandouts(getActivity(), courseData);
                    }
                });
            }

            if (environment.getConfig().isAnnoucementsEnabled()) {
                holder = createViewHolder(inflater, parent);
                holder.typeView.setIcon(FontAwesomeIcons.fa_bullhorn);
                holder.titleView.setText(R.string.announcement_title);
                holder.subtitleView.setText(R.string.announcement_subtitle);
                holder.rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (courseData != null)
                            environment.getRouter().showCourseAnnouncement(getActivity(), courseData);
                    }
                });
            }

            if (environment.getConfig().isCourseDatesEnabled()) {
                holder = createViewHolder(inflater, parent);
                holder.typeView.setIcon(FontAwesomeIcons.fa_calendar);
                holder.titleView.setText(R.string.course_dates_title);
                holder.subtitleView.setText(R.string.course_dates_subtitle);
                holder.rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (courseData != null) {
                            environment.getRouter().showCourseDatesActivity(getActivity(), courseData);
                        }
                    }
                });
            }
        } else {
            errorText.setText(R.string.course_not_started);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (courseData == null || !isCoursewareAccessible) return;

        final String headerImageUrl = courseData.getCourse().getCourse_image(environment.getConfig().getApiHostURL());
        Glide.with(CourseDashboardFragment.this)
                .load(headerImageUrl)
                .placeholder(R.drawable.placeholder_course_card_image)
                .transform(new TopAnchorFillWidthTransformation(getActivity()))
                .into(headerImageView);

        courseTextName.setText(courseData.getCourse().getName());
        CourseEntry course = courseData.getCourse();
        courseTextDetails.setText(course.getDescriptionWithStartDate(getActivity()));

        if (environment.getConfig().isCourseSharingEnabled()) {
            shareButton.setVisibility(headerImageView.VISIBLE);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openShareMenu();
                }
            });
        }
    }

    /**
     * Creates a dropdown menu with appropriate apps when the share button is clicked.
     */
    private void openShareMenu() {
        final String baseUrl = courseData.getCourse().getCourse_about().replace("https://learn.proversity.org", environment.getConfig().getApiHostURL());
        final String shareTextWithPlatformName = ResourceUtil.getFormattedString(
                getResources(),
                R.string.share_course_message,
                "platform_name",
                getString(R.string.platform_name)).toString() + "\n" + baseUrl;
        ShareUtils.showShareMenu(
                ShareUtils.newShareIntent(shareTextWithPlatformName),
                getActivity().findViewById(R.id.course_detail_share),
                new ShareUtils.ShareMenuItemListener() {
                    @Override
                    public void onMenuItemClick(@NonNull ComponentName componentName, @NonNull ShareUtils.ShareType shareType) {
                        final String shareText;
                        if (shareType == ShareUtils.ShareType.UNKNOWN) {
                            shareText = shareTextWithPlatformName;
                        } else {
                            shareText = getSharingText(shareType);
                        }
                        analyticsRegistry.courseDetailShared(courseData.getCourse().getId(), shareText, shareType);
                        final Intent intent = ShareUtils.newShareIntent(shareText);
                        intent.setComponent(componentName);
                        startActivity(intent);
                    }

                    @NonNull
                    private String getSharingText(@NonNull ShareUtils.ShareType shareType) {
                        String courseUrl = baseUrl;
                        if (!TextUtils.isEmpty(shareType.getUtmParamKey())) {
                            final String utmParams = courseData.getCourse().getCourseSharingUtmParams(shareType.getUtmParamKey());
                            if (!TextUtils.isEmpty(utmParams)) {
                                courseUrl += "?" + utmParams;
                            }
                        }
                        final String platform;
                        final String twitterTag = environment.getConfig().getTwitterConfig().getHashTag();
                        if (shareType == ShareUtils.ShareType.TWITTER && !TextUtils.isEmpty(twitterTag)) {
                            platform = twitterTag;
                        } else {
                            platform = getString(R.string.platform_name);
                        }
                        return ResourceUtil.getFormattedString(
                                getResources(), R.string.share_course_message, "platform_name", platform).toString() +
                                "\n" + courseUrl;
                    }
                });
    }

    private ViewHolder createViewHolder(LayoutInflater inflater, LinearLayout parent) {
        ViewHolder holder = new ViewHolder();
        holder.rowView = inflater.inflate(R.layout.row_course_dashboard_list, parent, false);
        holder.typeView = (IconImageView) holder.rowView.findViewById(R.id.row_type);
        holder.titleView = (TextView) holder.rowView.findViewById(R.id.row_title);
        holder.subtitleView = (TextView) holder.rowView.findViewById(R.id.row_subtitle);
        parent.addView(holder.rowView);
        return holder;
    }

    private class ViewHolder {
        View rowView;
        IconImageView typeView;
        TextView titleView;
        TextView subtitleView;
    }
}

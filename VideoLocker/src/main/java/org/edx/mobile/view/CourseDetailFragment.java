package org.edx.mobile.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.inject.Inject;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconImageView;

import org.apache.http.protocol.HTTP;
import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.course.CourseDetail;
import org.edx.mobile.course.GetCourseDetailTask;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.FileUtil;
import org.edx.mobile.util.images.CourseCardUtils;
import org.edx.mobile.util.images.TopAnchorFillWidthTransformation;
import org.edx.mobile.view.custom.EdxWebView;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import java.io.IOException;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;


public class CourseDetailFragment extends RoboFragment {

    @Nullable
    private GetCourseDetailTask getCourseDetailTask;

    private TextView courseTextName;
    private TextView courseTextDetails;
    private ImageView headerImageView;
    private ImageView headerPlayIcon;

    private LinearLayout courseDetailLayout;

    private TextView shortDescription;

    private LinearLayout courseDetailFieldLayout;

    private LinearLayout courseAbout;
    private EdxWebView courseAboutWebView;

    static public final String COURSE_DETAIL = "course_detail";

    @InjectExtra(COURSE_DETAIL)
    CourseDetail courseDetail;


    protected final Logger logger = new Logger(getClass().getName());


    @Inject
    IEdxEnvironment environment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            courseDetail = bundle.getParcelable(Router.EXTRA_COURSE_DETAIL);
        }
    }

    /**
     * Sets the view for the Course Card and play button if there is an intro video.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Course Card View
        View view;
        view = inflater.inflate(R.layout.fragment_course_dashboard, container, false);
        courseTextName = (TextView) view.findViewById(R.id.course_detail_name);
        courseTextDetails = (TextView) view.findViewById(R.id.course_detail_extras);
        headerImageView = (ImageView) view.findViewById(R.id.header_image_view);
        headerPlayIcon = (ImageView) view.findViewById(R.id.header_play_icon);
        courseDetailLayout = (LinearLayout) view.findViewById(R.id.dashboard_detail);

        headerPlayIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(courseDetail.media.course_video.uri);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        return view;
    }

    /**
     * Populates the course details.
     * Creates and populates the short description if given,
     * Creates and populates fields such as "effort", "duration" if any. This is handled on a case
     * by case basis rather than using a list view.
     * Sets the view for About this Course which is retrieved in a later api call.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Short Description
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View child = inflater.inflate(R.layout.fragment_course_detail, courseDetailLayout, false);
        shortDescription = (TextView) child.findViewById(R.id.course_detail_short_description);
        if (courseDetail.short_description == null || courseDetail.short_description.isEmpty()) {
            ((ViewGroup) shortDescription.getParent()).removeView(shortDescription);
        }

        courseDetailLayout.addView(child);

        // Course Detail Fields - if any fields exist

        courseDetailFieldLayout = (LinearLayout) view.findViewById(R.id.course_detail_fields);
        if (courseDetail.effort != null && !courseDetail.effort.isEmpty()) {
            ViewHolder holder = createCourseDetailFieldViewHolder(inflater, courseDetailLayout);
            holder.rowIcon.setIcon(FontAwesomeIcons.fa_dashboard);
            holder.rowFieldName.setText("Effort:");
            holder.rowFieldText.setText(courseDetail.effort);
        }

        //  About this Course
        courseAbout = (LinearLayout) view.findViewById(R.id.course_detail_course_about);
        courseAboutWebView = (EdxWebView) courseAbout.findViewById(R.id.course_detail_course_about_webview);
    }

    /**
     * Populates the information for course details.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setCourseImage();
        setCourseVideoButton();
        setCourseCardText();
        shortDescription.setText(courseDetail.short_description);
        setAboutThisCourse();
    }

    private void setCourseCardText() {
        String formattedDate = CourseCardUtils.getFormattedDate(
                getActivity(),
                courseDetail.start,
                courseDetail.end,
                courseDetail.start_type,
                courseDetail.start_display);
        courseTextDetails.setText(CourseCardUtils.getDescription(courseDetail.org, courseDetail.number, formattedDate));
        courseTextName.setText(courseDetail.name);
    }

    private void setCourseImage() {
        final String headerImageUrl = environment.getConfig().getApiHostURL() + courseDetail.media.course_image.uri;
        Glide.with(CourseDetailFragment.this)
                .load(headerImageUrl)
                .placeholder(R.drawable.edx_map_login)
                .transform(new TopAnchorFillWidthTransformation(getActivity()))
                .into(headerImageView);
    }

    private void setCourseVideoButton() {
        if (courseDetail.media.course_video.uri == null || courseDetail.media.course_video.uri.isEmpty()) {
            headerPlayIcon.setEnabled(false);
        } else {
            headerPlayIcon.setVisibility(headerPlayIcon.VISIBLE);
        }
    }

    /**
     * Makes a call the the course details api and sets the overview if given. If there is no
     * overview, remove the courseAbout view.
     */
    private void setAboutThisCourse() {
        getCourseDetailTask = new GetCourseDetailTask(getActivity(), courseDetail.course_id) {
            @Override
            protected void onSuccess(CourseDetail courseDetail) throws Exception {
                super.onSuccess(courseDetail);
                if (courseDetail.overview != null && !courseDetail.overview.isEmpty()) {
                    setAboutThisCourse(courseDetail.overview);
                }
                else {
                    courseAbout.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);
                showErrorMessage(e);
            }
        };

        getCourseDetailTask.setProgressCallback(null);
        getCourseDetailTask.execute();
    }

    /**
     * Takes a string which can include html and then renders it into the courseAbout webview.
     *
     * @param overview A string that can contain html tags
     */
    private void setAboutThisCourse(String overview) {
        courseAbout.setVisibility(View.VISIBLE);
        URLInterceptorWebViewClient client = new URLInterceptorWebViewClient(
                getActivity(), courseAboutWebView);
        client.setAllLinksAsExternal(true);

        StringBuffer buff = new StringBuffer();
        buff.append("<head>");
        buff.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        try {
            String cssFileContent = FileUtil.loadTextFileFromAssets(getActivity(), "css/render-html-in-webview.css");
            buff.append("<style>");
            buff.append(cssFileContent);
            buff.append("</style>");
        } catch (IOException e) {
            logger.error(e);
        }
        buff.append("</head>");
        buff.append("<body>");
        buff.append("<div class=\"header\">");
        buff.append(overview);
        buff.append("</div>");
        buff.append("</body>");
        courseAboutWebView.clearCache(true);
        courseAboutWebView.loadDataWithBaseURL(environment.getConfig().getApiHostURL(), buff.toString(), "text/html", HTTP.UTF_8, null);
    }

    /**
     * Creates a ViewHolder for a course detail field such as "effort" or "duration" and then adds
     * it to the top of the list.
     */
    private ViewHolder createCourseDetailFieldViewHolder(LayoutInflater inflater, LinearLayout parent) {
        ViewHolder holder = new ViewHolder();
        holder.rowView = inflater.inflate(R.layout.course_detail_field, parent, false);

        holder.rowIcon = (IconImageView) holder.rowView.findViewById(R.id.course_detail_field_icon);
        holder.rowFieldName = (TextView) holder.rowView.findViewById(R.id.course_detail_field_name);
        holder.rowFieldText = (TextView) holder.rowView.findViewById(R.id.course_detail_field_text);

        courseDetailFieldLayout.addView(holder.rowView, 0);
        return holder;
    }

    private class ViewHolder {
        View rowView;
        IconImageView rowIcon;
        TextView rowFieldName;
        TextView rowFieldText;
    }
}

package org.edx.mobile.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.discussion.CourseDiscussionInfo;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.task.GetCourseDiscussionInfoTask;
import org.edx.mobile.third_party.iconify.IconView;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.util.images.TopAnchorFillWidthTransformation;
import org.edx.mobile.view.common.TaskProcessCallback;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class CourseDashboardFragment extends RoboFragment {
    public interface ShowCourseOutlineCallback{
        void showCourseOutline();
    }
    protected final Logger logger = new Logger(getClass().getName());
    static public String TAG = CourseHandoutFragment.class.getCanonicalName();
    static public String CourseData = TAG + ".course_data";

    private EnrolledCoursesResponse courseData;
    private GetCourseDiscussionInfoTask getCourseDiscussionInfoTask;

    @Inject
    IEdxEnvironment environment;
    @InjectView(R.id.course_detail_name)
    private TextView courseTextName;
    @InjectView(R.id.course_detail_extras)
    private TextView courseTextDetails;
    @InjectView(R.id.header_image_view)
    private ImageView headerImageView;
    @InjectView(R.id.dashboard_detail)
    private LinearLayout parent;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_dashboard, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Implementation Note: - we can create a list view and populate the list.
        //but as number of rows are fixed and each row is different. the only common
        //thing is UI layout. so we reuse the same UI layout programmatically here.
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        ViewHolder holder = createViewHolder(inflater, parent);

        holder.typeView.setIcon(Iconify.IconValue.fa_list_alt);
        holder.titleView.setText(R.string.courseware_title);
        holder.subtitleView.setText(R.string.courseware_subtitle);
        holder.rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if ( activity != null && activity instanceof ShowCourseOutlineCallback){
                    ((ShowCourseOutlineCallback)activity).showCourseOutline();
                }
            }
        });

        holder = createViewHolder(inflater, parent);

        holder.typeView.setIcon(Iconify.IconValue.fa_comments_o);
        holder.titleView.setText(R.string.discussion_title);
        holder.subtitleView.setText(R.string.discussion_subtitle);
        holder.rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchCourseDiscussionMetaData();
            }
        });

        holder = createViewHolder(inflater, parent);

        holder.typeView.setIcon(Iconify.IconValue.fa_file_text_o);
        holder.titleView.setText(R.string.handouts_title);
        holder.subtitleView.setText(R.string.handouts_subtitle);
        holder.rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( courseData != null )
                    environment.getRouter().showHandouts(getActivity(), courseData);
            }
        });

        holder = createViewHolder(inflater, parent);

        holder.typeView.setIcon(Iconify.IconValue.fa_bullhorn);
        holder.titleView.setText(R.string.announcement_title);
        holder.subtitleView.setText(R.string.announcement_subtitle);
        holder.rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( courseData != null )
                    environment.getRouter().showCourseAnnouncement(getActivity(), environment.getConfig(), courseData);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            final Bundle bundle = getArguments();
            courseData = (EnrolledCoursesResponse) bundle
                    .getSerializable(CourseData);

            if ( courseData == null )
                return;

            final String headerImageUrl = courseData.getCourse().getCourse_image(environment.getConfig());
            Glide.with(CourseDashboardFragment.this)
                    .load(headerImageUrl)
                    .placeholder(R.drawable.edx_map_login)
                    .transform(new TopAnchorFillWidthTransformation(getActivity()))
                    .into(headerImageView);

            courseTextName.setText(courseData.getCourse().getName());
            CourseEntry course = courseData.getCourse();
            courseTextDetails.setText(course.getDescription(getActivity()));

        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    protected void fetchCourseDiscussionMetaData() {

        if ( getCourseDiscussionInfoTask != null ){
            getCourseDiscussionInfoTask.cancel(true);
        }
        getCourseDiscussionInfoTask = new GetCourseDiscussionInfoTask(getActivity(), courseData.getCourse().getId(), false) {
            @Override
            public void onSuccess(CourseDiscussionInfo discussionInfo) {
                    Activity activity = getActivity();
                    if ( activity instanceof TaskProcessCallback){
                        if ( courseData != null )
                            environment.getRouter().showCourseDiscussionTopics(getActivity(), courseData);
                    }
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
            }
        };

        getCourseDiscussionInfoTask.execute();

    }

    private ViewHolder createViewHolder(LayoutInflater inflater, LinearLayout parent){
        ViewHolder holder = new ViewHolder();
        holder.rowView = inflater.inflate(R.layout.row_course_dashboard_list, null);
        holder.typeView = (IconView) holder.rowView.findViewById(R.id.row_type);
        holder.titleView = (TextView) holder.rowView.findViewById(R.id.row_title);
        holder.subtitleView = (TextView) holder.rowView.findViewById(R.id.row_subtitle);
        parent.addView(holder.rowView);
        return holder;
    }

    private class ViewHolder {
        View rowView;
        IconView typeView;
        TextView titleView;
        TextView subtitleView;
    }
}

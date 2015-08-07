package org.edx.mobile.view;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.third_party.iconify.IconView;
import org.edx.mobile.third_party.iconify.Iconify;

import roboguice.fragment.RoboFragment;

public class CourseDashboardFragment extends RoboFragment {

    private TextView courseTextName;
    private TextView courseTextDetails;

    public static interface ShowCourseOutlineCallback{
        void showCourseOutline();
    }
    protected final Logger logger = new Logger(getClass().getName());
    static public String TAG = CourseHandoutFragment.class.getCanonicalName();
    static public String CourseData = TAG + ".course_data";

    private EnrolledCoursesResponse courseData;

    @Inject
    IEdxEnvironment environment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_dashboard, container,
                false);

        courseTextName = (TextView) view.findViewById(R.id.course_detail_name);
        courseTextDetails = (TextView) view.findViewById(R.id.course_detail_extras);

        //Implementation Note: - we can create a list view and populate the list.
        //but as number of rows are fixed and each row is different. the only common
        //thing is UI layout. so we reuse the same UI layout programmatically here.
        LinearLayout parent = (LinearLayout)  view.findViewById(R.id.dashboard_detail);


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
                if ( courseData != null )
                    environment.getRouter().showCourseDiscussionTopics(getActivity(), courseData);
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

        return view;
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

            String headerImageUrl = courseData.getCourse().getCourse_image(environment.getConfig());
            NetworkImageView headerImageView = (NetworkImageView)getView().findViewById(R.id.header_image_view);
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            float screenWidth = displayMetrics.widthPixels;
            float ideaHeight = screenWidth * 9 / 16;

            headerImageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) ideaHeight));
            headerImageView.requestLayout();

            headerImageView.setImageUrl(headerImageUrl, environment.getImageCacheManager().getImageLoader() );

            courseTextName.setText(courseData.getCourse().getName());
            CourseEntry course = courseData.getCourse();
            courseTextDetails.setText( course.getDescription(this.getActivity()));

        } catch (Exception ex) {
            logger.error(ex);
        }
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

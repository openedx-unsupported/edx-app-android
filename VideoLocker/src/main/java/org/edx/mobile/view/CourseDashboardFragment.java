package org.edx.mobile.view;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.util.images.ImageCacheManager;

public class CourseDashboardFragment extends Fragment {
    private TextView courseTextName;
    private TextView courseTextDetails;

    public static interface ShowCourseOutlineCallback{
        void showCourseOutline();
    }
    protected final Logger logger = new Logger(getClass().getName());
    static public String TAG = CourseHandoutFragment.class.getCanonicalName();
    static public String CourseData = TAG + ".course_data";

    private ShowCourseOutlineCallback callback;
    private EnrolledCoursesResponse courseData;

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

        Iconify.setIcon(holder.typeView, Iconify.IconValue.fa_list_alt );
        holder.titleView.setText(R.string.course_title);
        holder.subtitleView.setText(R.string.course_subtitle);
        holder.rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( callback != null)
                    callback.showCourseOutline();
            }
        });

        holder = createViewHolder(inflater, parent);

        Iconify.setIcon(holder.typeView, Iconify.IconValue.fa_comments_o );
        holder.titleView.setText(R.string.discussion_title);
        holder.subtitleView.setText(R.string.discussion_subtitle);
        holder.rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FIXME - hook to forum activities
            }
        });

        holder = createViewHolder(inflater, parent);

        Iconify.setIcon(holder.typeView, Iconify.IconValue.fa_file_text_o );
        holder.titleView.setText(R.string.handouts_title);
        holder.subtitleView.setText(R.string.handouts_subtitle);
        holder.rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( courseData != null )
                    Router.getInstance().showHandouts(getActivity(), courseData);
            }
        });

        holder = createViewHolder(inflater, parent);

        Iconify.setIcon(holder.typeView, Iconify.IconValue.fa_bullhorn );
        holder.titleView.setText(R.string.announcement_title);
        holder.subtitleView.setText(R.string.announcement_subtitle);
        holder.rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( courseData != null )
                    Router.getInstance().showCourseAnnouncement(getActivity(), courseData);
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

            String headerImageUrl = courseData.getCourse().getCourse_image(getActivity());
            NetworkImageView headerImageView = (NetworkImageView)getView().findViewById(R.id.header_image_view);
            headerImageView.setImageUrl(headerImageUrl, ImageCacheManager.getInstance().getImageLoader() );

            courseTextName.setText(courseData.getCourse().getName());
            CourseEntry course = courseData.getCourse();
            courseTextDetails.setText( course.getDescription(this.getActivity()));

        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void setCallback(ShowCourseOutlineCallback callback){
        this.callback = callback;
    }

    private ViewHolder createViewHolder(LayoutInflater inflater, LinearLayout parent){
        ViewHolder holder = new ViewHolder();
        holder.rowView = inflater.inflate(R.layout.row_course_dashboard_list, null);
        holder.typeView = (TextView) holder.rowView.findViewById(R.id.row_type);
        holder.arrowView = (TextView) holder.rowView.findViewById(R.id.right_arrow);
        holder.titleView = (TextView) holder.rowView.findViewById(R.id.row_title);
        holder.subtitleView = (TextView) holder.rowView.findViewById(R.id.row_subtitle);
        parent.addView(holder.rowView);
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            Configuration config = getResources().getConfiguration();
            if(config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                Iconify.setIcon(holder.arrowView, Iconify.IconValue.fa_angle_left );
            } else {
                Iconify.setIcon(holder.arrowView, Iconify.IconValue.fa_angle_right );
            }
        } else {
            Iconify.setIcon(holder.arrowView, Iconify.IconValue.fa_angle_right );
        }
        return holder;
    }

    private class ViewHolder {
        View rowView;
        TextView typeView;
        TextView arrowView;
        TextView titleView;
        TextView subtitleView;
    }
}

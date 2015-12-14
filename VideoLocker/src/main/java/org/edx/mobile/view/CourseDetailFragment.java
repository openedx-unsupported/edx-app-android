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

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.course.CourseDetail;
import org.edx.mobile.course.GetCourseDetailTask;
import org.edx.mobile.third_party.iconify.IconView;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.util.images.CourseCardUtils;
import org.edx.mobile.util.images.TopAnchorFillWidthTransformation;
import org.edx.mobile.view.custom.EButton;

import roboguice.fragment.RoboFragment;


public class CourseDetailFragment extends RoboFragment {

    @Nullable
    private GetCourseDetailTask getCourseDetailTask;

    private CourseDetailActivity courseDetailActivity;

    private TextView courseTextName;
    private TextView courseTextDetails;
    private ImageView headerImageView;
    private ImageView headerPlayIcon; // TODO probably doesn't need to be an ImageView since this icon is stored
    private EButton enrollButton;
    private LinearLayout parent;
    private LinearLayout courseDetailFieldContainer; //TODO is container the correct name?
    private TextView shortDescription;

    private CourseDetail courseDetail;

    @Inject
    IEdxEnvironment environment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            courseDetail = bundle.getParcelable(Router.EXTRA_COURSE_DETAIL);
        }

        getCourseDetailTask = new GetCourseDetailTask(getActivity(), courseDetail.course_id) {
            @Override
            protected void onSuccess(CourseDetail courseDetail) throws Exception {
                super.onSuccess(courseDetail);
                // TODO Add the course info to the course info window.

            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);
                showErrorMessage(e);
            }
        };
        getCourseDetailTask.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Course Card View
        View view;
        view = inflater.inflate(R.layout.fragment_course_dashboard, container, false);
        courseTextName = (TextView) view.findViewById(R.id.course_detail_name);
        courseTextDetails = (TextView) view.findViewById(R.id.course_detail_extras);
        headerImageView = (ImageView) view.findViewById(R.id.header_image_view);
        headerPlayIcon = (ImageView) view.findViewById(R.id.header_play_icon);
        parent = (LinearLayout) view.findViewById(R.id.dashboard_detail);
        enrollButton = (EButton) view.findViewById(R.id.button_enroll_now);

        headerPlayIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = environment.getConfig().getApiHostURL() + courseDetail.media.course_video.uri;
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

//        enrollButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Where does this button go?
//                System.out.println("enroll click");
//            }
//        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Short Description
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View child = inflater.inflate(R.layout.fragment_course_detail, parent, false);
        shortDescription = (TextView) child.findViewById(R.id.course_detail_short_description);
        if (courseDetail.short_description == null || courseDetail.short_description.isEmpty()) {
            ((ViewGroup) shortDescription.getParent()).removeView(shortDescription);
        }
        parent.addView(child);

        courseDetailFieldContainer = (LinearLayout) view.findViewById(R.id.course_field_list_container);


        // Course Detail Fields
        ViewHolder holder = createViewHolder(inflater, parent);
        // TODO effort in if statement
//        if (true) {
            holder.rowIcon.setIcon(Iconify.IconValue.fa_dashboard);
            // TODO efort in text
            holder.rowFieldName.setText("Effort:");
            holder.rowFieldText.setText("I AM A HAMMOCK!");
//        }

        holder = createViewHolder(inflater, parent);
        // TODO effort in if statement
//        if (true) {
            holder.rowIcon.setIcon(Iconify.IconValue.fa_clock_o);
            // TODO efort in text
            holder.rowFieldName.setText("Length:");
            holder.rowFieldText.setText("I AM A HAMMOCK!");

//        }

        // Course About



    }

    private ViewHolder createViewHolder(LayoutInflater inflater, LinearLayout parent) {
        ViewHolder holder = new ViewHolder();
        holder.rowView = inflater.inflate(R.layout.course_detail_field, parent, false);

        holder.rowIcon = (IconView) holder.rowView.findViewById(R.id.course_detail_field_icon);
        holder.rowFieldName = (TextView) holder.rowView.findViewById(R.id.course_detail_field_name);
        holder.rowFieldText = (TextView) holder.rowView.findViewById(R.id.course_detail_field_text);

        courseDetailFieldContainer.addView(holder.rowView);
        return holder;
    }

    private class ViewHolder {
        View rowView;
        IconView rowIcon;
        TextView rowFieldName;
        TextView rowFieldText;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setCourseImage();
        setCourseVideoButton();
        setCourseCardText();
        shortDescription.setText(courseDetail.short_description);
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
        final String headerImageUrl = environment.getConfig().getApiHostURL() +courseDetail.media.course_image.uri;
        Glide.with(CourseDetailFragment.this)
                .load(headerImageUrl)
                .placeholder(R.drawable.edx_map_login)
                .transform(new TopAnchorFillWidthTransformation(getActivity()))
                .into(headerImageView);
    }

    private void setCourseVideoButton() {
        if (courseDetail.media.course_video.uri == null) {
            headerPlayIcon.setEnabled(false);
        } else {
            headerPlayIcon.setVisibility(headerPlayIcon.VISIBLE);
        }
    }

//    private void setAboutThisCourse() {
//
//    }
}

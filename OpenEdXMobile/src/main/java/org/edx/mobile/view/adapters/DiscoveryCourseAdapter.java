package org.edx.mobile.view.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.edx.mobile.R;
import org.edx.mobile.databinding.RowDiscoveryCourseBinding;
import org.edx.mobile.discovery.model.CourseRuns;
import org.edx.mobile.programs.ResumeCourse;

import java.util.List;

public class DiscoveryCourseAdapter extends RecyclerView.Adapter<DiscoveryCourseAdapter.DiscoveryCourseViewHolder> {
    private List<CourseRuns> programCoursesLists;
    private boolean enroll;
    private ResumeCourse resumeCourse;
    private Context context;
    private OnRecyclerItemClickListener listener;
    private long lastClickTime;

    public DiscoveryCourseAdapter(Context context, OnRecyclerItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.lastClickTime = 0;
    }

    @NonNull
    @Override
    public DiscoveryCourseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
      /*  DiscoveryCourseViewHolder viewHolder = new DiscoveryCourseAdapter.DiscoveryCourseViewHolder(RowDiscoveryCourseBinding.
                inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);*/
        return new DiscoveryCourseViewHolder(RowDiscoveryCourseBinding.
                inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DiscoveryCourseViewHolder holder, int position) {
        final CourseRuns model = programCoursesLists.get(position);
        holder.itemBinding.course.setText(/*"COURSE "*/ context.getString(R.string.course_caps) + " " + String.valueOf(position + 1));
        if (enroll) {
            holder.itemBinding.shimmerLayoutStatus.startShimmer();
            holder.itemBinding.shimmerLayoutStatus.setVisibility(View.VISIBLE);
            if (model.getCourse_status() != null) {
                holder.itemBinding.shimmerLayoutStatus.stopShimmer();
                holder.itemBinding.shimmerLayoutStatus.setVisibility(View.GONE);
                if (model.getCourse_status().toLowerCase().equals("completed")) {
                    holder.itemBinding.lnCourseStatus.setBackgroundColor(Color.parseColor("#7CCBB7"));
                    holder.itemBinding.courseStatus.setText("COMPLETED");
                } else if (model.getCourse_status().toLowerCase().equals("not_started")) {
                    holder.itemBinding.lnCourseStatus.setBackgroundColor(Color.parseColor("#C8A1DE"));
                    holder.itemBinding.courseStatus.setText("NOT STARTED");
                } else {
                    holder.itemBinding.lnCourseStatus.setBackgroundColor(Color.parseColor("#F9E2A6"));
                    holder.itemBinding.courseStatus.setText("IN PROGRESS");
                }
                holder.itemBinding.cvCourseStatus.setVisibility(View.VISIBLE);
            }
        } else {
            holder.itemBinding.cvCourseStatus.setVisibility(View.GONE);
        }
        holder.itemBinding.courseCardNotEnrolled.setVisibility(View.GONE);
        holder.itemBinding.courseCardEnrolled.setVisibility(View.GONE);
        if (enroll) {
            holder.itemBinding.courseCardNotEnrolled.setVisibility(View.GONE);
            holder.itemBinding.courseCardEnrolled.setVisibility(View.VISIBLE);
            holder.itemBinding.shimmerLayoutViewButton.startShimmer();
            holder.itemBinding.shimmerLayoutViewButton.setVisibility(View.VISIBLE);
            if (model.getCourse_status() != null) {
                holder.itemBinding.shimmerLayoutViewButton.stopShimmer();
                holder.itemBinding.shimmerLayoutViewButton.setVisibility(View.GONE);
                if (resumeCourse != null) {
                    if (resumeCourse.getCourse_id().equals(model.getKey())) {
                        holder.itemBinding.viewButton.setVisibility(View.GONE);
                        holder.itemBinding.contnueButton.setVisibility(View.VISIBLE);
                    } else {
                        holder.itemBinding.viewButton.setVisibility(View.VISIBLE);
                        holder.itemBinding.contnueButton.setVisibility(View.GONE);
                    }
                } else {
                    holder.itemBinding.viewButton.setVisibility(View.VISIBLE);
                    holder.itemBinding.contnueButton.setVisibility(View.GONE);
                }
            }
            holder.itemBinding.courseNameEnrolled.setText(model.getTitle());
        } else {
            holder.itemBinding.courseCardNotEnrolled.setVisibility(View.VISIBLE);
            holder.itemBinding.courseCardEnrolled.setVisibility(View.GONE);
            holder.itemBinding.courseName.setText(model.getTitle());
        }
        holder.itemBinding.viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view, model);
            }
        });
        holder.itemBinding.contnueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view, model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return programCoursesLists == null ? 0 : programCoursesLists.size();
    }

    public void setProgramCoursesLists(List<CourseRuns> programCoursesLists, boolean enroll,
                                       ResumeCourse resumeCourse) {
        this.programCoursesLists = programCoursesLists;
        this.enroll = enroll;
        this.resumeCourse = resumeCourse;
        notifyDataSetChanged();
    }

    public void setEnroll(boolean enroll) {
        this.enroll = enroll;
        notifyDataSetChanged();
    }
    public void setResumeCourse(ResumeCourse resumeCourse) {
        this.resumeCourse = resumeCourse;
        notifyDataSetChanged();
    }

    public String getProgramCoursesIds() {
        String listOfCourseIds = "";
        for (CourseRuns courseRuns : programCoursesLists) {
            if (listOfCourseIds.isEmpty()) {
                listOfCourseIds = courseRuns.getKey();
            } else {
                listOfCourseIds = listOfCourseIds + "," + courseRuns.getKey();
            }
        }
        return listOfCourseIds;
    }

    public class DiscoveryCourseViewHolder extends RecyclerView.ViewHolder {
        private RowDiscoveryCourseBinding itemBinding;

        public DiscoveryCourseViewHolder(RowDiscoveryCourseBinding rowDiscoveryCourseBinding) {
            super(rowDiscoveryCourseBinding.getRoot());
            this.itemBinding = rowDiscoveryCourseBinding;
        }

    }
}

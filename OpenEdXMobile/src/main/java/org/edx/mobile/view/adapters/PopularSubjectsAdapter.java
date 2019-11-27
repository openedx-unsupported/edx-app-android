package org.edx.mobile.view.adapters;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.databinding.SubjectItemBinding;
import org.edx.mobile.model.SubjectModel;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.util.images.ImageUtils;

import java.util.List;

public class PopularSubjectsAdapter extends RecyclerView.Adapter {

    private List<SubjectModel> popularSubjects;
    private ClickListener clickListener;

    static class RowType {
        static final int SUBJECT_ITEM = 0;
        static final int VIEW_ALL_SUBJECTS = 1;
    }

    public interface ClickListener {
        void onSubjectClick(View view);

        void onViewAllSubjectsClick();
    }

    public PopularSubjectsAdapter(@NonNull List<SubjectModel> subjectModels,
                                  @NonNull ClickListener listener) {
        popularSubjects = subjectModels;
        clickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view;
        switch (viewType) {
            case RowType.VIEW_ALL_SUBJECTS:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_subjects_item, parent, false);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onViewAllSubjectsClick();
                    }
                });
                return new BindingViewHolder(view);
            case RowType.SUBJECT_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subject_item, parent, false);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onSubjectClick(v);
                    }
                });
                return new BindingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case RowType.SUBJECT_ITEM:
                final SubjectModel model = popularSubjects.get(position);
                final SubjectItemBinding subjectItemBinding = DataBindingUtil.bind(holder.itemView);
                subjectItemBinding.tvSubjectName.setText(model.name);
                @DrawableRes final int imageRes = UiUtil.getDrawable(subjectItemBinding.ivSubjectLogo.getContext(), model.imageName);
                ImageUtils.setRoundedCornerImage(subjectItemBinding.ivSubjectLogo, imageRes);
                holder.itemView.setContentDescription(
                        holder.itemView.getResources().getString(R.string.browse_by_subject) + " " + model.name);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return popularSubjects.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == getItemCount() - 1 ? RowType.VIEW_ALL_SUBJECTS : RowType.SUBJECT_ITEM;
    }

    class BindingViewHolder extends RecyclerView.ViewHolder {
        BindingViewHolder(View itemView) {
            super(itemView);
        }

    }
}

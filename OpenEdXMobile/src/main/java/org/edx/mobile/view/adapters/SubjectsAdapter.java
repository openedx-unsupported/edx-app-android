package org.edx.mobile.view.adapters;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import org.edx.mobile.R;
import org.edx.mobile.databinding.SubjectItemGridBinding;
import org.edx.mobile.model.SubjectModel;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.util.images.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class SubjectsAdapter extends ArrayAdapter<SubjectModel> implements Filterable {
    private List<SubjectModel> allSubjects;
    private List<SubjectModel> filteredSubjects;
    private ItemFilter filter = new ItemFilter();

    public SubjectsAdapter(@NonNull Context context, @NonNull List<SubjectModel> subjectModels) {
        super(context, 0, subjectModels);
        allSubjects = filteredSubjects = subjectModels;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.subject_item_grid, parent, false);
        }
        final SubjectModel model = getItem(position);
        final SubjectItemGridBinding subjectItemBinding = DataBindingUtil.bind(convertView);
        subjectItemBinding.tvSubjectName.setText(model.name);
        @DrawableRes final int imageRes = UiUtil.getDrawable(getContext(), model.imageName);
        ImageUtils.setRoundedCornerImage(subjectItemBinding.ivSubjectLogo, imageRes);
        convertView.setContentDescription(
                convertView.getResources().getString(R.string.browse_by_subject) + " " + model.name);
        return convertView;
    }

    @Override
    public int getCount() {
        return filteredSubjects.size();
    }

    @Nullable
    @Override
    public SubjectModel getItem(int position) {
        return filteredSubjects.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final String filterString = constraint.toString().toLowerCase();
            final FilterResults results = new FilterResults();

            final ArrayList<SubjectModel> finalList = new ArrayList<>();
            String filterableString;
            for (SubjectModel item : allSubjects) {
                filterableString = item.name.toLowerCase();
                if (filterableString.startsWith(filterString)) {
                    finalList.add(item);
                }
            }
            results.values = finalList;
            results.count = finalList.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredSubjects = (List<SubjectModel>) results.values;
            notifyDataSetChanged();
        }

    }
}

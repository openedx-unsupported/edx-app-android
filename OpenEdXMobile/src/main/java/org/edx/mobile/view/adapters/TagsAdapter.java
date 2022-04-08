package org.edx.mobile.view.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.databinding.RowTagsBinding;
import org.edx.mobile.discovery.model.TagTermResult;
import org.edx.mobile.module.prefs.LoginPrefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagsViewHolder> {

    private Context context;
    private int colorCode;
    private List<TagTermResult> tagTermResults;
    private OnRecyclerItemClickListener listener;
    String userType;

    public TagsAdapter(Context context, OnRecyclerItemClickListener listener, int colorCode) {
        this.context = context;
        this.listener = listener;
        this.colorCode = colorCode;
    }

    @NonNull
    @Override
    public TagsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new TagsViewHolder(RowTagsBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TagsViewHolder holder, int position) {
        final TagTermResult model = tagTermResults.get(position);
        LayerDrawable layerDrawable = (LayerDrawable) context.getResources()
                .getDrawable(R.drawable.tags_side_background);
        GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable
                .findDrawableByLayerId(R.id.gradientDrawble);
        if (userType != null) {
            if (userType.equals("teacher")) {
                int newColor = Color.parseColor("#147682");
                gradientDrawable.setColor(newColor);
            } else {
                int newColor = Color.parseColor("#FFB700");
                gradientDrawable.setColor(newColor);
            }
        } else {
            int newColor = Color.parseColor("#C8A1DE");
            gradientDrawable.setColor(newColor);
        }
        //  gradientDrawable.setColor(colorCode);
        holder.itemBinding.tagColorCode.setBackground(gradientDrawable);
        holder.itemBinding.tagsName.setText(model.getTerm());
        holder.itemBinding.tagCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view, model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tagTermResults == null ? 0 : tagTermResults.size();
    }

    public void setTags(List<TagTermResult> tagTermResults, String userType) {
        this.tagTermResults = tagTermResults;
        this.userType = userType;
        notifyDataSetChanged();
    }

    public class TagsViewHolder extends RecyclerView.ViewHolder {
        private RowTagsBinding itemBinding;

        public TagsViewHolder(RowTagsBinding rowTagsBinding) {
            super(rowTagsBinding.getRoot());
            this.itemBinding = rowTagsBinding;

        }
    }
}



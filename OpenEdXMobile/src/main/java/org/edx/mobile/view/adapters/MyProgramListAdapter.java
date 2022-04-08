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

import org.edx.mobile.R;
import org.edx.mobile.databinding.RowProgramEnrolledItemBinding;
import org.edx.mobile.programs.MyProgramListModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyProgramListAdapter extends RecyclerView.Adapter<MyProgramListAdapter.ProgramViewHolder> {
    private Context context;
    private List<MyProgramListModel> myProgramList;
    private OnRecyclerItemClickListener listener;

    public MyProgramListAdapter(Context context, OnRecyclerItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProgramViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ProgramViewHolder(RowProgramEnrolledItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramViewHolder holder, int position) {
        final MyProgramListModel model = myProgramList.get(position);
        String[] colorsTxt = context.getResources().getStringArray(R.array.subject_colors_name);
        List<Integer> colors = new ArrayList<Integer>();
        for (int i = 0; i < colorsTxt.length; i++) {
            int newColor = Color.parseColor(colorsTxt[i]);
            colors.add(newColor);
        }
        int rand = new Random().nextInt(colors.size());
        Integer color = colors.get(rand);
        LayerDrawable layerDrawable = (LayerDrawable) context.getResources()
                .getDrawable(R.drawable.tags_side_background);
        GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable
                .findDrawableByLayerId(R.id.gradientDrawble);
        gradientDrawable.setColor(color);
        holder.itemBinding.programColorCode.setBackground(gradientDrawable);
        holder.itemBinding.tagsName.setText(model.getTagName());
        holder.itemBinding.programName.setText(model.getProgramName());
        holder.itemBinding.tagCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view, model);
            }
        });
    }

    public void setMyProgramList(List<MyProgramListModel> myProgramList) {
        this.myProgramList = myProgramList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return myProgramList == null ? 0 : myProgramList.size();
    }

    public class ProgramViewHolder extends RecyclerView.ViewHolder {
        private RowProgramEnrolledItemBinding itemBinding;

        public ProgramViewHolder(RowProgramEnrolledItemBinding rowProgramEnrolledItemBinding) {
            super(rowProgramEnrolledItemBinding.getRoot());
            this.itemBinding = rowProgramEnrolledItemBinding;

        }
    }
}

package org.edx.mobile.view.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.edx.mobile.R;
import org.edx.mobile.databinding.RowSubjectsBinding;
import org.edx.mobile.discovery.model.DiscoverySubjectResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NewSubjectAdapter extends RecyclerView.Adapter<NewSubjectAdapter.NewSubjectViewHolder> {
    private Context context;
    private List<DiscoverySubjectResult> discoverySubjectResults;
    private OnRecyclerItemClickListener listener;

    public NewSubjectAdapter(Context context, OnRecyclerItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NewSubjectViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new NewSubjectAdapter.NewSubjectViewHolder(RowSubjectsBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NewSubjectViewHolder holder, int position) {
        final DiscoverySubjectResult model = discoverySubjectResults.get(position);
        String[] colorsTxt = context.getResources().getStringArray(R.array.subject_colors_name);
        List<Integer> colors = new ArrayList<Integer>();
        for (int i = 0; i < colorsTxt.length; i++) {
            int newColor = Color.parseColor(colorsTxt[i]);
            colors.add(newColor);
        }
        int rand = new Random().nextInt(colors.size());
        Integer color = colors.get(rand);
        model.setCardColorName(color);
        holder.itemBinding.lnSubjects.setBackgroundColor(color);
        String sourceString = "<b>" + model.getName() + "</b> ";
        holder.itemBinding.subjectName.setText(Html.fromHtml(sourceString));
        holder.itemBinding.lnSubjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view, model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return discoverySubjectResults==null ? 0 : discoverySubjectResults.size();
    }

    public void setSubjects(List<DiscoverySubjectResult> discoverySubjectResults) {
        this.discoverySubjectResults = discoverySubjectResults;
        notifyDataSetChanged();
    }

    public class NewSubjectViewHolder extends RecyclerView.ViewHolder {
        private RowSubjectsBinding itemBinding;

        public NewSubjectViewHolder(RowSubjectsBinding rowSubjectsBinding) {
            super(rowSubjectsBinding.getRoot());
            this.itemBinding = rowSubjectsBinding;

        }
    }
}

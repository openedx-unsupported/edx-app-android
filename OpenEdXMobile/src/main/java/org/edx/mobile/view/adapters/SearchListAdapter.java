package org.edx.mobile.view.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.edx.mobile.databinding.RowSearchItemsBinding;
import org.edx.mobile.discovery.model.CombinationOfSeachResult;

import java.util.List;

public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.SearchViewHolder> {
    private Context context;
    private List<CombinationOfSeachResult>searchResultLists;
    private OnRecyclerItemClickListener listener;

    public SearchListAdapter(Context context, OnRecyclerItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new SearchListAdapter.SearchViewHolder(RowSearchItemsBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        final CombinationOfSeachResult model = searchResultLists.get(position);
        String sourceString = "<b>" + model.getCourseName() + "</b> ";
        holder.itemBinding.courseName.setText(Html.fromHtml(sourceString));
        holder.itemBinding.programName.setText(model.getProgramName());
        holder.itemBinding.tagName.setText(model.getTagName());
        holder.itemBinding.searchItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view,model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchResultLists!=null? searchResultLists.size() : 0;
    }
    public void setSearchResult(List<CombinationOfSeachResult> searchResultLists) {
        this.searchResultLists = searchResultLists;
        notifyDataSetChanged();
    }
    public void updateSearchResult(List<CombinationOfSeachResult> searchResultLists) {
        this.searchResultLists.addAll(searchResultLists);
        notifyDataSetChanged();
    }
    public class SearchViewHolder extends RecyclerView.ViewHolder {
        private RowSearchItemsBinding itemBinding;

        public SearchViewHolder(RowSearchItemsBinding rowSearchItemsBinding) {
            super(rowSearchItemsBinding.getRoot());
            this.itemBinding = rowSearchItemsBinding;

        }
    }

}

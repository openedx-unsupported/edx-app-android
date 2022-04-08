package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.edx.mobile.R;
import org.edx.mobile.databinding.RowOrganisationBinding;
import org.edx.mobile.discovery.model.OrganisationModel;

import java.util.List;

public class OrganisationAdapter extends RecyclerView.Adapter<OrganisationAdapter.NewOrganisationViewHolder> {
    private Context context;
    private List<OrganisationModel> organisationModels;
    private OnRecyclerItemClickListener listener;

    public OrganisationAdapter(Context context, OnRecyclerItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }


    @NonNull
    @Override
    public NewOrganisationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new NewOrganisationViewHolder(RowOrganisationBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NewOrganisationViewHolder holder, int position) {
        final OrganisationModel model = organisationModels.get(position);
        String imageUrl = model.getLogo_image_url();
        holder.itemBinding.organisationName.setText(model.getKey());
      //  if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.v_e_logo).dontAnimate()
                    .into(holder.itemBinding.ivImageview);
    //    }
    }

    public void setOrganisation(List<OrganisationModel> organisationModels) {
        this.organisationModels = organisationModels;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return organisationModels != null ? organisationModels.size() : 0;
    }

    public class NewOrganisationViewHolder extends RecyclerView.ViewHolder {
        private RowOrganisationBinding itemBinding;

        public NewOrganisationViewHolder(RowOrganisationBinding rowOrganisationBinding) {
            super(rowOrganisationBinding.getRoot());
            this.itemBinding = rowOrganisationBinding;

        }
    }
}

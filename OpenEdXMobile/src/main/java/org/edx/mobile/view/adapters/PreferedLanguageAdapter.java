package org.edx.mobile.view.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.inject.Inject;

import org.edx.mobile.core.EdxEnvironment;
import org.edx.mobile.databinding.RowLanguageItemBinding;
import org.edx.mobile.http.notifications.DialogErrorNotification;
import org.edx.mobile.user.Account;
import org.edx.mobile.user.LanguageProficiency;
import org.edx.mobile.user.PreferedLangList;
import org.edx.mobile.user.UserAPI;
import org.edx.mobile.user.UserService;

import java.util.Collections;
import java.util.List;

public class PreferedLanguageAdapter extends RecyclerView.Adapter<PreferedLanguageAdapter.PreferedlanguageViewHolder> {
    private Context context;
    private String selected ="";

    private List<PreferedLangList> preferedLangLists;
    String userName;
    private OnUpdateLanguage onUpdateLanguage;

    public PreferedLanguageAdapter(Context context,OnUpdateLanguage onUpdateLanguage) {
        this.context = context;
        this.onUpdateLanguage = onUpdateLanguage;
    }
    @NonNull
    @Override
    public PreferedlanguageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new PreferedlanguageViewHolder(RowLanguageItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PreferedlanguageViewHolder holder, int i) {
        final PreferedLangList model = preferedLangLists.get(i);

        holder.itemBinding.language.setText(model.getName());
        if (selected!=null){
            if (selected.equals(model.getShort_name())){
                holder.itemBinding.lnLayout.setSelected(true);
            }else{
                holder.itemBinding.lnLayout.setSelected(false);
            }
        }
        holder.itemBinding.language.setContentDescription(model.getContent_description());
        holder.itemBinding.lnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (!holder.itemBinding.lnLayout.isSelected()){
                    selected = model.getShort_name();
                    onUpdateLanguage.onClick(model.getShort_name());
                   notifyDataSetChanged();
                }
            }
        });

    }

    public void setLanguages(List<PreferedLangList> preferedLangLists,String selectedlanguage) {
        this.preferedLangLists = preferedLangLists;
        this.selected = selectedlanguage;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return preferedLangLists.size();
    }
    public class PreferedlanguageViewHolder extends RecyclerView.ViewHolder {
        private RowLanguageItemBinding itemBinding;

        public PreferedlanguageViewHolder(RowLanguageItemBinding rowLanguageItemBinding) {
            super(rowLanguageItemBinding.getRoot());
            this.itemBinding = rowLanguageItemBinding;

        }
    }
    public interface OnUpdateLanguage {
        void onClick(String fieldValue);
    }
}

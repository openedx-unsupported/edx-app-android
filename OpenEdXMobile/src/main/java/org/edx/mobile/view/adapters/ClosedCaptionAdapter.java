package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;

import java.util.HashMap;

public abstract class ClosedCaptionAdapter extends BaseListAdapter<HashMap<String, String>> {

    //public int selectedPosition = -1;
    public String selectedLanguage;
    public ClosedCaptionAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_cc_list, environment);
    }

    @Override
    public void render(BaseViewHolder tag, HashMap<String, String> language) {
        final ViewHolder holder = (ViewHolder) tag;

        if(language!=null){
            holder.tv_ccLang.setText(language.values().toArray()[0].toString());
        }
        
        if(selectedLanguage!=null){
            if(selectedLanguage.equalsIgnoreCase(language.keySet().toArray()[0].toString())){
                holder.tv_ccLang.setBackgroundResource(R.color.cyan_text_navigation_20);
            }else{
                holder.tv_ccLang.setBackgroundResource(R.drawable.list_item_overlay_selector);
            }
        }else{
            holder.tv_ccLang.setBackgroundResource(R.drawable.list_item_overlay_selector);
        }
    }
    
    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.tv_ccLang = (TextView) convertView
                .findViewById(R.id.row_cc_lang);

        return holder;
    }

    private static class ViewHolder extends BaseViewHolder {
        TextView tv_ccLang;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
            long arg3) {
        HashMap<String, String> language = getItem(position);
        if(language!=null) onItemClicked(language);
    }

    public abstract void onItemClicked(HashMap<String, String> language);
}

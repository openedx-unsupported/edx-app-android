package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import org.edx.mobile.R;

import java.util.HashMap;

public abstract class ClosedCaptionAdapter extends BaseListAdapter<HashMap<String, String>> {

    //public int selectedPosition = -1;
    public String strSelectedLanguage;
    public ClosedCaptionAdapter(Context context) {
        super(context, R.layout.row_cc_list);
    }

    @Override
    public void render(BaseViewHolder tag, HashMap<String, String> language) {
        final ViewHolder holder = (ViewHolder) tag;

        if(language!=null){
            holder.txtCcLang.setText(language.values().toArray()[0].toString());
        }
        
        if(strSelectedLanguage !=null){
            if(strSelectedLanguage.equalsIgnoreCase(language.keySet().toArray()[0].toString())){
                holder.txtCcLang.setBackgroundResource(R.color.cyan_text_navigation_20);
            }else{
                holder.txtCcLang.setBackgroundResource(R.drawable.list_selector);
            }
        }else{
            holder.txtCcLang.setBackgroundResource(R.drawable.list_selector);
        }
    }
    
    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.txtCcLang = (TextView) convertView
                .findViewById(R.id.row_cc_lang);

        return holder;
    }

    private static class ViewHolder extends BaseViewHolder {
        TextView txtCcLang;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
            long arg3) {
        HashMap<String, String> language = getItem(position);
        if(language!=null) onItemClicked(language);
    }

    public abstract void onItemClicked(HashMap<String, String> language);
}

package org.edx.mobile.view.custom.cc;

import org.edx.mobile.player.IVideo.IClosedCaption;
import org.edx.mobile.view.custom.BaseListAdapter;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.android.exoplayer.R;

public abstract class ClosedCaptionAdapter extends BaseListAdapter<IClosedCaption> {

    public ClosedCaptionAdapter(Context context) {
        super(context);
    }

    @Override
    public void render(BaseViewHolder tag, IClosedCaption lang) {
        ViewHolder holder = (ViewHolder) tag;
        holder.tv_ccLang.setText(lang.getLanguage());
        if (isSelected(holder.position)) {
            holder.tv_ccLang.setBackgroundResource(R.color.cc_lang_selected);
        } else {
            holder.tv_ccLang.setBackgroundResource(R.color.list_selector);
        } 
    }
    
    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.tv_ccLang = (TextView) convertView.findViewById(R.id.row_cc_lang);
        return holder;
    }

    @Override
    public int getListItemLayoutResId() {
        return R.layout.row_cc_list;
    }

    private static class ViewHolder extends BaseViewHolder {
        TextView tv_ccLang;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
            long arg3) {
        onItemClicked(getItem(position));
    }

    public abstract void onItemClicked(IClosedCaption lang);
}

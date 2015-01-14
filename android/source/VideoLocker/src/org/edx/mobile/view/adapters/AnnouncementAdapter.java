package org.edx.mobile.view.adapters;

import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.R;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

public abstract class AnnouncementAdapter extends
        BaseListAdapter<AnnouncementsModel> {

    public AnnouncementAdapter(Context context) {
        super(context);
    }

    @Override
    public void render(BaseViewHolder tag, final AnnouncementsModel model) {
        ViewHolder holder = (ViewHolder) tag;
        holder.date.setText(model.getDate());
        holder.content.setText(Html.fromHtml(model.content));
        holder.content.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.date = (TextView) convertView
                .findViewById(R.id.announcement_date);
        
        holder.content = (TextView) convertView
                .findViewById(R.id.announcement_content);
        return holder;
    }

    @Override
    public int getListItemLayoutResId() {
        return R.layout.row_announcement_list;
    }

    private static class ViewHolder extends BaseViewHolder {
        TextView date;
        TextView content;
    }
    
}

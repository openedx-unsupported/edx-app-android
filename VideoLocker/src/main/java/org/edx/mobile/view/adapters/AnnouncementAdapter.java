package org.edx.mobile.view.adapters;

import org.edx.mobile.http.OutboundUrlSpan;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.model.api.AnnouncementsModel;
public abstract class AnnouncementAdapter extends
        BaseListAdapter<AnnouncementsModel> {

    public AnnouncementAdapter(Context context) {
        super(context, R.layout.row_announcement_list);
    }

    @Override
    public void render(BaseViewHolder tag, final AnnouncementsModel model) {
        ViewHolder holder = (ViewHolder) tag;
        holder.txtDate.setText(model.getDate());
        Spanned text = Html.fromHtml(model.content);

        Spanned intercepted = OutboundUrlSpan.interceptAllLinks(text);
        holder.txtContent.setText(intercepted);
        holder.txtContent.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.txtDate = (TextView) convertView
                .findViewById(R.id.announcement_date);
        
        holder.txtContent = (TextView) convertView
                .findViewById(R.id.announcement_content);
        return holder;
    }

    private static class ViewHolder extends BaseViewHolder {
        TextView txtDate;
        TextView txtContent;
    }
    
}

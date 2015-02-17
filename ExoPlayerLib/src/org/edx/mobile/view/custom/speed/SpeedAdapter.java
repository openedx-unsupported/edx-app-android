package org.edx.mobile.view.custom.speed;

import org.edx.mobile.view.custom.BaseListAdapter;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.android.exoplayer.R;

public abstract class SpeedAdapter extends BaseListAdapter<Float> {

    public SpeedAdapter(Context context) {
        super(context);
    }

    @Override
    public void render(BaseViewHolder tag, Float speed) {
        ViewHolder holder = (ViewHolder) tag;
        holder.tvSpeed.setText(String.format("%.1f x", speed));
        if (isSelected(holder.position)) {
            holder.tvSpeed.setBackgroundResource(R.color.speed_selected);
        } else {
            holder.tvSpeed.setBackgroundResource(R.color.list_selector);
        }
        
        if ((getCount()-1) == holder.position) {
            // this is last item in the list, so bottom corners must be rounded
            if (isSelected(holder.position)) {
                holder.tvSpeed.setBackgroundResource(R.drawable.selector_rounded_bottom_lightblue_blue);
            } else {
                holder.tvSpeed.setBackgroundResource(R.drawable.selector_rounded_bottom_white_gray);
            }
        }
    }
    
    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.tvSpeed = (TextView) convertView.findViewById(R.id.row_speed);
        return holder;
    }

    @Override
    public int getListItemLayoutResId() {
        return R.layout.row_speed_list;
    }

    private static class ViewHolder extends BaseViewHolder {
        TextView tvSpeed;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
            long arg3) {
        onItemClicked(getItem(position));
    }

    public abstract void onItemClicked(Float lang);
}

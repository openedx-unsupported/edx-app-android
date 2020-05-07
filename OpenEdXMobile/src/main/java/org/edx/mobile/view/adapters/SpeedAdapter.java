package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;

import java.util.Locale;


public abstract class SpeedAdapter extends BaseListAdapter<Float> {

    private final float selectedPlaybackSpeed;

    public SpeedAdapter(Context context, IEdxEnvironment environment, float selectedPlaybackSpeed) {
        super(context, R.layout.row_dialog_list, environment);
        this.selectedPlaybackSpeed = selectedPlaybackSpeed;
    }

    @Override
    public void render(BaseViewHolder tag, Float speed) {
        ViewHolder holder = (ViewHolder) tag;
        holder.tvSpeed.setText(String.format(Locale.getDefault(), "%.2f x", speed));
        if (speed == selectedPlaybackSpeed) {
            holder.tvSpeed.setBackgroundResource(R.color.cyan_text_navigation_20);
        } else {
            holder.tvSpeed.setBackgroundResource(R.drawable.list_item_overlay_selector);
        }

        if ((getCount() - 1) == holder.position) {
            // this is last item in the list, so bottom corners must be rounded
            if (speed == selectedPlaybackSpeed) {
                holder.tvSpeed.setBackgroundResource(R.color.cyan_text_navigation_20);
            } else {
                holder.tvSpeed.setBackgroundResource(R.drawable.white_bottom_rounded_selector);
            }
        }
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.tvSpeed = (TextView) convertView.findViewById(R.id.row_list);
        return holder;
    }

    private static class ViewHolder extends BaseViewHolder {
        TextView tvSpeed;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                            long arg3) {
        onItemClicked(getItem(position));
    }

    public abstract void onItemClicked(Float speed);
}

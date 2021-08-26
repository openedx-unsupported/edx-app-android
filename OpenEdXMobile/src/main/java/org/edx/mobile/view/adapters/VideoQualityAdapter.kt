package org.edx.mobile.view.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import org.edx.mobile.R
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.model.video.VideoQuality
import org.edx.mobile.util.TextUtils

abstract class VideoQualityAdapter(
    context: Context?,
    environment: IEdxEnvironment?,
    var selectedVideoQuality: VideoQuality
) :
    BaseListAdapter<VideoQuality>(
        context,
        R.layout.video_quality_row_item,
        environment
    ) {

    override fun render(tag: BaseViewHolder?, model: VideoQuality) {
        val holder = tag as ViewHolder
        holder.tvVideoQualityRowTitle.text = context.getString(model.titleResId)
        val typeface = holder.tvVideoQualityRowTitle.typeface
        if (selectedVideoQuality == model) {
            holder.tvVideoQualityRowTitle.setTypeface(typeface, Typeface.BOLD)
            holder.ivVideoQualityCheck.visibility = View.VISIBLE
        } else {
            TextUtils.setTextAppearance(
                context,
                holder.tvVideoQualityRowTitle,
                R.style.regular_primary_dark_color
            )
            holder.ivVideoQualityCheck.visibility = View.GONE
        }
    }

    override fun getTag(convertView: View): BaseViewHolder {
        val holder = ViewHolder()
        holder.tvVideoQualityRowTitle = convertView.findViewById(R.id.tv_video_quality_row_title)
        holder.ivVideoQualityCheck = convertView.findViewById(R.id.iv_video_quality_check)
        return holder
    }

    private class ViewHolder : BaseViewHolder() {
        lateinit var tvVideoQualityRowTitle: TextView
        lateinit var ivVideoQualityCheck: ImageView
    }

    override fun onItemClick(
        adapter: AdapterView<*>?, view: View?, position: Int,
        id: Long
    ) {
        onItemClicked(getItem(position))
    }

    abstract fun onItemClicked(videoQuality: VideoQuality)
}

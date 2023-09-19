package org.edx.mobile.view.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.edx.mobile.R
import org.edx.mobile.databinding.RowTranscriptItemBinding
import org.edx.mobile.interfaces.OnItemClickListener
import org.edx.mobile.util.TextUtils
import subtitleFile.Caption

class TranscriptAdapter(
    context: Context,
    private val listener: OnItemClickListener<Caption>,
) : ListAdapter<Caption, TranscriptAdapter.CaptionViewHolder>(CaptionComparator) {

    @ColorInt
    private val selectedTranscriptColor =
        ContextCompat.getColor(context, R.color.neutralBlack)

    @ColorInt
    private val unselectedTranscriptColor =
        ContextCompat.getColor(context, R.color.primaryBaseColor)

    private var selectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaptionViewHolder {
        return CaptionViewHolder(
            RowTranscriptItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CaptionViewHolder, position: Int) {
        getItem(position)?.apply {
            var captionText = content
            if (captionText.endsWith("<br />")) {
                captionText = captionText.substring(0, captionText.length - 6)
            }
            holder.binding.transcriptItem.text = captionText?.let { TextUtils.formatHtml(it) }
            if (position == selectedPosition) {
                holder.binding.transcriptItem.setTextColor(selectedTranscriptColor)
                holder.binding.transcriptItem.typeface = Typeface.DEFAULT_BOLD
            } else {
                holder.binding.transcriptItem.setTextColor(unselectedTranscriptColor)
                holder.binding.transcriptItem.typeface = Typeface.DEFAULT
            }
            holder.binding.root.setOnClickListener {
                listener.onItemClick(this)
                clearSelection()
                select(position)
            }
        }
    }

    private fun clearSelection() {
        select(-1)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun select(position: Int) {
        selectedPosition = position
        if (selectedPosition >= 1)
            notifyItemRangeChanged(position - 1, 2)
        else
            notifyDataSetChanged()
    }

    fun isSelected(position: Int): Boolean {
        return selectedPosition == position
    }

    class CaptionViewHolder(val binding: RowTranscriptItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    object CaptionComparator : DiffUtil.ItemCallback<Caption>() {
        /**
         * To check whether two objects represent the same item
         */
        override fun areItemsTheSame(
            oldItem: Caption,
            newItem: Caption,
        ): Boolean {
            return oldItem == newItem
        }

        /**
         * To check whether two items have the same data. With a RecyclerView.Adapter, we should return
         * whether the items' visual representations are the same.
         */
        override fun areContentsTheSame(
            oldItem: Caption,
            newItem: Caption,
        ): Boolean {
            return oldItem.rawContent == newItem.rawContent
        }
    }
}

package org.edx.mobile.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.edx.mobile.R
import org.edx.mobile.databinding.RowDiscussionTopicBinding
import org.edx.mobile.interfaces.OnItemClickListener
import org.edx.mobile.model.discussion.DiscussionTopicDepth
import org.edx.mobile.util.UiUtils

class DiscussionTopicsAdapter(
    private val listener: OnItemClickListener<DiscussionTopicDepth>
) : ListAdapter<DiscussionTopicDepth, DiscussionTopicsAdapter.DiscussionTopicViewHolder>(
    DiscussionTopicDepth.DiscussionTopicsComparator()
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DiscussionTopicViewHolder {
        return DiscussionTopicViewHolder(
            RowDiscussionTopicBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(
        holder: DiscussionTopicViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.binding.discussionTopicNameTextView.apply {
            text = item.discussionTopic.getTopicTitle(resources)

            if (getItemViewType(position) == VIEW_TYPE_HEADER) {
                UiUtils.setTextViewDrawableStart(
                    context, this, R.drawable.ic_star_rate,
                    R.dimen.edx_base, R.color.primaryBaseColor
                )
            }

            // Add padding based on thread's depth
            val padding = context.resources.getDimensionPixelOffset(R.dimen.edx_margin)
            ViewCompat.setPaddingRelative(
                this, padding * (1 + item.depth), padding, padding, padding
            )
            setOnClickListener { listener.onItemClick(item) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 1) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }

    class DiscussionTopicViewHolder(val binding: RowDiscussionTopicBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }
}

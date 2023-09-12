package org.edx.mobile.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import org.edx.mobile.R
import org.edx.mobile.databinding.RowDiscussionTopicBinding
import org.edx.mobile.interfaces.OnItemClickListener
import org.edx.mobile.model.discussion.DiscussionTopicDepth
import org.edx.mobile.util.UiUtils.setTextViewDrawableStart

class DiscussionTopicsAdapter(
    private val items: List<DiscussionTopicDepth>,
    private val listener: OnItemClickListener<DiscussionTopicDepth>
) :
    RecyclerView.Adapter<DiscussionTopicsAdapter.DiscussionTopicViewHolder>() {
    private var childPadding: Int = 0
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DiscussionTopicViewHolder {
        val binding =
            RowDiscussionTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        childPadding = parent.context.resources.getDimensionPixelOffset(R.dimen.edx_margin);
        return DiscussionTopicViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: DiscussionTopicViewHolder,
        position: Int
    ) {
        val item = items[position]
        holder.binding.apply {
            if (getItemViewType(position) == VIEW_TYPE_HEADER) {
                setTextViewDrawableStart(
                    root.context, discussionTopicNameTextView, R.drawable.ic_star_rate,
                    R.dimen.edx_base, R.color.primaryBaseColor
                )
            }
            discussionTopicNameTextView.text = item.discussionTopic.getTopicTitle(root.resources)
            this.root.setOnClickListener { listener.onItemClick(item) }
            ViewCompat.setPaddingRelative(
                discussionTopicNameTextView,
                childPadding * (1 + item.depth),
                childPadding,
                childPadding,
                childPadding
            )
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return if (position == 1) VIEW_TYPE_HEADER else 1
    }

    class DiscussionTopicViewHolder(val binding: RowDiscussionTopicBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val VIEW_TYPE_HEADER = 0
    }
}

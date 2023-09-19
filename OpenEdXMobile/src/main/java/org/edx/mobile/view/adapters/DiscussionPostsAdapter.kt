package org.edx.mobile.view.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import org.edx.mobile.R
import org.edx.mobile.databinding.ListViewFooterProgressBinding
import org.edx.mobile.databinding.RowDiscussionThreadBinding
import org.edx.mobile.discussion.DiscussionTextUtils
import org.edx.mobile.extenstion.setCustomTextAppearance
import org.edx.mobile.extenstion.setInVisible
import org.edx.mobile.extenstion.setSrcColor
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.interfaces.OnItemClickListener
import org.edx.mobile.model.discussion.DiscussionThread
import org.edx.mobile.model.discussion.formattedCount
import org.edx.mobile.util.ResourceUtil
import org.edx.mobile.util.UiUtils.getDrawable

class DiscussionPostsAdapter(
    private val listener: OnItemClickListener<DiscussionThread>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    InfiniteScrollUtils.ListContentController<DiscussionThread> {

    private val items: ArrayList<DiscussionThread> = arrayListOf()

    // Record the current time at initialization to keep the display of the elapsed time durations stable.
    private val initialTimeStampMs = System.currentTimeMillis()
    private var progressVisible = false
    private var selectedItemPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            RowType.PROGRESS -> {
                val binding = ListViewFooterProgressBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                object : RecyclerView.ViewHolder(binding.root) {}
            }

            else -> {
                val binding =
                    RowDiscussionThreadBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                DiscussionPostViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == RowType.PROGRESS) return
        val itemHolder = holder as DiscussionPostViewHolder
        val discussionThread = items[position]
        itemHolder.binding.apply {
            bindView(this, discussionThread)
            root.isSelected = position == selectedItemPosition
            root.setOnClickListener {
                listener.onItemClick(discussionThread)
                if (!discussionThread.isRead) {
                    // Refresh the row to mark it as read immediately.
                    discussionThread.isRead = true
                    notifyItemChanged(position)
                }
            }
        }
    }

    private fun bindView(binding: RowDiscussionThreadBinding, discussionThread: DiscussionThread) {
        binding.apply {
            setThreadType(this, discussionThread)
            setViewsAppearance(this, discussionThread)
            setIconVisibility(this, discussionThread)
            setTotalReplies(this, discussionThread)
            setLastUpdate(this, discussionThread)
            setUnreadReplies(this, discussionThread)
        }
    }

    private fun setThreadType(
        binding: RowDiscussionThreadBinding,
        discussionThread: DiscussionThread
    ) {
        binding.apply {
            @DrawableRes val iconResId: Int
            @ColorRes val iconColorRes: Int
            if (discussionThread.type === DiscussionThread.ThreadType.QUESTION) {
                if (discussionThread.isHasEndorsed) {
                    iconResId = R.drawable.ic_verified
                    iconColorRes = R.color.successBase
                } else {
                    iconResId = R.drawable.ic_help_center
                    iconColorRes = R.color.secondaryDarkColor
                }
            } else {
                iconResId = R.drawable.ic_chat
                iconColorRes =
                    if (discussionThread.isRead) R.color.neutralXDark else R.color.primaryBaseColor
            }
            discussionPostTypeIcon.setImageDrawable(
                getDrawable(root.context, iconResId, 0, iconColorRes)
            )
        }
    }

    private fun setViewsAppearance(
        binding: RowDiscussionThreadBinding,
        discussionThread: DiscussionThread
    ) {
        binding.apply {
            val threadTitle: CharSequence? = discussionThread.title
            discussionPostTitle.text = threadTitle
            if (!discussionThread.isRead) {
                discussionPostTitle.setCustomTextAppearance(R.style.discussion_title_text)
                discussionPostTitle.typeface =
                    ResourcesCompat.getFont(root.context, R.font.inter_semi_bold)
            } else {
                discussionPostTitle.setCustomTextAppearance(R.style.discussion_responses_read)
                discussionPostRepliesCount.setCustomTextAppearance(R.style.discussion_responses_read)
                discussionPostDate.setCustomTextAppearance(R.style.discussion_responses_read)
                discussionUnreadRepliesText.setCustomTextAppearance(R.style.discussion_responses_read)
                discussionPostTypeIcon.setSrcColor(R.color.neutralXDark)
                discussionPostClosedIcon.setSrcColor(R.color.neutralXDark)
                discussionPostPinIcon.setSrcColor(R.color.neutralXDark)
                discussionPostFollowingIcon.setSrcColor(R.color.neutralXDark)
            }
        }
    }

    private fun setIconVisibility(
        binding: RowDiscussionThreadBinding,
        discussionThread: DiscussionThread
    ) {
        binding.apply {
            discussionPostClosedIcon.setVisibility(discussionThread.isClosed)
            discussionPostPinIcon.setVisibility(discussionThread.isPinned)
            discussionPostFollowingIcon.setVisibility(discussionThread.isFollowing)
        }
    }

    private fun setTotalReplies(
        binding: RowDiscussionThreadBinding,
        discussionThread: DiscussionThread
    ) {
        binding.apply {
            val commentCount = discussionThread.commentCount
            discussionSubtitleFirstPipe.setVisibility(commentCount > 0 && discussionThread.isAnyIconVisible())
            discussionPostRepliesCount.setVisibility(commentCount > 0)
            val totalReplies = ResourceUtil.getFormattedString(
                root.resources, R.string.discussion_post_total_replies,
                "total_replies", discussionThread.commentCount.formattedCount()
            )
            discussionPostRepliesCount.text = totalReplies
        }
    }

    private fun setLastUpdate(
        binding: RowDiscussionThreadBinding,
        discussionThread: DiscussionThread
    ) {
        binding.apply {
            val lastPostDate = DiscussionTextUtils.getRelativeTimeSpanString(
                root.context,
                initialTimeStampMs, discussionThread.updatedAt?.time ?: 0,
                DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_SHOW_YEAR
            )
            discussionSubtitleSecondPipe.setVisibility(discussionThread.isAnyIconVisible() || discussionThread.commentCount != 0)
            discussionPostDate.text = ResourceUtil.getFormattedString(
                root.resources, R.string.discussion_post_last_interaction_date,
                "date", lastPostDate
            )
        }
    }

    private fun setUnreadReplies(
        binding: RowDiscussionThreadBinding,
        discussionThread: DiscussionThread
    ) {
        val unreadCommentCount = discussionThread.unreadCommentCount
        binding.discussionUnreadRepliesText.setInVisible(unreadCommentCount == 0)
        binding.discussionUnreadRepliesText.text =
            discussionThread.unreadCommentCount.formattedCount()
    }

    override fun getItemCount(): Int {
        return if (progressVisible) items.size + 1 else items.size
    }

    fun getItem(position: Int): DiscussionThread = items[position]

    override fun clear() {
        val itemCount: Int = itemCount
        items.clear()
        notifyItemRangeRemoved(0, itemCount)
    }

    override fun setProgressVisible(visible: Boolean) {
        if (progressVisible != visible) {
            progressVisible = visible
            val progressRowIndex: Int = itemCount
            if (visible) {
                notifyItemInserted(progressRowIndex)
            } else {
                notifyItemRemoved(progressRowIndex)
            }
        }
    }

    override fun addAll(items: MutableList<DiscussionThread>) {
        val lastItemsCount = itemCount
        this.items.addAll(items)
        notifyItemRangeInserted(lastItemsCount, itemCount)
    }

    fun insert(position: Int, item: DiscussionThread) {
        items.add(position, item)
        notifyItemRangeChanged(0, position)
    }

    fun selectedItem(position: Int) {
        if (selectedItemPosition != position) {
            val previousSelected = selectedItemPosition
            selectedItemPosition = position
            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedItemPosition)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (progressVisible && position == itemCount - 1) RowType.PROGRESS
        else RowType.ITEM
    }

    class DiscussionPostViewHolder(val binding: RowDiscussionThreadBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        internal object RowType {
            const val ITEM = 0
            const val PROGRESS = 1
        }
    }
}

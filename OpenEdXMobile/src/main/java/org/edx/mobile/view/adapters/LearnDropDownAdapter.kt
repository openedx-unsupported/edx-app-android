package org.edx.mobile.view.adapters

import android.content.Context
import android.view.View
import android.widget.AdapterView
import androidx.core.widget.TextViewCompat
import org.edx.mobile.R
import org.edx.mobile.databinding.LearnSelectionItemBinding
import org.edx.mobile.view.LearnFragment.LearnScreenItem

class LearnDropDownAdapter(
    context: Context,
    layoutRes: Int
) : BaseListAdapter<LearnScreenItem>(context, layoutRes, null) {

    override fun render(tag: BaseViewHolder?, item: LearnScreenItem) {
        val holder = tag as LearnItemViewHolder
        holder.binding.tvLearnItem.text = context.getString(item.labelRes)
        TextViewCompat.setTextAppearance(
            holder.binding.tvLearnItem,
            if (isSelected(holder.position))
                R.style.bold_text
            else
                R.style.regular_text
        )
    }

    override fun getTag(convertView: View): BaseViewHolder {
        val binding = LearnSelectionItemBinding.bind(convertView)
        return LearnItemViewHolder(binding)
    }

    override fun onItemClick(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ) {
        // nothing to do cuz adapter attached with ListPopupWindow, and click action redirected to
        // the ListPopupWindow.setOnItemClickListener by default.
    }

    private class LearnItemViewHolder(val binding: LearnSelectionItemBinding) : BaseViewHolder()
}

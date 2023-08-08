package org.edx.mobile.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.edx.mobile.R
import org.edx.mobile.databinding.LayoutUnitDropDownItemBinding
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.model.api.AuthorizationDenialReason
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.model.course.IBlock

class UnitsDropDownAdapter(
    private val courseData: EnrolledCoursesResponse?,
    private val units: MutableList<IBlock>,
    private val listener: OnItemSelect?
) : RecyclerView.Adapter<UnitsDropDownAdapter.UnitDropDownViewHolder>() {

    private var selectedItemPosition: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitDropDownViewHolder {
        val binding = LayoutUnitDropDownItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UnitDropDownViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UnitDropDownViewHolder, position: Int) {
        val unit = units[position]
        holder.binding.tvUnitTitle.text = unit.displayName
        holder.binding.ivUnitStatus.setVisibility(true)
        if (unit.isCompleted) {
            holder.binding.ivUnitStatus.setImageResource(R.drawable.ic_green_check)
        } else if (courseData?.isUpgradeable == true &&
            unit is CourseComponent &&
            unit.authorizationDenialReason == AuthorizationDenialReason.FEATURE_BASED_ENROLLMENTS
        ) {
            holder.binding.ivUnitStatus.setImageResource(R.drawable.ic_white_lock_secondary_base)
        } else {
            holder.binding.ivUnitStatus.setVisibility(false)
        }
        holder.binding.divider.setVisibility(position < itemCount - 1)
        holder.binding.rlContent.background =
            ContextCompat.getDrawable(
                holder.binding.rlContent.context,
                R.drawable.round_view_selector
            )
        holder.binding.rlContent.isSelected = selectedItemPosition == position
        holder.binding.rlContent.setOnClickListener {
            listener?.onUnitSelect(unit)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelection(position: Int) {
        selectedItemPosition = position
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return units.size
    }

    fun getUnitIndex(unit: CourseComponent): Int {
        return units.indexOf(unit)
    }

    class UnitDropDownViewHolder(val binding: LayoutUnitDropDownItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface OnItemSelect {
        fun onUnitSelect(unit: IBlock)
    }
}

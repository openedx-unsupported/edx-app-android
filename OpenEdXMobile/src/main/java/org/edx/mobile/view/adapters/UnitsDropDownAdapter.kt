package org.edx.mobile.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.edx.mobile.R
import org.edx.mobile.databinding.LayoutUnitDropDownItemBinding
import org.edx.mobile.extenstion.setInVisible
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.interfaces.OnItemClickListener
import org.edx.mobile.model.api.AuthorizationDenialReason
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.model.course.IBlock

class UnitsDropDownAdapter(
    private val courseData: EnrolledCoursesResponse?,
    private val units: MutableList<IBlock>,
    private val listener: OnItemClickListener<IBlock>?
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
        holder.binding.ivUnitStatus.setInVisible(unit.isCompleted.not())
        holder.binding.containerLockedUnit.setVisibility(
            courseData?.isUpgradeable == true &&
                    unit is CourseComponent &&
                    unit.authorizationDenialReason == AuthorizationDenialReason.FEATURE_BASED_ENROLLMENTS
        )
        holder.binding.rlContent.isSelected = selectedItemPosition == position
        holder.binding.rlContent.setOnClickListener {
            listener?.onItemClick(unit)
        }
        holder.binding.rlContent.background =
            ContextCompat.getDrawable(
                holder.binding.rlContent.context,
                when (position) {
                    0 -> {
                        R.drawable.top_round_view_selector
                    }

                    itemCount - 1 -> {
                        R.drawable.bottom_round_view_selector
                    }

                    else -> {
                        R.drawable.view_selector
                    }
                }
            )
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
}

package org.edx.mobile.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.edx.mobile.databinding.LayoutUnitDropDownItemBinding
import org.edx.mobile.extenstion.setInVisible
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.interfaces.OnItemClickListener
import org.edx.mobile.model.api.AuthorizationDenialReason
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.model.course.IBlock

class UnitsDropDownAdapter(
    private val isCourseUpgradeable: Boolean,
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
        holder.binding.apply {
            tvUnitTitle.text = unit.displayName
            ivUnitStatus.setInVisible(unit.isCompleted.not())
            containerLockedUnit.setVisibility(
                isCourseUpgradeable && unit is CourseComponent &&
                        unit.authorizationDenialReason == AuthorizationDenialReason.FEATURE_BASED_ENROLLMENTS
            )
            rlContent.isSelected = selectedItemPosition == position
            rlContent.setOnClickListener {
                listener?.onItemClick(unit)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelection(position: Int) {
        selectedItemPosition = position
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = units.size

    fun getUnitIndex(unit: CourseComponent): Int = units.indexOf(unit)

    class UnitDropDownViewHolder(val binding: LayoutUnitDropDownItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}

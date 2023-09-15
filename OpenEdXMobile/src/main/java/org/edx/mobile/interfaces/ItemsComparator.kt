package org.edx.mobile.interfaces

import androidx.recyclerview.widget.DiffUtil

abstract class ItemsComparator<T> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
}

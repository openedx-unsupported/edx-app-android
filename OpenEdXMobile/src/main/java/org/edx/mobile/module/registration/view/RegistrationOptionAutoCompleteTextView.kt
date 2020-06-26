package org.edx.mobile.module.registration.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import org.edx.mobile.R
import org.edx.mobile.module.registration.model.RegistrationOption

class RegistrationOptionAutoCompleteTextView : AppCompatAutoCompleteTextView {
    private var adapter: ArrayAdapter<RegistrationOption>? = null
    var selectedItem: RegistrationOption? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    fun hasValue(value: String?): Boolean {
        value?.let {
            return getAdapterPosition(value) >= 0
        }
        return false
    }

    fun select(value: String?) {
        value?.let {
            val pos = getAdapterPosition(value)
            if (pos >= 0) {
                setSelection(pos)
            }
        }
    }

    val selectedItemValue: String?
        get() {
            var value: String? = null
            selectedItem?.let {
                value = it.value
            }
            return value
        }

    val selectedItemName: String?
        get() {
            var name: String? = null
            selectedItem?.let {
                name = it.name
            }
            return name
        }

    private fun getAdapterPosition(input: String): Int {
        var position = -1
        adapter?.let {
            for (i in 0 until it.count) {
                val item = it.getItem(i)
                if (item != null && input.equals(item.toString(), ignoreCase = true)) {
                    position = i
                    selectedItem = item
                    break
                }
            }
        }
        return position
    }

    fun setItems(options: List<RegistrationOption>) {
        adapter = ArrayAdapter(context, R.layout.registration_selection_item, options)
        setAdapter(adapter)
        threshold = 0
    }
}

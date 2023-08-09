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

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    fun hasValue(value: String?): Boolean {
        value?.let {
            return getAdapterPositionFromValueOrName(value) >= 0
        }
        return false
    }

    fun hasName(name: String?): Boolean {
        name?.let {
            return getAdapterPositionFromValueOrName(name) >= 0
        }
        return false
    }

    fun selectFromValue(value: String?) {
        value?.let {
            val pos = getAdapterPositionFromValueOrName(value)
            if (pos >= 0) {
                val option = getAdapter().getItem(pos) as RegistrationOption
                setText(option.name, false)
            }
        }
    }

    val selectedItemValue: String?
        get() = selectedItem?.value

    val selectedItemName: String?
        get() = selectedItem?.name

    /**
     * Retrieves the adapter position for a given value or name in a selectable adapter.
     *
     * @param input The value or name to search for.
     * @return The adapter position of the matched item, or -1 if no match is found.
     */
    private fun getAdapterPositionFromValueOrName(input: String): Int {
        var position = -1
        adapter?.let {
            for (i in 0 until it.count) {
                val item = it.getItem(i)
                if (input.equals(item?.value, ignoreCase = true)
                    || input.equals(item?.name, ignoreCase = true)
                ) {
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

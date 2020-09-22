package org.edx.mobile.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for ViewModels
 * Ref: https://medium.com/koderlabs/viewmodel-with-viewmodelprovider-factory-the-creator-of-viewmodel-8fabfec1aa4f
 */
class ViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            when (modelClass) {
                CourseDateViewModel::class.java -> CourseDateViewModel() as T
                else -> throw IllegalArgumentException("Class doesn't exist in ViewModelFactory")
            }
}

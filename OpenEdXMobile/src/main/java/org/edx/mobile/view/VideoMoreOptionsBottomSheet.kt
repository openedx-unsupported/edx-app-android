package org.edx.mobile.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.databinding.BottomSheetVideoMoreOptionsBinding
import org.edx.mobile.extenstion.serializable
import org.edx.mobile.viewModel.VideoViewModel

@AndroidEntryPoint
class VideoMoreOptionsBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetVideoMoreOptionsBinding

    private val videoViewModel: VideoViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetVideoMoreOptionsBinding.inflate(inflater, container, false)

        binding.deleteOption.setOnClickListener {
            val selectedItemPosition =
                arguments?.serializable(LIST_ITEM_POSITION) as Pair<Int, Int>?
            videoViewModel.deleteVideosAtPosition(selectedItemPosition ?: DEFAULT_LIST_POSITION)
            dismiss()
        }

        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        videoViewModel.clearChoices()
    }

    companion object {
        private const val LIST_ITEM_POSITION = "list_item_position"
        private val DEFAULT_LIST_POSITION = Pair(-1, -1)

        @JvmStatic
        fun newInstance(position: Pair<Int, Int>): VideoMoreOptionsBottomSheet =
            VideoMoreOptionsBottomSheet().apply {
                arguments = Bundle().apply {
                    putSerializable(LIST_ITEM_POSITION, position)
                }
            }
    }
}

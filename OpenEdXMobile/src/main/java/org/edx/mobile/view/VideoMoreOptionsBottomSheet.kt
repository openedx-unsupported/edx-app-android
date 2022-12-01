package org.edx.mobile.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.databinding.BottomSheetVideoMoreOptionsBinding
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as BottomSheetDialog).behavior.apply {
            // To expand the modal in landscape mode
            maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.deleteOption.setOnClickListener {
            videoViewModel.deleteVideosAtPosition(
                arguments?.getInt(LIST_ITEM_POSITION) ?: DEFAULT_LIST_POSITION
            )
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        videoViewModel.clearChoices(shouldClear = true)
    }

    companion object {
        private const val LIST_ITEM_POSITION = "list_item_position"
        private const val DEFAULT_LIST_POSITION = -1

        @JvmStatic
        fun newInstance(position: Int): VideoMoreOptionsBottomSheet =
            VideoMoreOptionsBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt(LIST_ITEM_POSITION, position)
                }
            }
    }
}

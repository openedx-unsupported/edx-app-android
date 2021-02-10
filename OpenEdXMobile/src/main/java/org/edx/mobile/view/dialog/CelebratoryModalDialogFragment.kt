package org.edx.mobile.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import org.edx.mobile.R
import org.edx.mobile.databinding.DialogCelebratoryModalBinding
import kotlin.math.roundToInt

class CelebratoryModalDialogFragment : DialogFragment() {

    private lateinit var binding: DialogCelebratoryModalBinding
    private lateinit var callback: CelebratoryModelCallback

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DialogCelebratoryModalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Place-holder is necessary otherwise glide will not load the gif properly.
        Glide.with(binding.ivCelebrateClap.context).asGif()
                .load(R.raw.celebrate_claps_anim)
                .placeholder(R.drawable.login_screen_image)
                .into(binding.ivCelebrateClap)

        binding.btnKeepGoing.setOnClickListener {
            dismiss()
            callback.onKeepGoing()
        }

        binding.llCelebratoryShare.setOnClickListener {
            callback.onCelebrationShare(it)
        }
        callback.celebratoryModalViewed()
    }

    override fun onResume() {
        super.onResume()
        // Inspiration: https://stackoverflow.com/a/9308284
        if (resources.getBoolean(R.bool.isTablet)) {
            dialog?.window?.setLayout((resources.displayMetrics.widthPixels * 0.6).roundToInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        } else {
            dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    companion object {
        @kotlin.jvm.JvmField
        val TAG: String? = CelebratoryModalDialogFragment::class.java.name

        @JvmStatic
        fun newInstance(celebratoryModelCallback: CelebratoryModelCallback): CelebratoryModalDialogFragment {
            val dialogFragment = CelebratoryModalDialogFragment()
            dialogFragment.callback = celebratoryModelCallback;
            return dialogFragment
        }
    }

    interface CelebratoryModelCallback {
        fun celebratoryModalViewed()

        fun onKeepGoing()

        fun onCelebrationShare(anchor: View)
    }
}

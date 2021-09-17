package org.edx.mobile.view.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import org.edx.mobile.R
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.VideoQualityDialogFragmentBinding
import org.edx.mobile.model.video.VideoQuality
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.prefs.LoginPrefs
import org.edx.mobile.util.ResourceUtil
import org.edx.mobile.view.adapters.VideoQualityAdapter

class VideoDownloadQualityDialogFragment(
    var environment: IEdxEnvironment,
    var callback: IListDialogCallback
) : DialogFragment() {

    private var loginPref: LoginPrefs? = environment.loginPrefs
    private val videoQualities: ArrayList<VideoQuality> = arrayListOf()
    private lateinit var binding: VideoQualityDialogFragmentBinding

    init {
        videoQualities.addAll(VideoQuality.values())
    }

    companion object {
        @JvmStatic
        val TAG: String = VideoDownloadQualityDialogFragment::class.java.name

        @JvmStatic
        fun getInstance(
            environment: IEdxEnvironment,
            callback: IListDialogCallback
        ): VideoDownloadQualityDialogFragment {
            return VideoDownloadQualityDialogFragment(environment, callback)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        environment.analyticsRegistry.trackScreenView(
            Analytics.Screens.VIDEO_DOWNLOAD_QUALITY, null,
            Analytics.Values.SCREEN_NAVIGATION
        )
        val platformName = resources.getString(R.string.platform_name)
        binding.tvVideoQualityMessage.text = ResourceUtil.getFormattedString(
            resources,
            R.string.video_download_quality_message,
            "platform_name",
            platformName
        )
        var selectedVideoQuality = VideoQuality.AUTO
        loginPref?.videoQuality?.let {
            selectedVideoQuality = it
        }
        val adapter = object : VideoQualityAdapter(context, environment, selectedVideoQuality) {
            override fun onItemClicked(videoQuality: VideoQuality) {
                environment.analyticsRegistry.trackVideoDownloadQualityChanged(
                    videoQuality,
                    environment.loginPrefs.videoQuality
                )
                environment.loginPrefs.videoQuality = videoQuality
                callback.onItemClicked(videoQuality)
                dismiss()
            }
        }
        adapter.setItems(videoQualities)
        binding.videoQualityList.adapter = adapter
        binding.videoQualityList.onItemClickListener = adapter
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding =
            VideoQualityDialogFragmentBinding.inflate(LayoutInflater.from(context), null, false)
        return AlertDialog.Builder(requireContext())
            .setNegativeButton(
                R.string.label_cancel
            ) { dialog, _ -> dialog.dismiss() }.setView(binding.root).create()
    }

    override fun onStart() {
        super.onStart()
        val negativeButton: Button =
            (dialog as AlertDialog).getButton(DialogInterface.BUTTON_NEGATIVE)
        negativeButton.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.primaryBaseColor
            )
        )
        negativeButton.setTypeface(null, Typeface.BOLD)
    }

    interface IListDialogCallback {
        fun onItemClicked(videoQuality: VideoQuality)
    }
}

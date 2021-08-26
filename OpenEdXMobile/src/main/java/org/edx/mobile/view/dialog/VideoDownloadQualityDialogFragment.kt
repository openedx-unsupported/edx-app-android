package org.edx.mobile.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import org.edx.mobile.R
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.VideoQualityDialogFragmentBinding
import org.edx.mobile.model.video.VideoQuality
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

    private fun setupWindow() {
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Dialog)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupWindow()
        binding = VideoQualityDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                callback.onItemClicked(videoQuality)
                dismiss()
            }
        }
        adapter.setItems(videoQualities)
        binding.videoQualityList.adapter = adapter
        binding.videoQualityList.onItemClickListener = adapter

        binding.tvClose.setOnClickListener {
            dialog?.dismiss()
        }
    }

    interface IListDialogCallback {
        fun onItemClicked(videoQuality: VideoQuality)
    }
}

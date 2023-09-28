package org.edx.mobile.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import org.edx.mobile.R
import org.edx.mobile.databinding.SubSectionRowLayoutBinding
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.logger.Logger
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.model.course.HasDownloadEntry
import org.edx.mobile.model.course.SectionRow
import org.edx.mobile.model.course.VideoBlockModel
import org.edx.mobile.model.db.DownloadEntry.DownloadedState
import org.edx.mobile.module.db.IDatabase
import org.edx.mobile.util.DateUtil
import org.edx.mobile.util.VideoUtil

class CourseSubSectionAdapter(
    component: CourseComponent,
    private val dbStore: IDatabase,
    private val itemClickListener: OnItemClickListener,
    private val checkedItemPosition: Int
) : RecyclerView.Adapter<CourseSubSectionAdapter.SubSectionViewHolder>() {

    private val logger = Logger(javaClass.name)
    private val adapterData: ArrayList<SectionRow> = arrayListOf()

    init {
        setupSubSectionData(component)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubSectionViewHolder {
        return SubSectionViewHolder(
            SubSectionRowLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return adapterData.size
    }

    override fun onBindViewHolder(holder: SubSectionViewHolder, position: Int) {
        holder.bind(position, position == checkedItemPosition)
    }

    /**
     * Set the data for adapter to populate the listview.
     *
     * @param component The CourseComponent to extract data from.
     */
    private fun setupSubSectionData(component: CourseComponent) {
        for (block in component.children) {
            val courseComponent = block as CourseComponent
            val row = SectionRow(SectionRow.SUB_SECTION, courseComponent)
            adapterData.add(row)
        }
    }

    inner class SubSectionViewHolder(val binding: SubSectionRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int, isChecked: Boolean) {

            val component = adapterData[position].component
            binding.tvSubSectionTitle.text = component.displayName
            if (component.dueDate?.isNotEmpty() == true) {
                try {
                    binding.tvSubTitle.text = String.format(
                        "%s %s", component.format,
                        DateUtil.getFormattedDueDate(binding.root.context, component.dueDate)
                    )
                    binding.tvSubTitle.setVisibility(true)
                    // Accessibility
                    ViewCompat.setImportantForAccessibility(
                        binding.tvSubTitle,
                        ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO
                    )
                } catch (e: IllegalArgumentException) {
                    logger.error(e)
                }
            } else {
                binding.tvSubTitle.setVisibility(false)
            }
            component.isCompleted.apply {
                binding.ivCompletedSection.setVisibility(this)
                binding.root.setBackgroundColor(
                    if (this) {
                        ContextCompat.getColor(binding.root.context, R.color.successXXLight)
                    } else {
                        ContextCompat.getColor(binding.root.context, R.color.neutralWhiteT)
                    }
                )
            }

            binding.subSectionRowContainer.isActivated = isChecked

            setBulkDownloadView(component)
            binding.root.setOnClickListener {
                itemClickListener.onSubSectionItemClick(binding.root, position)
            }

            binding.root.setOnLongClickListener {
                itemClickListener.onSubSectionLongClick(binding.root, position)
                return@setOnLongClickListener true
            }
        }

        private fun setBulkDownloadView(component: CourseComponent) {
            val totalDownloadableVideos: Int = component.downloadableVideosCount
            if (totalDownloadableVideos == 0) {
                binding.bulkDownloadLayout.bulkDownloadLayout.setVisibility(false)
                return
            }

            //then do the string match to get the record
            val path = component.path
            val chapterId = path.get(1)?.displayName ?: ""
            val sequentialId = path.get(2)?.displayName ?: ""
            val downloadedCount: Int = dbStore.getDownloadedVideosCountForSection(
                component.courseId,
                chapterId, sequentialId, null
            )
            val downloadingVideosCount = dbStore.getDownloadingVideosCountForSection(
                component.courseId,
                chapterId,
                sequentialId,
                null
            )

            binding.bulkDownloadLayout.root.setVisibility(true)
            binding.bulkDownloadLayout.bulkDownload.setVisibility(true)
            binding.bulkDownloadLayout.noOfVideos.apply {
                setVisibility(true)
                text = totalDownloadableVideos.toString()

                if (downloadedCount == totalDownloadableVideos) {
                    setRowStateOnDownload(DownloadedState.DOWNLOADED)
                } else if (downloadingVideosCount + downloadedCount == totalDownloadableVideos) {
                    setRowStateOnDownload(DownloadedState.DOWNLOADING) {
                        itemClickListener.viewDownloadsStatus()
                    }
                } else {
                    val downloadableVideos = component.getVideos(true)
                        .filterIsInstance<VideoBlockModel>()
                        .onEach { videoBlockModel ->
                            videoBlockModel.downloadUrl =
                                VideoUtil.getPreferredVideoUrlForDownloading(videoBlockModel.data)
                        }
                    setRowStateOnDownload(DownloadedState.ONLINE) {
                        itemClickListener.download(downloadableVideos = downloadableVideos)
                    }
                }
            }
        }

        /**
         * Makes various changes to the row based on a video element's download status
         *
         * @param state     current state of video download
         * @param listener  the listener to attach to the video download button
         */
        private fun setRowStateOnDownload(
            state: DownloadedState, listener: View.OnClickListener? = null
        ) {
            binding.bulkDownloadLayout.apply {
                when (state) {
                    DownloadedState.DOWNLOADING -> {
                        loadingIndicator.setVisibility(true)
                        loadingIndicator.tag = DownloadedState.DOWNLOADING
                        loadingIndicator.contentDescription = state.toString()
                        bulkDownload.setVisibility(false)
                    }

                    DownloadedState.DOWNLOADED -> {
                        loadingIndicator.setVisibility(false)
                        bulkDownload.setVisibility(true)
                        bulkDownload.setImageResource(R.drawable.download_done_selector)
                        bulkDownload.tag = R.drawable.ic_download_done
                    }

                    DownloadedState.ONLINE -> {
                        loadingIndicator.setVisibility(false)
                        bulkDownload.setVisibility(true)
                        bulkDownload.setImageResource(R.drawable.ic_download)
                        bulkDownload.tag = R.drawable.ic_download
                    }
                }
                bulkDownload.contentDescription = state.toString()
                bulkDownloadLayout.setOnClickListener(listener)
                if (listener == null) {
                    bulkDownloadLayout.isClickable = false
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onSubSectionItemClick(itemView: LinearLayout, position: Int)

        fun onSubSectionLongClick(itemView: LinearLayout, position: Int)

        fun viewDownloadsStatus()

        fun download(downloadableVideos: List<HasDownloadEntry>)
    }
}

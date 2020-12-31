package org.edx.mobile.event

import android.net.Uri

data class FileSelectionEvent(val files: ArrayList<Uri>?) : BaseEvent()
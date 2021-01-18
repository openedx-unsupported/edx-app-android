package org.edx.mobile.event

import android.net.Uri

data class FileSelectionEvent(val files: Array<Uri>?) : BaseEvent()

package org.edx.mobile.event

import android.net.Uri

class FileShareEvent(val files: Array<Uri>?) : BaseEvent()

package org.edx.mobile.extenstion

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

fun CancellableContinuation<Boolean>.resumeIfActive(value: Boolean) {
    if (isActive && isCompleted.not()) {
        resume(value)
    }
}

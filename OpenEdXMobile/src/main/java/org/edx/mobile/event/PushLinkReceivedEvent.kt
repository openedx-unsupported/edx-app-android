package org.edx.mobile.event

import org.edx.mobile.deeplink.PushLink

class PushLinkReceivedEvent(val pushLink: PushLink) : BaseEvent()

package org.edx.mobile.event

import org.edx.mobile.model.iap.IAPFlowData

class IAPFlowEvent(
    val flowAction: IAPFlowData.IAPAction,
    val iapFlowData: IAPFlowData? = null
) : BaseEvent()

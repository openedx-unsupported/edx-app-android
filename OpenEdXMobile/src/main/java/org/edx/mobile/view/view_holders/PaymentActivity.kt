package org.edx.mobile.view.view_holders

import androidx.fragment.app.Fragment
import org.edx.mobile.base.BaseSingleFragmentActivity

class PaymentActivity : BaseSingleFragmentActivity() {
    override fun getFirstFragment(): Fragment {
        return PaymentFragment();
    }
}
package org.edx.mobile.test;

import android.content.Intent;
import android.net.Uri;

public class UtilTests extends BaseTestCase {
    
    public void testBrowserOpenUrl() throws Exception {
        String url = "https://courses.edx.org/register";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(url));
        getInstrumentation().getTargetContext().startActivity(intent);

        print("finished open URL in browser");
    }

}

package org.humana.mobile.x_block;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;

/**
 * Created by JARVICE on 11-10-2017.
 */

public class pdfManager {
    private Uri outputFileUri;
    public void viewPDF(Context ctx,File filePath)
    {
        /** PDF reader code */
        // File file = new File(Environment.getExternalStorageDirectory() + "/" + "abc.pdf");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            outputFileUri = FileProvider.getUriForFile(ctx,
                    ctx.getApplicationContext().getPackageName() + ".provider",
                    filePath);
        } else {
            outputFileUri = Uri.fromFile(filePath);
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
        intent.setDataAndType(outputFileUri,"application/pdf");

        try
        {
            ctx.startActivity(intent);
        }
        catch (ActivityNotFoundException e)
        {
            Toast.makeText(ctx, "NO Pdf Viewer", Toast.LENGTH_SHORT).show();
        }
    }
}
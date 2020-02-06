package org.humana.mobile.model.download;

import org.humana.mobile.tta.data.local.db.table.Period;

public class PDFDownloadModel {
    private Period period;

    public boolean isDownload() {
        return isDownload;
    }

    private boolean isDownload;

    public PDFDownloadModel(boolean isDownload) {
        this.isDownload = isDownload;
    }
}

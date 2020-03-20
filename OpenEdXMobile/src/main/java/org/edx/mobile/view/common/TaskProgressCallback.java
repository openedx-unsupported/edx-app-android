package org.edx.mobile.view.common;

import androidx.annotation.NonNull;
import android.view.View;

public interface TaskProgressCallback {
    void startProcess();
    void finishProcess();

    class ProgressViewController implements TaskProgressCallback {
        @NonNull
        private final View progressView;

        public ProgressViewController(@NonNull final View progressView) {
            this.progressView = progressView;
        }

        @Override
        public void startProcess() {
            progressView.setVisibility(View.VISIBLE);
        }

        @Override
        public void finishProcess() {
            progressView.setVisibility(View.GONE);
        }
    }
}

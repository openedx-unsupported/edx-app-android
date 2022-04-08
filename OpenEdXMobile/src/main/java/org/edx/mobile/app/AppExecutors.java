package org.edx.mobile.app;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {
    private static final int THREAD_COUNT = 2;
    private Executor diskIo = Executors.newSingleThreadExecutor();
    private Executor netIo = Executors.newFixedThreadPool(THREAD_COUNT);
    private Executor mainThread = new MainThreadExecutor();

    static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);

        }
    }

    public Executor getDiskIo() {
        return diskIo;
    }

    public Executor getNetIo() {
        return netIo;
    }

    public Executor getMainThread() {
        return mainThread;
    }
}

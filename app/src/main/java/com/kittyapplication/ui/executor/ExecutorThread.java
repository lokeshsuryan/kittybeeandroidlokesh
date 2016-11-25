package com.kittyapplication.ui.executor;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by MIT on 10/27/2016.
 */
public class ExecutorThread extends HandlerThread {
    private Handler mWorkerHandler;

    public ExecutorThread() {
        super("ExecutorThread");
    }

    public void postTask(Runnable task) {
        mWorkerHandler.post(task);
    }

    private void prepareHandler() {
        mWorkerHandler = new Handler(getLooper());
    }

    public ExecutorThread startExecutor() {
        start();
        prepareHandler();
        return this;
    }
}

package com.kittyapplication.ui.executor;

import android.os.AsyncTask;

/**
 * Created by MIT on 11/17/2016.
 */

public class BackgroundExecutorThread {
    /**
     * Execute current task
     * @param interactor current task interactor
     * @param listener to post task has been completed
     */
    public void execute(final Interactor interactor,
                        final Interactor.OnExecutionFinishListener listener){
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                interactor.execute();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                listener.onFinish();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}

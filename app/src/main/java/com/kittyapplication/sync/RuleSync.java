package com.kittyapplication.sync;

import android.os.AsyncTask;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 7/10/16.
 */
public class RuleSync {
    private final String mGroupId;

    public RuleSync(final String gId) {
        mGroupId = gId;
    }

    private class GetGroupTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}

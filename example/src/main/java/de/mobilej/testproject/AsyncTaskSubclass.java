package de.mobilej.testproject;

import android.os.AsyncTask;
import android.util.Log;


/**
 * A subclass of AsyncTask
 * <p/>
 * Created by bjoern on 14.05.2016.
 */
public class AsyncTaskSubclass extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... params) {
        Log.d("XXX", "doInBackground");
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.d("XXX", "onPostExecute");
    }
}

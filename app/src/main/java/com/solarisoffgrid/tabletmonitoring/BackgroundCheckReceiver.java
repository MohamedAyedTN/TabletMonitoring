package com.solarisoffgrid.tabletmonitoring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import static android.content.Context.MODE_PRIVATE;

public class BackgroundCheckReceiver extends BroadcastReceiver {
    static FetchTopAppAsync fetchTopAppAsync = null;
    static FetchTopWebSiteAsync fetchTopWebSiteAsync = null;
    static FetchPaygStatus fetchPaygStatus;

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        SharedPreferences mPrefs = arg0.getSharedPreferences(arg0.getResources().getString(R.string.sharedpref_title), MODE_PRIVATE);
        Boolean status = mPrefs.getBoolean(arg0.getResources().getString(R.string.sharedpref_service), false);

        if (!status) {
            fetchTopAppAsync = new FetchTopAppAsync(arg0);
            fetchTopWebSiteAsync = new FetchTopWebSiteAsync(arg0);
            fetchPaygStatus = new FetchPaygStatus(arg0);
            fetchTopAppAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
            //       fetchTopWebSiteAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
            fetchPaygStatus.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        }
    }


}
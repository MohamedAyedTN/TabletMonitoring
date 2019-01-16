package com.solarisoffgrid.tabletmonitoring;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;
import static com.solarisoffgrid.tabletmonitoring.JsonUtils.extractPaygStatus;

public class FetchPaygStatus extends AsyncTask<Void, Void, Boolean> {
    public static final String REQUEST_METHOD = "GET";
    public static final int READ_TIMEOUT = 15000;
    public static final int CONNECTION_TIMEOUT = 15000;
    public static String URL;
    SharedPreferences mPrefs;
    DevicePolicyManager deviceManger;
    ComponentName compName;
    private Context ctx;
    public static AsyncResponse delegate;


    public FetchPaygStatus(Context context) {
        ctx = context;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        mPrefs = ctx.getSharedPreferences(ctx.getResources().getString(R.string.sharedpref_title), MODE_PRIVATE);
        String serial = mPrefs.getString(ctx.getResources().getString(R.string.sharedpref_serial), "");
        URL = ctx.getResources().getString(R.string.url_get_expiration_date_and_password);
        String stringUrl = URL + serial;
        String result;
        String inputLine;
        try {
            URL myUrl = new URL(stringUrl);
            HttpURLConnection connection = (HttpURLConnection)
                    myUrl.openConnection();
            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.connect();
            InputStreamReader streamReader = new
                    InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();
            while ((inputLine = reader.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            reader.close();
            streamReader.close();
            result = stringBuilder.toString();
            extractPaygStatus(result, ctx);
        } catch (IOException e) {
            e.printStackTrace();
            result = null;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean param) {
        deviceManger = (DevicePolicyManager) ctx.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        compName = new ComponentName(ctx, MyAdminReceiver.class);
        String shared_expiration_date = mPrefs.getString(ctx.getResources().getString(R.string.sharedpref_expiration_date), "");
        try {
            Date expiration_date = new SimpleDateFormat(ctx.getResources().getString(R.string.date_format)).parse(shared_expiration_date.substring(0, 10));
            Date current_date = Calendar.getInstance().getTime();
            int expiration_status = expiration_date.compareTo(current_date);
            if (expiration_status < 0) {
                String password = mPrefs.getString(ctx.getResources().getString(R.string.sharedpref_password), "");
                boolean active = deviceManger.isAdminActive(compName);
                if (active) {
                    try {
                        deviceManger.resetPassword(password + "", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
                        deviceManger.lockNow();
                    } catch (NullPointerException e) {
                        Log.e("NullPE_deviceManger", e.toString());
                    }
                }
            } else if (expiration_status > 0) {

            } else {

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (delegate != null) {
            delegate.checkstatusfinish();
        }
    }

}

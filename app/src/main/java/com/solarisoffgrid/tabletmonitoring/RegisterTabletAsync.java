package com.solarisoffgrid.tabletmonitoring;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;

import static android.content.Context.MODE_PRIVATE;

public class RegisterTabletAsync extends AsyncTask<Void, Boolean, Boolean> {
    private Context ctx;
    private String phone;


    public RegisterTabletAsync(Context context, String phone) {
        ctx = context;
        this.phone = phone;
    }


    @Override
    protected Boolean doInBackground(Void... voids) {
        SharedPreferences mPrefs = ctx.getSharedPreferences(ctx.getResources().getString(R.string.sharedpref_title), MODE_PRIVATE);

        Connection c = null;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        StringBuilder sb = new StringBuilder();
        String http = ctx.getResources().getString(R.string.url_register_tablet);
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(http);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.connect();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("tablet_id", mPrefs.getString(ctx.getResources().getString(R.string.sharedpref_serial), ""));
            jsonObject.put("client_phone_number", phone);
            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            wr.write(jsonObject.toString());
            wr.flush();
            int HttpResult = urlConnection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                Log.i("servicebg", "2/" + sb.toString());

            } else {
                return false;
            }
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString(ctx.getResources().getString(R.string.sharedpref_phone), phone);
            editor.putBoolean(ctx.getResources().getString(R.string.sharedpref_payg_status), true);
            editor.commit();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean bool) {
        if (bool) {
            Intent i = new Intent(ctx.getApplicationContext(), PaymentCheckActivity.class);
            ctx.startActivity(i);
        } else {
            Toast.makeText(ctx, R.string.no_connection_toast, Toast.LENGTH_LONG).show();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}

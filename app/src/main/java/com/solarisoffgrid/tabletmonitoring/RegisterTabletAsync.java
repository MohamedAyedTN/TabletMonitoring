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
import static com.solarisoffgrid.tabletmonitoring.JsonUtils.extractPaygStatusatRegistration;

public class RegisterTabletAsync extends AsyncTask<Void, Integer, Integer> {
    private Context ctx;
    private String phone;
    StringBuilder sb;
    SharedPreferences mPrefs;

    public RegisterTabletAsync(Context context, String phone) {
        ctx = context;
        this.phone = phone;
    }


    @Override
    protected Integer doInBackground(Void... voids) {
        int HttpResult = 0;
        mPrefs = ctx.getSharedPreferences(ctx.getResources().getString(R.string.sharedpref_title), MODE_PRIVATE);
        Connection c = null;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        sb = new StringBuilder();
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
            jsonObject.put("serial_number", mPrefs.getString(ctx.getResources().getString(R.string.sharedpref_serial), ""));
            jsonObject.put("phone_number", phone);
            Log.i("register", "0/" + jsonObject.toString());

            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            wr.write(jsonObject.toString());
            wr.flush();
            HttpResult = urlConnection.getResponseCode();
            Log.i("register", "0/" + HttpResult);
            if (HttpResult == 201) {

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                Log.i("register", "200/" + sb.toString());
                String token = urlConnection.getHeaderField("token");
                Log.i("register", "token/" + token);

                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString(ctx.getResources().getString(R.string.sharedpref_access_token), token);
                editor.commit();
                extractPaygStatusatRegistration(sb.toString(), ctx);
                //   JsonUtils.extractAccessToken(sb.toString(),ctx);

            } else if (HttpResult == 400) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        urlConnection.getErrorStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                Log.i("register", "400/" + sb.toString());
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        urlConnection.getErrorStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                Log.i("register", HttpResult + "/" + sb.toString());
            }


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

        return HttpResult;
    }

    @Override
    protected void onPostExecute(Integer result) {

        if (result == 201) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString(ctx.getResources().getString(R.string.sharedpref_phone), phone);
            editor.putBoolean(ctx.getResources().getString(R.string.sharedpref_payg_status), true);
            editor.commit();
            Intent i = new Intent(ctx.getApplicationContext(), PaymentCheckActivity.class);
            ctx.startActivity(i);
        } else {
            String serial_toast = "";
            try {
                JSONObject obj = new JSONObject(sb.toString());
                serial_toast = obj.getString("serial_number");
                Toast.makeText(ctx, obj.getString("client_id"), Toast.LENGTH_LONG).show();

            } catch (Throwable t) {
                Log.e("register", "Could not parse malformed JSON: \"" + sb.toString() + "\"");
                Toast.makeText(ctx, serial_toast, Toast.LENGTH_LONG).show();

            }
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

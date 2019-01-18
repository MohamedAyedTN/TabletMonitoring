package com.solarisoffgrid.tabletmonitoring;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;
import static com.solarisoffgrid.tabletmonitoring.BackgroundCheckReceiver.fetchTopAppAsync;

public class SavetoDBAsync extends AsyncTask<Void, Void, Void> {
    private Context ctx;
    private Tablet tablet;
    SharedPreferences mPrefs;

    public SavetoDBAsync(Context context) {
        ctx = context;
        tablet = new Tablet();
        mPrefs = context.getSharedPreferences(ctx.getResources().getString(R.string.sharedpref_title), MODE_PRIVATE);
        tablet.setTablet_serial(mPrefs.getString(ctx.getResources().getString(R.string.sharedpref_serial), ""));
        tablet.setReport_date(Calendar.getInstance().getTime().toString());
        tablet.setClient_phone(mPrefs.getString(ctx.getResources().getString(R.string.sharedpref_phone), ""));
        tablet.setExpirtaion_date(mPrefs.getString(ctx.getResources().getString(R.string.sharedpref_expiration_date), ""));
        tablet.setPayg_status(mPrefs.getBoolean(ctx.getResources().getString(R.string.sharedpref_payg_status), true));
        tablet.setTablet_password(mPrefs.getString(ctx.getResources().getString(R.string.sharedpref_password), ""));
        DAOApp daoApp = new DAOApp(ctx);
        daoApp.openToRead();
        tablet.setTop_app(daoApp.getAllApp());
        daoApp.close();



    }


    @Override
    protected Void doInBackground(Void... errors) {

        if (fetchTopAppAsync.finished) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(ctx.getResources().getString(R.string.sharedpref_service), false);
            editor.commit();
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            JSONObject jsonObject = JsonUtils.toJSon(tablet);
    /*        try {
               HttpPost httpPost = new HttpPost(url);
                StringEntity entity = new StringEntity(jsonObj.toString(), HTTP.UTF_8);
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(httpPost);
                Log.e("servicebg", "insert success");
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Class.forName(ctx.getResources().getString(R.string.postgres_driver));
                    c = DriverManager
                            .getConnection(ctx.getResources().getString(R.string.postgres_address_db),
                                    ctx.getResources().getString(R.string.postgres_user), ctx.getResources().getString(R.string.postgres_password));
                    PreparedStatement statement;
                    statement = c.prepareStatement("update tablet set details = to_json(?::json) where tablet_serial=?");
                    statement.setObject(2, tablet.getTablet_serial());
                    statement.setString(1, jsonObject.toString());
                    statement.executeUpdate();
                    c.close();
                    Log.e("servicebg", "update success");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e("servicebg", "db fail");
                }*/


            StringBuilder sb = new StringBuilder();
            String http = ctx.getResources().getString(R.string.url_save_reports);
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
                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
                wr.write(jsonObject.toString());
                wr.flush();
                int HttpResult = urlConnection.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
               /*     BufferedReader br = new BufferedReader(new InputStreamReader(
                            urlConnection.getInputStream(),"utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    Log.i("servicebg",""+sb.toString());*/

                } else {
                    Log.i("servicebg", urlConnection.getResponseMessage());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }

        } else {
            Log.e("servicebg", "still checking");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        Log.i("servicebg", "done");
    }


}

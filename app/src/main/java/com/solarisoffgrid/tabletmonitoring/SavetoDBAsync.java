package com.solarisoffgrid.tabletmonitoring;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;
import static com.solarisoffgrid.tabletmonitoring.BackgroundCheckReceiver.fetchTopAppAsync;
import static com.solarisoffgrid.tabletmonitoring.BackgroundCheckReceiver.fetchTopWebSiteAsync;

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
        DAOWebsite daoWebsite = new DAOWebsite(ctx);
        daoWebsite.openToRead();
        tablet.setTop_website(daoWebsite.getAllWebSite());
        daoWebsite.close();


    }


    @Override
    protected Void doInBackground(Void... errors) {

        if (fetchTopAppAsync.finished && fetchTopWebSiteAsync.finished) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(ctx.getResources().getString(R.string.sharedpref_service), false);
            editor.commit();
            Connection c = null;
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Log.i("servicebg", Calendar.getInstance().getTime().toString());
            JSONObject jsonObject = JsonUtils.toJSon(tablet);
            try {
                Class.forName(ctx.getResources().getString(R.string.postgres_driver));
                c = DriverManager
                        .getConnection(ctx.getResources().getString(R.string.postgres_address_db),
                                ctx.getResources().getString(R.string.postgres_user), ctx.getResources().getString(R.string.postgres_password));
                PreparedStatement statement;
                statement = c.prepareStatement("insert into tablet (tablet_serial,details) values (?,to_json(?::json)) ");
                statement.setObject(1, tablet.getTablet_serial());
                statement.setString(2, jsonObject.toString());
                statement.executeUpdate();
                c.close();
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
                }
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

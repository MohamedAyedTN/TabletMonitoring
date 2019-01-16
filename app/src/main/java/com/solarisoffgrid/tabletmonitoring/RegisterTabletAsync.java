package com.solarisoffgrid.tabletmonitoring;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import static android.content.Context.MODE_PRIVATE;

public class RegisterTabletAsync extends AsyncTask<Void, Void, String> {
    private Context ctx;
    private String phone;


    public RegisterTabletAsync(Context context, String phone) {
        ctx = context;
        this.phone = phone;
    }


    @Override
    protected String doInBackground(Void... voids) {
        SharedPreferences mPrefs = ctx.getSharedPreferences(ctx.getResources().getString(R.string.sharedpref_title), MODE_PRIVATE);

        Connection c = null;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Class.forName(ctx.getResources().getString(R.string.postgres_driver));
            c = DriverManager
                    .getConnection(ctx.getResources().getString(R.string.postgres_address_db),
                            ctx.getResources().getString(R.string.postgres_user), ctx.getResources().getString(R.string.postgres_password));
            PreparedStatement statement;
            statement = c.prepareStatement("insert into register (tablet_id,client_phone_number) values (?,?) ");
            statement.setObject(1, mPrefs.getString(ctx.getResources().getString(R.string.sharedpref_serial), ""));
            statement.setString(2, phone);
            statement.executeUpdate();
            c.close();
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString(ctx.getResources().getString(R.string.sharedpref_phone), phone);
            editor.putBoolean(ctx.getResources().getString(R.string.sharedpref_payg_status), true);
            editor.commit();
            Log.e("servicebg", "register success");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("servicebg", "already registred");
        }

        return null;
    }
}

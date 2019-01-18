package com.solarisoffgrid.tabletmonitoring;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DAOApp {

    private SQLiteDatabase bdd;
    private SQLiteDB mySQLiteHelper;

    public DAOApp(Context context) {
        mySQLiteHelper = new SQLiteDB(context);
    }

    public void openToWrite() {
        bdd = mySQLiteHelper.getWritableDatabase();
    }

    public void openToRead() {
        bdd = mySQLiteHelper.getWritableDatabase();
    }

    public void close() {
        bdd.close();
    }

    public long insertApp(App app) {
        try {
            ContentValues values = new ContentValues();
            values.put(SQLiteDB.COLUMN_app_name, app.getApp_name());
            values.put(SQLiteDB.COLUMN_app_last_use, app.getLast_use());
            values.put(SQLiteDB.COLUMN_app_used_for, app.getUsed_for());
            values.put(SQLiteDB.COLUMN_app_category, app.getCategory());
            values.put(SQLiteDB.COLUMN_app_data_sent, app.getData_sent());
            values.put(SQLiteDB.COLUMN_app_data_received, app.getData_received());
            return bdd.insert(SQLiteDB.TABLE_app, null, values);
        } catch (SQLiteConstraintException e) {

            return -1;
        }

    }

    public int updateApp(App app) {
        ContentValues values = new ContentValues();
        values.put(SQLiteDB.COLUMN_app_last_use, app.getLast_use());
        values.put(SQLiteDB.COLUMN_app_used_for, app.getUsed_for());
        values.put(SQLiteDB.COLUMN_app_category, app.getCategory());
        values.put(SQLiteDB.COLUMN_app_data_sent, app.getData_sent());
        values.put(SQLiteDB.COLUMN_app_data_received, app.getData_received());
        return bdd.update(SQLiteDB.TABLE_app, values,
                SQLiteDB.COLUMN_app_name + " = \"" + app.getApp_name() + "\"", null);
    }

    public App getAppByName(String app_name) {
        App app = new App();
        Cursor c = bdd.query(SQLiteDB.TABLE_app,
                new String[]{SQLiteDB.COLUMN_app_name,
                        SQLiteDB.COLUMN_app_last_use,
                        SQLiteDB.COLUMN_app_used_for,
                        SQLiteDB.COLUMN_app_category,
                        SQLiteDB.COLUMN_app_data_sent,
                        SQLiteDB.COLUMN_app_data_received},
                SQLiteDB.COLUMN_app_name + " = \"" + app_name + "\"", null, null, null, null);

        if (c.moveToFirst()) {
            app.setApp_name(c.getString(0));
            app.setLast_use((c.getString(1)));
            app.setUsed_for((c.getString(2)));
            app.setCategory((c.getString(3)));
            app.setData_sent((c.getString(4)));
            app.setData_received((c.getString(5)));
        }
        c.close();
        return app;
    }


    public int removeAllApp() {
        return bdd.delete(SQLiteDB.TABLE_app,
                null, null);
    }

    public List<App> getAllApp() {
        List<App> apps = new ArrayList<App>();
        Cursor c = bdd.query(SQLiteDB.TABLE_app,
                new String[]{SQLiteDB.COLUMN_app_name,
                        SQLiteDB.COLUMN_app_last_use,
                        SQLiteDB.COLUMN_app_used_for,
                        SQLiteDB.COLUMN_app_category,
                        SQLiteDB.COLUMN_app_data_sent,
                        SQLiteDB.COLUMN_app_data_received,
                }, null, null, null, null, null);

        if (c.moveToFirst()) {
            do {
                App app = new App();
                app.setApp_name(c.getString(0));
                app.setLast_use((c.getString(1)));
                app.setUsed_for((c.getString(2)));
                app.setCategory((c.getString(3)));
                app.setData_sent((c.getString(4)));
                app.setData_received((c.getString(5)));

                apps.add(app);
            } while (c.moveToNext());
        }
        c.close();


        return apps;
    }
}

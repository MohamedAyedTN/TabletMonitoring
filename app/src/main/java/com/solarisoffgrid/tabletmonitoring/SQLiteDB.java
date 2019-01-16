package com.solarisoffgrid.tabletmonitoring;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteDB extends SQLiteOpenHelper {


    public static final String TABLE_app = "app";
    public static final String COLUMN_app_name = "app_name";
    public static final String COLUMN_app_last_use = "last_use";
    public static final String COLUMN_app_used_for = "used_for";
    public static final String COLUMN_app_category = "category";
    public static final String COLUMN_app_data_sent = "data_sent";
    public static final String COLUMN_app_data_received = "data_received";

    public static final String TABLE_website = "website";
    public static final String COLUMN_website_title = "title";
    public static final String COLUMN_website_url = "url";
    public static final String COLUMN_website_last_visit = "last_visit";
    public static final String COLUMN_website_visits = "visits";
    public static final String COLUMN_website_icon = "icon";

    private static final String DATABASE_NAME = "tablet.db";
    private static final int DATABASE_VERSION = 1;


    private static final String DATABASE_APP = "create table "
            + TABLE_app + "(" + COLUMN_app_name + " text primary key , "
            + COLUMN_app_last_use + " text , "
            + COLUMN_app_used_for + " text , "
            + COLUMN_app_category + " text , "
            + COLUMN_app_data_sent + " text , "
            + COLUMN_app_data_received + " text);";

    private static final String DATABASE_WEBSITE = "create table "
            + TABLE_website + "(" + COLUMN_website_title + " text primary key , "
            + COLUMN_website_url + " text not null, "
            + COLUMN_website_last_visit + " text not null, "
            + COLUMN_website_visits + " text not null, "
            + COLUMN_website_icon + " TEXT not null);";

    public SQLiteDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_APP);
        database.execSQL(DATABASE_WEBSITE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int
            newVersion) {
        Log.d(SQLiteDB.class.getName(), "upgrading  " + oldVersion + " to " + newVersion);
        onCreate(db);
    }

}


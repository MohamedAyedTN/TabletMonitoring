package com.solarisoffgrid.tabletmonitoring;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DAOWebsite {

    private SQLiteDatabase bdd;
    private SQLiteDB mySQLiteHelper;

    public DAOWebsite(Context context) {
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

    public long insertWebSite(WebSite webSite) {
        try {
            ContentValues values = new ContentValues();
            values.put(SQLiteDB.COLUMN_website_title, webSite.getWebsite_title());
            values.put(SQLiteDB.COLUMN_website_url, webSite.getWebsite_url());
            values.put(SQLiteDB.COLUMN_website_last_visit, webSite.getLast_visit());
            values.put(SQLiteDB.COLUMN_website_visits, webSite.getVisits());
            values.put(SQLiteDB.COLUMN_website_icon, webSite.getIcon());
            return bdd.insert(SQLiteDB.TABLE_website, null, values);
        } catch (SQLiteConstraintException e) {
            return 0;
        }
    }

    public int removeAllWebSite() {
        return bdd.delete(SQLiteDB.TABLE_website,
                null, null);
    }

    public List<WebSite> getAllWebSite() {
        List<WebSite> webSites = new ArrayList<WebSite>();
        Cursor c = bdd.query(SQLiteDB.TABLE_website,
                new String[]{SQLiteDB.COLUMN_website_title,
                        SQLiteDB.COLUMN_website_url,
                        SQLiteDB.COLUMN_website_last_visit,
                        SQLiteDB.COLUMN_website_visits,
                        SQLiteDB.COLUMN_website_icon,
                }, null, null, null, null, null);

        if (c.moveToFirst()) {
            do {
                WebSite webSite = new WebSite();
                webSite.setWebsite_title(c.getString(0));
                webSite.setWebsite_url((c.getString(1)));
                webSite.setLast_visit((c.getString(2)));
                webSite.setVisits((c.getInt(3)));
                webSite.setIcon((c.getBlob(4)));
                webSites.add(webSite);
            } while (c.moveToNext());
        }
        c.close();


        return webSites;
    }
}

package com.solarisoffgrid.tabletmonitoring;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class JsonUtils {

    public static JSONObject toJSon(Tablet tablet) {
        try {
            // Here we convert Java Object to JSON
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("tablet_serial", tablet.getTablet_serial());
            jsonObj.put("report_date", tablet.getReport_date());
            jsonObj.put("client_phone", tablet.getClient_phone());
            jsonObj.put("paygstatus", tablet.getPayg_status());
            jsonObj.put("expiration_date", tablet.getExpirtaion_date());
            jsonObj.put("tablet_password", tablet.getTablet_password());

            JSONArray jsonArrApp = new JSONArray();
            try {
                for (App pn : tablet.getTop_app()) {
                    JSONObject tpObj = new JSONObject();
                    tpObj.put("app_name", pn.getApp_name());
                    tpObj.put("last_use", pn.getLast_use());
                    tpObj.put("used_for", pn.getUsed_for());
                    tpObj.put("category", pn.getCategory());
                    tpObj.put("data_sent", pn.getData_sent());
                    tpObj.put("data_received", pn.getData_received());
                    jsonArrApp.put(tpObj);
                }
                jsonObj.put("top_app", jsonArrApp);
            } catch (NullPointerException e) {
                for (App pn : FetchTopAppAsync.apps) {
                    JSONObject tpObj = new JSONObject();
                    tpObj.put("app_name", pn.getApp_name());
                    tpObj.put("last_use", pn.getLast_use());
                    tpObj.put("used_for", pn.getUsed_for());
                    tpObj.put("category", pn.getCategory());
                    tpObj.put("data_sent", pn.getData_sent());
                    tpObj.put("data_received", pn.getData_received());
                    jsonArrApp.put(tpObj);
                }
                jsonObj.put("top_app", jsonArrApp);
            }

    /*        JSONArray jsonArrWeb = new JSONArray();
            try {
                for (WebSite pn : tablet.getTop_website()) {
                    JSONObject tpObj = new JSONObject();
                    tpObj.put("url_title", pn.getWebsite_title());
                    tpObj.put("url", pn.getWebsite_url());
                    tpObj.put("last_visit", pn.getLast_visit());
                    tpObj.put("visits", pn.getVisits());
                    jsonArrWeb.put(tpObj);
                }
                jsonObj.put("top_website", jsonArrWeb);
            } catch (NullPointerException e) {
                for (WebSite pn : FetchTopWebSiteAsync.webSites) {
                    JSONObject tpObj = new JSONObject();
                    tpObj.put("url_title", pn.getWebsite_title());
                    tpObj.put("url", pn.getWebsite_url());
                    tpObj.put("last_visit", pn.getLast_visit());
                    tpObj.put("visits", pn.getVisits());
                    jsonArrWeb.put(tpObj);
                }
                jsonObj.put("top_website", jsonArrWeb);
            }*/
            return jsonObj;
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void extractPaygStatus(String response, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.sharedpref_title), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        try {
            JSONObject mainObject = new JSONObject(response);
            String expiration_time = mainObject.getString("expiration_time");
            String password = mainObject.getString("Password");
            Log.i("servicebg", "password " + password);
            Log.i("servicebg", "expiration_time " + expiration_time);
            editor.putString(context.getResources().getString(R.string.sharedpref_expiration_date), expiration_time);
            editor.putString(context.getResources().getString(R.string.sharedpref_password), password);
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
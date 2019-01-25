package com.solarisoffgrid.tabletmonitoring;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.TELEPHONY_SERVICE;

public class FetchTopAppAsync extends AsyncTask<Void, Void, List<App>> {

    public final static String GOOGLE_URL = "https://play.google.com/store/apps/details?id=";
    public static final String ERROR = "error";
    public static List<App> apps;
    public static List<Drawable> drawables;
    public AsyncResponse delegate;
    public boolean finished = false;
    public boolean usage_permisson = false;
    int topAppCount = 0;
    private UsageStatsManager mUsageStatsManager;
    private UsageTimeComparator mUsageTimeComparator = new UsageTimeComparator();
    private Context ctx;

    public FetchTopAppAsync(Context context) {
        ctx = context;
        SharedPreferences mPrefs = context.getSharedPreferences(ctx.getResources().getString(R.string.sharedpref_title), MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(ctx.getResources().getString(R.string.sharedpref_service), true);
        editor.commit();
    }

    @Override
    protected List<App> doInBackground(Void... errors) {
        drawables = new ArrayList<>();
        apps = new ArrayList<>();

        List<String> packages_to_ignore = Arrays.asList(ctx.getResources().getStringArray(R.array.packages_to_ignore));
        String category;
        int result = ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE);
        mUsageStatsManager = (UsageStatsManager) ctx.getSystemService(Context.USAGE_STATS_SERVICE);
        usage_permisson = checkForPermission(ctx);
        PackageManager pm = ctx.getPackageManager();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -ctx.getResources().getInteger(R.integer.app_used_interval_in_months));
        if (result == PackageManager.PERMISSION_GRANTED) {
            DAOApp daoApp = new DAOApp(ctx);
            daoApp.openToWrite();
            daoApp.removeAllApp();
            daoApp.close();
            final List<UsageStats> stats =
                    mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                            cal.getTimeInMillis(), Calendar.getInstance().getTimeInMillis());
            if (stats == null) {
                return null;
            }
            Collections.sort(stats, mUsageTimeComparator);
            NetworkStatsManager nsm = (NetworkStatsManager) ctx.getSystemService(Context.NETWORK_STATS_SERVICE);
            for (int i = 0; i < stats.size(); i++) {
                final android.app.usage.UsageStats us = stats.get(i);
                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(us.getPackageName(), 0);
                    String label = appInfo.loadLabel(pm).toString();
                    if (!label.equals(us.getPackageName()) && us.getTotalTimeInForeground() != 0) {
                        String packageName = us.getPackageName();
                        if (packageName.contains("package:")) {
                            packageName = packageName.replace("package:", "");
                        }
                        if (!packages_to_ignore.contains(label)) {
                            App app = new App();
                            topAppCount++;

                            app.setIcon(appInfo.loadIcon(pm));
                            app.setApp_name(label);
                            //  app.setLast_use(DateUtils.formatSameDayTime(us.getLastTimeUsed(),System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM) + "");

                            Date date = new Date(us.getLastTimeUsed());
                            DateFormat formattes = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                            String dateFormatted = formattes.format(date);
                            dateFormatted = dateFormatted.replace(" ", "T").concat("Z");
                            app.setLast_use(dateFormatted);

                            app.setUsed_for(us.getTotalTimeInForeground() / 1000);


                            String query_url = GOOGLE_URL + packageName;
                            category = getCategory(query_url);
                            app.setCategory(category);
                            if (usage_permisson) {
                                int uid = 0;
                                try {
                                    uid = pm.getApplicationInfo(us.getPackageName(), 0).uid;
                                    TelephonyManager tm = (TelephonyManager) ctx.getSystemService(TELEPHONY_SERVICE);
                                    String subscriberID = tm.getSubscriberId();
                                    NetworkStats networkStatsByApp;
                                    long received = 0, send = 0;
                                    networkStatsByApp = nsm.queryDetailsForUid(ConnectivityManager.TYPE_MOBILE, subscriberID, getTodayPlus(Calendar.MONTH, -ctx.getResources().getInteger(R.integer.data_used_interval_in_months)), getTodayPlus(0, 0), uid);
                                    while (networkStatsByApp.hasNextBucket()) {
                                        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                                        networkStatsByApp.getNextBucket(bucket);
                                        received = received + bucket.getRxBytes();
                                        send = send + bucket.getTxBytes();
                                    }
                                    networkStatsByApp.close();
                                    networkStatsByApp = nsm.queryDetailsForUid(ConnectivityManager.TYPE_WIFI, subscriberID, getTodayPlus(Calendar.MONTH, -ctx.getResources().getInteger(R.integer.data_used_interval_in_months)), getTodayPlus(0, 0), uid);
                                    while (networkStatsByApp.hasNextBucket()) {
                                        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                                        networkStatsByApp.getNextBucket(bucket);
                                        received = received + bucket.getRxBytes();
                                        send = send + bucket.getTxBytes();
                                    }
                                    networkStatsByApp.close();
                                    app.setData_received(received);
                                    app.setData_sent(send);
                                    long adding_result;
                                    daoApp.openToWrite();
                                    adding_result = daoApp.insertApp(app);
                                    daoApp.close();
                                    if (adding_result != -1) {
                                        apps.add(app);
                                        drawables.add(appInfo.loadIcon(pm));
                                    } else {
                                        daoApp.openToRead();
                                        App registred_app = daoApp.getAppByName(app.getApp_name());
                                        daoApp.close();
                                        if (registred_app.getLast_use().compareTo(app.getLast_use()) > 0) {
                                            app.setUsed_for(registred_app.getUsed_for() + app.getUsed_for());
                                            daoApp.openToWrite();
                                            daoApp.updateApp(app);
                                            daoApp.close();
                                        }
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.i("servicebg", "permisson not granted for usage ");
                                app.setData_received(0);
                                app.setData_sent(0);
                                long adding_result;
                                daoApp.openToWrite();
                                adding_result = daoApp.insertApp(app);
                                daoApp.close();
                                if (adding_result != -1) {
                                    apps.add(app);
                                    drawables.add(appInfo.loadIcon(pm));
                                }
                            }
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    // This package may be gone.
                }
                if (topAppCount == ctx.getResources().getInteger(R.integer.TOP_APP_NUMBER))
                    return apps;
            }
        }
        return apps;
    }

    @Override
    protected void onPostExecute(List<App> apps) {
        finished = true;
        new SavetoDBAsync(ctx).execute();
        if (delegate != null) {
            delegate.topAppFinish();
        }
    }

    private boolean checkForPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        return mode == MODE_ALLOWED;
    }

    private String getCategory(String query_url) {
         /*   if (!network) {
                //manage connectivity lost
                return ERROR;
            } else {*/
        try {
            Document doc = Jsoup.connect(query_url).userAgent("Mozilla/5.0 (Linux; Android 6.0.1; SM-G920V Build/MMB29K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.98 Mobile Safari/537.36").get();
            Element link = doc.select("a[itemprop=genre]").first();
            return link.text();
        } catch (Exception e) {
            return ERROR;
        }
        //  }
    }


    private long getTodayPlus(int calendarField, int valueToAdd) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (valueToAdd != 0) {
            calendar.add(calendarField, valueToAdd);
        }

        return calendar.getTime().getTime();
    }

    public static class UsageTimeComparator implements Comparator<UsageStats> {
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            return (int) (b.getTotalTimeInForeground() - a.getTotalTimeInForeground());
        }
    }

}

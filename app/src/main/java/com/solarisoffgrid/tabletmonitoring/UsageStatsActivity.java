package com.solarisoffgrid.tabletmonitoring;

import android.Manifest;
import android.app.Activity;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Activity to display package usage statistics.
 */
public class UsageStatsActivity extends Activity implements OnItemSelectedListener {
    private static final String TAG = "UsageStatsActivity";
    public final static String GOOGLE_URL = "https://play.google.com/store/apps/details?id=";
    public static final String ERROR = "error";
    private static final boolean localLOGV = false;
    private UsageStatsManager mUsageStatsManager;
    private LayoutInflater mInflater;
    private UsageStatsAdapter mAdapter;
    private PackageManager mPm;
    private final Context ctx = this;
    private int loop=0;

    private Map<String,String> mPackageCategories = new ArrayMap<>();
    private final ArrayList<UsageStats> mPackageStats = new ArrayList<>();
    private Map<String,String> mPackageRx;
    private Map<String,String> mPackageTx;


    private ListView listView;

    public static class AppNameComparator implements Comparator<UsageStats> {
        private Map<String, String> mAppLabelList;

        AppNameComparator(Map<String, String> appList) {
            mAppLabelList = appList;
        }

        @Override
        public final int compare(UsageStats a, UsageStats b) {
            String alabel = mAppLabelList.get(a.getPackageName());
            String blabel = mAppLabelList.get(b.getPackageName());
            return alabel.compareTo(blabel);
        }
    }

    public static class LastTimeUsedComparator implements Comparator<UsageStats> {
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            // return by descending order
            return (int) (b.getLastTimeUsed() - a.getLastTimeUsed());
        }
    }

    public static class UsageTimeComparator implements Comparator<UsageStats> {
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            return (int) (b.getTotalTimeInForeground() - a.getTotalTimeInForeground());
        }
    }

    // View Holder used when displaying views
    static class AppViewHolder {
        TextView pkgName;
        TextView lastTimeUsed;
        TextView usageTime;
        TextView usageData;
        TextView category;
        ImageView pkgIcon;
    }

    class UsageStatsAdapter extends BaseAdapter {
        // Constants defining order for display order
        private static final int _DISPLAY_ORDER_USAGE_TIME = 0;
        private static final int _DISPLAY_ORDER_LAST_TIME_USED = 1;
        private static final int _DISPLAY_ORDER_APP_NAME = 2;

        private int mDisplayOrder = _DISPLAY_ORDER_USAGE_TIME;
        private LastTimeUsedComparator mLastTimeUsedComparator = new LastTimeUsedComparator();
        private UsageTimeComparator mUsageTimeComparator = new UsageTimeComparator();
        private AppNameComparator mAppLabelComparator;
        private final ArrayMap<String, String> mAppLabelMap = new ArrayMap<>();

        UsageStatsAdapter() {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -5);

            final List<UsageStats> stats =
                    mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                            cal.getTimeInMillis(), System.currentTimeMillis());
            if (stats == null) {
                return;
            }

            ArrayMap<String, UsageStats> map = new ArrayMap<>();
            final int statCount = stats.size();
            for (int i = 0; i < statCount; i++) {
                final android.app.usage.UsageStats pkgStats = stats.get(i);

                // load application labels for each application
                try {
                    ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                    String label = appInfo.loadLabel(mPm).toString();
                    mAppLabelMap.put(pkgStats.getPackageName(), label);

                    UsageStats existingStats = map.get(pkgStats.getPackageName());
                    if (existingStats == null) {
                        if (!label.equals(pkgStats.getPackageName()) && pkgStats.getTotalTimeInForeground() != 0) {
                            map.put(pkgStats.getPackageName(), pkgStats);
                        }
                    } else {
                        existingStats.add(pkgStats);
                    }

                } catch (NameNotFoundException e) {
                    // This package may be gone.
                }
            }
            mPackageStats.addAll(map.values());
            mAppLabelComparator = new AppNameComparator(mAppLabelMap);
            sortList();

        }

        @Override
        public int getCount() {
            return mPackageStats.size() > 10 ? 10 : mPackageStats.size();
        }

        @Override
        public Object getItem(int position) {
            return mPackageStats.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AppViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.usage_stats_item, null);
                holder = new AppViewHolder();
                holder.pkgName = convertView.findViewById(R.id.package_name);
                holder.lastTimeUsed = convertView.findViewById(R.id.last_time_used);
                holder.usageTime = convertView.findViewById(R.id.usage_time);
                holder.pkgIcon = convertView.findViewById(R.id.package_icon);
                holder.usageData = convertView.findViewById(R.id.usage_data);
                holder.category = convertView.findViewById(R.id.category);
                convertView.setTag(holder);
            } else {
                holder = (AppViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder
            UsageStats pkgStats = mPackageStats.get(position);
            PackageManager packageManager = getPackageManager();
            if (pkgStats != null) {
                String label = mAppLabelMap.get(pkgStats.getPackageName());
                holder.pkgName.setText(label);
                holder.lastTimeUsed.setText("Last use: "+DateUtils.formatSameDayTime(pkgStats.getLastTimeUsed(),
                        System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM));
                holder.usageTime.setText("used for: "+
                        DateUtils.formatElapsedTime(pkgStats.getTotalTimeInForeground() / 1000));
                try {
                    holder.category.setText("Category: " + mPackageCategories.get(pkgStats.getPackageName()));
                    holder.usageData.setText("Send :"+mPackageRx.get(pkgStats.getPackageName())+", Received :"+mPackageTx.get(pkgStats.getPackageName()));
                    ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                    holder.pkgIcon.setImageDrawable(scaleImage(appInfo.loadIcon(mPm)));
                } catch (NameNotFoundException e) {
                    // This package may be gone.
                }
            } else {
                Log.w(TAG, "No usage stats info for package:" + position);
            }
            return convertView;
        }

        void sortList(int sortOrder) {
            if (mDisplayOrder == sortOrder) {
                // do nothing
                return;
            }
            mDisplayOrder = sortOrder;
            sortList();
        }

        private void sortList() {
            if (mDisplayOrder == _DISPLAY_ORDER_USAGE_TIME) {
                if (localLOGV) Log.i(TAG, "Sorting by usage time");
                Collections.sort(mPackageStats, mUsageTimeComparator);
            } else if (mDisplayOrder == _DISPLAY_ORDER_LAST_TIME_USED) {
                if (localLOGV) Log.i(TAG, "Sorting by last time used");
                Collections.sort(mPackageStats, mLastTimeUsedComparator);
            } else if (mDisplayOrder == _DISPLAY_ORDER_APP_NAME) {
                if (localLOGV) Log.i(TAG, "Sorting by application name");
                Collections.sort(mPackageStats, mAppLabelComparator);
            }
           // new FetchCategoryTask().execute();
            notifyDataSetChanged();
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_usage_stats);

        mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPm = getPackageManager();

        Spinner typeSpinner = findViewById(R.id.typeSpinner);
        typeSpinner.setOnItemSelectedListener(this);
        Log.i("", "start");
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UsageStatsActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    123);
        }
        mAdapter = new UsageStatsAdapter();
        new FetchCategoryTask().execute();

    }

    public Drawable scaleImage (Drawable image) {

        if ((image == null) || !(image instanceof BitmapDrawable)) {
            return image;
        }
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 50, 50, false);
        image = new BitmapDrawable(getResources(), bitmapResized);
        return image;

    }

    private class FetchCategoryTask extends AsyncTask<Void, Void, Map<String,String>> {

        private PackageManager pm;

        @Override
        protected Map<String,String> doInBackground(Void... errors) {
            String category;
            pm = getPackageManager();
            mPackageCategories = new ArrayMap<>();
            mPackageRx = new ArrayMap<>();
            mPackageTx = new ArrayMap<>();
            int result = ContextCompat.checkSelfPermission(UsageStatsActivity.this, Manifest.permission.READ_PHONE_STATE);
            if (result == PackageManager.PERMISSION_GRANTED) {
                Log.i("data ", "permission granted");
                for (UsageStats us : mPackageStats) {
                    String packageName = us.getPackageName();
                    if (packageName.contains("package:")) {
                        packageName = packageName.replace("package:", "");
                    }
                    String query_url = GOOGLE_URL + packageName;
                    category = getCategory(query_url);
                    mPackageCategories.put(us.getPackageName(),category);
                    int uid = 0;
                    try {
                        uid = pm.getApplicationInfo(us.getPackageName(), 0).uid;
                    } catch (PackageManager.NameNotFoundException e) {

                    }
                    TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    String subscriberID = tm.getSubscriberId();
                    NetworkStatsManager nsm = (NetworkStatsManager) ctx.getSystemService(Context.NETWORK_STATS_SERVICE);
                    NetworkStats networkStatsByApp;
                    try {
                        long received = 0, send = 0;
                        networkStatsByApp = nsm.queryDetailsForUid(ConnectivityManager.TYPE_MOBILE, subscriberID, getTodayPlus(0, 0), getTodayPlus(Calendar.DAY_OF_MONTH, 1), uid);
                        while (networkStatsByApp.hasNextBucket()) {
                            NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                            networkStatsByApp.getNextBucket(bucket);
                            received = received + bucket.getRxBytes();
                            send = send + bucket.getTxBytes();
                        }
                        networkStatsByApp.close();
                        mPackageRx.put(us.getPackageName(),formatSize(send));
                        mPackageTx.put(us.getPackageName(),formatSize(received));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }


                }
            }

            return mPackageCategories;
        }

        @Override
        protected void onPostExecute(Map<String,String> categories) {
    /*        Log.i("data ", loop+"");
            if(loop==0){
            mAdapter = new UsageStatsAdapter();
            listView.setAdapter(mAdapter);
            loop++;
}
            else loop=0;
            */
            listView = findViewById(R.id.pkg_list);
            listView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();

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
    }

    private long getTodayPlus(int calendarField, int valueToAdd) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (valueToAdd > 0) {
            calendar.add(calendarField, valueToAdd);
        }
        return calendar.getTime().getTime();
    }

    private String formatSize(long sizeBytes) {
        return Formatter.formatFileSize(ctx, sizeBytes);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.sortList(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
    }
}


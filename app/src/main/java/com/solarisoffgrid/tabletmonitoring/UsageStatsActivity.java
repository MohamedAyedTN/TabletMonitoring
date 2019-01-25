package com.solarisoffgrid.tabletmonitoring;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Activity to display package usage statistics.
 */
public class UsageStatsActivity extends Activity implements AsyncResponse {


    private final Context ctx = this;
    private LayoutInflater mInflater;
    private UsageStatsAdapter mAdapter;
    private List<App> apps = new ArrayList<>();
    private ListView listView;


    public void showList() {
        DAOApp daoApp = new DAOApp(ctx);
        daoApp.openToRead();
        apps = daoApp.getAllApp();
        daoApp.close();
        if (!apps.isEmpty()) {
            mAdapter = new UsageStatsAdapter(ctx, apps);
            listView.setAdapter(mAdapter);
        }
        Log.i("servicebg", "drawables " + FetchTopAppAsync.drawables.size());
        Log.i("servicebg", "apps " + apps.size());

    }

    @Override
    public void topAppFinish() {
        Log.i("servicebg", "topApp done");
        showList();
    }

    @Override
    public void topWebSitefinish() {

    }

    @Override
    public void checkstatusfinish() {

    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_usage_stats);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        BackgroundCheckReceiver.fetchTopAppAsync.delegate = this;
        Log.i("servicebg", "activity topapp start");
        listView = findViewById(R.id.pkg_list);
        showList();
    }

    public Drawable scaleImage(Drawable image) {
        if (!(image instanceof BitmapDrawable)) {
            return image;
        }
        Bitmap b = ((BitmapDrawable) image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 50, 50, false);
        image = new BitmapDrawable(getResources(), bitmapResized);
        return image;
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
        private List<App> apps = new ArrayList<>();
        private Context context;

        UsageStatsAdapter(Context context, List<App> apps) {
            this.context = context;
            this.apps = apps;
        }

        @Override
        public int getCount() {
            return apps.size() > 10 ? 10 : apps.size();
        }

        @Override
        public Object getItem(int position) {
            return apps.get(position);
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
            App app = apps.get(position);
            holder.pkgName.setText(app.getApp_name());
            holder.lastTimeUsed.setText(ctx.getResources().getString(R.string.app_item_1) + app.getLast_use());
            holder.usageTime.setText(ctx.getResources().getString(R.string.app_item_2) + from_long_to_timer(app.getUsed_for()));
            holder.usageData.setText(ctx.getResources().getString(R.string.app_item_3) + from_byte_to_KB(app.getData_sent())
                    + ctx.getResources().getString(R.string.app_item_4) + from_byte_to_KB(app.getData_received()));
            holder.pkgIcon.setImageDrawable(scaleImage(app.getIcon()));
            holder.category.setText(ctx.getResources().getString(R.string.app_item_5) + app.getCategory());
            holder.pkgIcon.setImageDrawable(scaleImage(FetchTopAppAsync.drawables.get(position)));
            return convertView;
        }
    }

    public String from_long_to_timer(long millis) {
        Date datetime = new Date(millis * 1000);
        DateFormat formattes2 = new SimpleDateFormat("HH:mm:ss");
        String dateFormatted2 = formattes2.format(datetime);
        return dateFormatted2;
    }

    private String from_byte_to_KB(long sizeBytes) {
        //  return Formatter.formatFileSize(ctx, sizeBytes);
        return sizeBytes / 1024 + " Kb";

    }

}


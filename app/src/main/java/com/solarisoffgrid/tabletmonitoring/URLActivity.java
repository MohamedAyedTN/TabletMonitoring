package com.solarisoffgrid.tabletmonitoring;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class URLActivity extends AppCompatActivity implements AsyncResponse {
    private final Context ctx = this;
    private LayoutInflater mInflater;
    private URLAdapter mAdapter;
    private List<WebSite> webSites = new ArrayList<>();
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        BackgroundCheckReceiver.fetchTopWebSiteAsync.delegate = this;
        listView = findViewById(R.id.url_list);
        showList();
    }

    private void showList() {
        DAOWebsite daoWebsite = new DAOWebsite(ctx);
        daoWebsite.openToRead();
        webSites = daoWebsite.getAllWebSite();
        daoWebsite.close();
        if (!webSites.isEmpty()) {
            mAdapter = new URLAdapter(ctx, webSites);
            listView.setAdapter(mAdapter);
        }
    }

    @Override
    public void topAppFinish() {

    }

    @Override
    public void topWebSitefinish() {
        Log.i("servicebg", "topWeb done");
        showList();
    }

    @Override
    public void checkstatusfinish() {

    }

    static class AppViewHolder {
        TextView urlTitle;
        TextView lastTimeVisited;
        TextView numberOfVisit;
        ImageView urlIcon;
    }

    class URLAdapter extends BaseAdapter {
        private List<WebSite> webSites = new ArrayList<>();
        private Context context;

        URLAdapter(Context context, List<WebSite> webSites) {
            this.context = context;
            this.webSites = webSites;
        }

        @Override
        public int getCount() {
            return webSites.size() > 10 ? 10 : webSites.size();
        }

        @Override
        public Object getItem(int position) {
            return webSites.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AppViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.url_item, null);
                holder = new AppViewHolder();
                holder.urlTitle = convertView.findViewById(R.id.url_title);
                holder.lastTimeVisited = convertView.findViewById(R.id.last_time_visited);
                holder.numberOfVisit = convertView.findViewById(R.id.number_of_visit);
                holder.urlIcon = convertView.findViewById(R.id.url_icon);
                convertView.setTag(holder);
            } else {
                holder = (AppViewHolder) convertView.getTag();
            }
            WebSite webSite = webSites.get(position);
            holder.urlTitle.setText(webSite.getWebsite_title());
            holder.lastTimeVisited.setText(ctx.getResources().getString(R.string.url_item_1) + webSite.getLast_visit());
            holder.numberOfVisit.setText(ctx.getResources().getString(R.string.url_item_2) + webSite.getVisits());
            Bitmap bitmap = BitmapFactory.decodeByteArray(webSite.getIcon(), 0, webSite.getIcon().length);
            holder.urlIcon.setImageBitmap(bitmap);
            return convertView;
        }
    }


}

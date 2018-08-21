package com.solarisoffgrid.tabletmonitoring;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.everything.providers.android.browser.Bookmark;
import me.everything.providers.android.browser.BrowserProvider;

public class URLActivity extends AppCompatActivity {
    private final Context ctx = this;
    private LayoutInflater mInflater;
    private URLAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url);
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

       ListView listView = (ListView) findViewById(R.id.url_list);
        mAdapter = new URLAdapter();
        listView.setAdapter(mAdapter);

    }

    static class AppViewHolder {
        TextView urlTitle;
        TextView lastTimeVisited;
        TextView numberOfVisit;
        ImageView urlIcon;
    }

    class URLAdapter extends BaseAdapter {

        List<Bookmark> bookmarks;

        public URLAdapter() {
            BrowserProvider browserProvider = new BrowserProvider(ctx);
            bookmarks= new ArrayList<>();
            bookmarks = browserProvider.getBookmarks().getList();
          List<Bookmark> bks = new ArrayList<>();

       for(int i=0;i<bookmarks.size();i++){

           boolean duplicate = false;
           int duplicateIndex = 0;
           if(bookmarks.get(i).visits!=0) {
               for (int j = 0; j < bks.size(); j++) {
                   if (bookmarks.get(i).title.contains(bks.get(j).title)) {
                       duplicate = true;
                       duplicateIndex=j;
                       break;
                   }
               }
               if(!duplicate) {
                   bks.add(bookmarks.get(i));
               }else{
                   if (bookmarks.get(i).visits>bks.get(duplicateIndex).visits){
                        bks.remove(duplicateIndex);
                       bks.add(bookmarks.get(i));
                   }
               }

           }
            }
            bookmarks=bks;
            Collections.sort(bookmarks, new Comparator<Bookmark>() {
                public int compare(Bookmark o1, Bookmark o2) {
                    if(o1.visits<o2.visits)
                    return 1;
                    else if(o1.visits>o2.visits)
                        return -1;
                    else return 0;
                }
            });

            for(Bookmark bk : bookmarks) {
                Log.d("url: ", bk.url + "\ntitle: " + bk.title + "\nvisited: " + bk.visits);
            }
        }




        @Override
        public int getCount() {
            return bookmarks.size()>10?10:bookmarks.size();
        }

        @Override
        public Object getItem(int position) {
            return bookmarks.get(position);
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
                holder.lastTimeVisited =  convertView.findViewById(R.id.last_time_visited);
                holder.numberOfVisit =  convertView.findViewById(R.id.number_of_visit);
                holder.urlIcon = convertView.findViewById(R.id.url_icon);
                convertView.setTag(holder);
            } else {
                holder = (AppViewHolder) convertView.getTag();
            }

           Bookmark bookmark = bookmarks.get(position);
            holder = new AppViewHolder();

                holder.urlTitle.setText(bookmark.title);
                holder.lastTimeVisited.setText(DateUtils.formatSameDayTime(bookmark.date,
                        System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM));
                holder.numberOfVisit.setText(bookmark.visits+"");

            Log.d("icon",bookmark.url);
            try {
                Bitmap bmp = BitmapFactory.decodeByteArray(bookmark.favicon, 0, bookmark.favicon.length);
                holder.urlIcon.setImageBitmap(bmp);
            }catch (NullPointerException e){

            }

            /*    try{
                    ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                    Drawable icon = appInfo.loadIcon(mPm);
                    holder.pkgIcon.setImageDrawable(icon);
                } catch (PackageManager.NameNotFoundException e) {
                    // This package may be gone.
                }*/

            return convertView;
        }
    }







}

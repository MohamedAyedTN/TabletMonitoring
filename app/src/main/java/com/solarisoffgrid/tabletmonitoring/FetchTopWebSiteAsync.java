package com.solarisoffgrid.tabletmonitoring;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.format.DateUtils;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.everything.providers.android.browser.Bookmark;
import me.everything.providers.android.browser.BrowserProvider;

import static android.content.Context.MODE_PRIVATE;

public class FetchTopWebSiteAsync extends AsyncTask<Void, Void, List<WebSite>> {


    public static List<WebSite> webSites = new ArrayList<>();
    public AsyncResponse delegate = null;
    public boolean finished = false;
    int topWebSiteCount = 0;
    List<Bookmark> bookmarks;
    private Context ctx;


    public FetchTopWebSiteAsync(Context context) {
        ctx = context;
        SharedPreferences mPrefs = context.getSharedPreferences(ctx.getResources().getString(R.string.sharedpref_title), MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(ctx.getResources().getString(R.string.sharedpref_service), true);
        editor.commit();
    }

    public String extractTitle(String title) {
        return title.subSequence(title.indexOf(".") + 1, title.indexOf(".", title.indexOf(".") + 1)).toString();
    }

    @Override
    protected List<WebSite> doInBackground(Void... errors) {
        BrowserProvider browserProvider = new BrowserProvider(ctx);
        bookmarks = new ArrayList<>();
        try {
            bookmarks = browserProvider.getBookmarks().getList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Bookmark> bks = new ArrayList<>();

        Collections.sort(bookmarks, new Comparator<Bookmark>() {
            public int compare(Bookmark o1, Bookmark o2) {
                if (o1.visits < o2.visits)
                    return 1;
                else if (o1.visits > o2.visits)
                    return -1;
                else return 0;
            }
        });
        for (int i = 0; i < bookmarks.size(); i++) {
            boolean duplicate = false;
            int duplicateIndex = 0;
            if (bookmarks.get(i).visits != 0) {
                for (int j = 0; j < bks.size(); j++) {
                    if (extractTitle(bookmarks.get(i).url).equals(extractTitle(bks.get(j).url))) {
                        duplicate = true;
                        duplicateIndex = j;
                        break;
                    }
                }
                if (!duplicate) {
                    bks.add(bookmarks.get(i));
                    topWebSiteCount++;
                } else {
                    if (bookmarks.get(i).visits > bks.get(duplicateIndex).visits) {
                        bks.remove(duplicateIndex);
                        bks.add(bookmarks.get(i));
                        topWebSiteCount++;
                    }
                }
            }

            if (bks.size() == ctx.getResources().getInteger(R.integer.TOP_WEBSITE_NUMBER)) break;
        }
        for (Bookmark bk : bks) {
            WebSite w = new WebSite();
            w.setWebsite_title(extractTitle(bk.url));
            w.setLast_visit(DateUtils.formatSameDayTime(bk.date,
                    System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM) + "");
            w.setVisits(bk.visits);
            w.setWebsite_url(bk.url);
            try {
                Bitmap bmp = BitmapFactory.decodeByteArray(bk.favicon, 0, bk.favicon.length);
                bmp = Bitmap.createScaledBitmap(bmp, 50, 50, false);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                w.setIcon(stream.toByteArray());
            } catch (NullPointerException e) {
                Bitmap bMap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.stepfinal2);
                Bitmap bMapScaled = Bitmap.createScaledBitmap(bMap, 50, 50, true);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bMapScaled.compress(Bitmap.CompressFormat.PNG, 100, stream);
                w.setIcon(stream.toByteArray());
            }
            webSites.add(w);
        }

        return webSites;
    }

    @Override
    protected void onPostExecute(List<WebSite> webSites) {
        DAOWebsite daoWebsite = new DAOWebsite(ctx);
        daoWebsite.openToWrite();
        daoWebsite.removeAllWebSite();
        for (WebSite a : webSites) daoWebsite.insertWebSite(a);
        daoWebsite.close();
        finished = true;
        new SavetoDBAsync(ctx).execute();
        if (delegate != null) {
            delegate.topWebSitefinish();
        }
    }
}


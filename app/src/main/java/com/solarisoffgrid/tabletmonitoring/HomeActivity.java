package com.solarisoffgrid.tabletmonitoring;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

import android.content.Intent;
import android.database.Cursor;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.provider.Browser;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.media.session.MediaSession;

import java.util.List;
import java.util.Random;

import me.everything.providers.android.browser.Bookmark;
import me.everything.providers.android.browser.BrowserProvider;

public class HomeActivity extends Activity implements OnClickListener {
    private Button lock;
    private Button disable;
    private Button enable;
    private Button top_app;
    private Button top_url;
    private int password=1234;
    static final int RESULT_ENABLE = 1;
private final Context ctx = this;
    DevicePolicyManager deviceManger;
    ActivityManager activityManager;
    ComponentName compName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        deviceManger = (DevicePolicyManager)getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager)getSystemService(
                Context.ACTIVITY_SERVICE);
        compName = new ComponentName(this, MyAdminReceiver.class);
        lock = findViewById(R.id.btnLock);
        lock.setOnClickListener(this);
        disable = findViewById(R.id.btnDisableAdmin);
        enable = findViewById(R.id.btnEnableAdmin);
        top_app = findViewById(R.id.btntop_app);
        top_url = findViewById(R.id.btntop_url);
        top_url.setOnClickListener(this);
        top_app.setOnClickListener(this);
        disable.setOnClickListener(this);
        enable.setOnClickListener(this);


Log.i("gfx","start");

    }
    public void sendNotification(String password) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("New Password set")
                        .setContentText(password);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());
    }

    @Override
    public void onClick(View v) {
        if(v == lock){
            boolean active = deviceManger.isAdminActive(compName);
            if (active) {
             /*   Random rand = new Random();
                password = rand.nextInt(8999)+1000;
                Log.d("password",password+"");*/
              sendNotification(password+"");
               try {
                  deviceManger.resetPassword(password+"",DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
                    deviceManger.lockNow();
                }catch(NullPointerException e){
                    Log.e("NullPE_deviceManger",e.toString());
                }
            }
        }

        if(v == enable){
            Intent intent = new Intent(DevicePolicyManager
                    .ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Additional text explaining why this needs to be added.");
            startActivityForResult(intent, RESULT_ENABLE);
        }

        if(v == disable){
            deviceManger.removeActiveAdmin(compName);
        }

        if(v == top_app){
            Intent i = new Intent(getApplicationContext(),UsageStatsActivity.class);
            startActivity(i);
                }
        if(v == top_url){
            Intent i = new Intent(getApplicationContext(),URLActivity.class);
            startActivity(i);
        }
    }

    private void updateButtonStates() {
        boolean active = deviceManger.isAdminActive(compName);
        if (active) {
            enable.setEnabled(false);
            disable.setEnabled(true);

        } else {
            enable.setEnabled(true);
            disable.setEnabled(false);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i("DeviceAdminSample", "Admin enabled!");
                } else {
                    Log.i("DeviceAdminSample", "Admin enable FAILED!");
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
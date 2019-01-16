package com.solarisoffgrid.tabletmonitoring;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import static android.app.AppOpsManager.OPSTR_WRITE_SETTINGS;

public class HomeActivity extends Activity implements OnClickListener {
    static final int RESULT_ENABLE = 1;
    private final Context ctx = this;
    public String serialNumber = "";
    public SharedPreferences sharedPreferences;
    DevicePolicyManager deviceManger;
    ActivityManager activityManager;
    ComponentName compName;
    private Button lock;
    private Button disable;
    private Button enable;
    private Button top_app;
    private Button top_url;
    private Button login;
    private String password;
    private PendingIntent pendingIntent;
    private AlarmManager manager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        deviceManger = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(
                Context.ACTIVITY_SERVICE);
        compName = new ComponentName(this, MyAdminReceiver.class);
        lock = findViewById(R.id.btnLock);
        lock.setOnClickListener(this);
        disable = findViewById(R.id.btnDisableAdmin);
        enable = findViewById(R.id.btnEnableAdmin);
        top_app = findViewById(R.id.btntop_app);
        top_url = findViewById(R.id.btntop_url);
        login = findViewById(R.id.btnlogin);
        login.setOnClickListener(this);
        top_url.setOnClickListener(this);
        top_app.setOnClickListener(this);
        disable.setOnClickListener(this);
        enable.setOnClickListener(this);
        sharedPreferences = getSharedPreferences("tablet", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("service_running", false);
        editor.commit();
        if (checkForPermission(ctx, OPSTR_WRITE_SETTINGS)) {
            Log.i("permission", "checked");
        }
        if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
         /*   ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    123);*/
            Log.i("Serial", "fail");
        } else {
            serialNumber = Build.SERIAL != Build.UNKNOWN ? Build.SERIAL : Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            editor.putString("serial_number", serialNumber);
            editor.commit();
        }


        Intent alarmIntent = new Intent(this, BackgroundCheckReceiver.class).addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 10000;
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        sendBroadcast(alarmIntent);
        if (!isOnline()) {
            Log.i("servicebg", "no cnction");
        }
        startService(new Intent(this, ConnectionService.class));
        Log.i("servicebg", "intent sent");
    }

    private boolean checkForPermission(Context context, String permission) {
        int result = ContextCompat.checkSelfPermission(context, permission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v == lock) {
            password = sharedPreferences.getString("password", "");
            boolean active = deviceManger.isAdminActive(compName);
            if (active) {
                try {
                    deviceManger.resetPassword(password + "", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
                    deviceManger.lockNow();
                } catch (NullPointerException e) {
                    Log.e("NullPE_deviceManger", e.toString());
                }
            }
        }

        if (v == enable) {
            Intent intent = new Intent(DevicePolicyManager
                    .ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Additional text explaining why this needs to be added.");
            startActivityForResult(intent, RESULT_ENABLE);
        }

        if (v == disable) {
            deviceManger.removeActiveAdmin(compName);
        }

        if (v == top_app) {
            Intent i = new Intent(getApplicationContext(), UsageStatsActivity.class);
            startActivity(i);
        }
        if (v == top_url) {
            Intent i = new Intent(getApplicationContext(), URLActivity.class);
            startActivity(i);
        }
        if (v == login) {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
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
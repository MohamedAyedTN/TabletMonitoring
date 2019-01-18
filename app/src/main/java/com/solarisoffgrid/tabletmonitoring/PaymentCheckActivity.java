package com.solarisoffgrid.tabletmonitoring;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PaymentCheckActivity extends Activity implements OnClickListener, AsyncResponse {
    public SharedPreferences sharedPreferences;
    private String shared_expiration_date;
    private TextView txtNoticeDate;
    private Button btnRefresh, btnAppUSage;
    private PendingIntent pendingIntent;
    private AlarmManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_check);
        FetchPaygStatus.delegate = this;
        txtNoticeDate = findViewById(R.id.txtNoticeDate);
        btnRefresh = findViewById(R.id.btnrefreshstatus);
        btnAppUSage = findViewById(R.id.btn_to_usage);
        btnAppUSage.setOnClickListener(this);
        btnRefresh.setOnClickListener(this);
        Intent alarmIntent = new Intent(this, BackgroundCheckReceiver.class).addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = getResources().getInteger(R.integer.service_loop_interval_in_milliseconds);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        sendBroadcast(alarmIntent);
        startService(new Intent(this, ConnectionService.class));
        showExpirationDate();
    }

    @Override
    public void onClick(View view) {
        if (view == btnRefresh) {
            new FetchPaygStatus(this).execute();
        }
        if (view == btnAppUSage) {
            Intent i = new Intent(getApplicationContext(), UsageStatsActivity.class);
            startActivity(i);
        }
    }

    public void showExpirationDate() {
        sharedPreferences = getSharedPreferences(getResources().getString(R.string.sharedpref_title), MODE_PRIVATE);
        shared_expiration_date = sharedPreferences.getString(getResources().getString(R.string.sharedpref_expiration_date), "");
        if (shared_expiration_date.length() != 0) {
            txtNoticeDate.setText(getResources().getString(R.string.payment_text_1) + shared_expiration_date.substring(0, 10)
                    + getResources().getString(R.string.payment_text_2));
        }
    }

    @Override
    public void topAppFinish() {

    }

    @Override
    public void topWebSitefinish() {

    }

    @Override
    public void checkstatusfinish() {
        showExpirationDate();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_app_usage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.to_app_usage) {
            Intent i = new Intent(getApplicationContext(), UsageStatsActivity.class);
            startActivity(i);
            return true;
        } else onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}

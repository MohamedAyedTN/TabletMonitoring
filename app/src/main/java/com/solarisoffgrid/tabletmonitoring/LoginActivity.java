package com.solarisoffgrid.tabletmonitoring;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static android.app.AppOpsManager.OPSTR_WRITE_SETTINGS;

public class LoginActivity extends Activity implements OnClickListener {
    EditText phoneNumber;
    Button btnLogin;
    static final int RESULT_ENABLE = 1;
    private static final String TAG = LoginActivity.class.getSimpleName();
    private final Context ctx = this;
    public String serialNumber = "";
    public SharedPreferences sharedPreferences;
    DevicePolicyManager deviceManger;
    ActivityManager activityManager;
    ComponentName compName;
    private String phone_number;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        phoneNumber = findViewById(R.id.EdTLogin);
        btnLogin = findViewById(R.id.login);
        btnLogin.setOnClickListener(this);
        deviceManger = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(
                Context.ACTIVITY_SERVICE);
        compName = new ComponentName(this, MyAdminReceiver.class);


        Intent intent = new Intent(DevicePolicyManager
                .ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                compName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Additional text explaining why this needs to be added.");
        startActivityForResult(intent, RESULT_ENABLE);

        sharedPreferences = getSharedPreferences(ctx.getResources().getString(R.string.sharedpref_title), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // editor.clear().commit();
        editor.putBoolean(ctx.getResources().getString(R.string.sharedpref_service), false);
        editor.commit();
        if (checkForPermission(ctx, OPSTR_WRITE_SETTINGS)) {
            Log.i("permission", "checked");
        }
        if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    123);
            Log.i("Serial", "fail");
        } else {
            serialNumber = Build.SERIAL != Build.UNKNOWN ? Build.SERIAL : Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            editor.putString(ctx.getResources().getString(R.string.sharedpref_serial), serialNumber);
            editor.commit();
        }
        sharedPreferences = getSharedPreferences(ctx.getResources().getString(R.string.sharedpref_title), MODE_PRIVATE);
        phone_number = sharedPreferences.getString(getResources().getString(R.string.sharedpref_phone), "");
        if (!phone_number.equals("")) {
            Intent i = new Intent(getApplicationContext(), PaymentCheckActivity.class);
            startActivity(i);
        } else {
            Log.i("servicebg", "no phone number registred");

        }

    }

    @Override
    public void onClick(View v) {
        if (v == btnLogin) {
            if (isOnline()) {
                String phone = phoneNumber.getText().toString();
                if (phone.startsWith("+255") && phone.length() == 13) {
                    RegisterTabletAsync registerTabletAsync = new RegisterTabletAsync(ctx, phone);
                    registerTabletAsync.execute();

                } else {
                    phoneNumber.setError(ctx.getResources().getString(R.string.login_phone_error));
                }
            } else {
                Toast.makeText(ctx, R.string.toast_no_connection, Toast.LENGTH_LONG).show();
            }
        }

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

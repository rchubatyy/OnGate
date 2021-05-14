package app.olivs.OnGate.Activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import app.olivs.OnGate.App.Services;
import app.olivs.OnGate.Databases.DatabaseAccess;
import app.olivs.OnGate.R;
import app.olivs.OnGate.Utils.NetworkUtil;

import static app.olivs.OnGate.App.Services.ServiceRequest;
import static app.olivs.OnGate.App.Services.AUTHENTICATION_TOKEN_KEY;

public class RegisterActivity extends AppCompatActivity implements Response.Listener<JSONObject>, Response.ErrorListener, Services.OnUpdatedSettingsListener {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private TextView messageField;
    private EditText devicePinField;
    private Intent registerIntent;
    DatabaseAccess databaseAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_register);
        TextView instructionLink = findViewById(R.id.instructionLink);
        TextView readMoreLink = findViewById(R.id.readMoreLink);
        devicePinField = findViewById(R.id.devicePIN);
        messageField = findViewById(R.id.messageField);
        instructionLink.setOnClickListener(view -> {
            String url = "https://know.olivs.app/time-attendance/ongate/register-new-device";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });
        readMoreLink.setOnClickListener(view -> {
            String url = "https://olivs.app/time-attendance/ongate";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });
        databaseAccess = DatabaseAccess.getInstance(this);
        requestCameraPermission();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        /*ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PackageManager pm = getApplicationContext().getPackageManager();
        ComponentName compName =
                new ComponentName(getApplicationContext(), SplashScreenActivity.class);
        pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
    }

    public void postDevicePin(View v) throws JSONException {
        String pin = devicePinField.getText().toString();
        if (pin.equals(""))
            return;
        hideKeyboard(this);
        showMessage(false, "Registering this device...");
        JSONObject body = new JSONObject();
        body.put("RegistrationPin",pin);
        body.put("DeviceName", Build.MANUFACTURER + " " + Build.MODEL);
        body.put("OSVersion", Build.VERSION.RELEASE);
        final ServiceRequest request = new ServiceRequest(this, Request.Method.POST, Services.getServiceURL(1), body, this, this) ;
        if (NetworkUtil.getConnectivityStatus(this)==NetworkUtil.TYPE_NOT_CONNECTED) {
            devicePinField.setText("");
            showMessage(true, "You must be connected to Internet to register");
        }
        else
            Volley.newRequestQueue(this).add(request);
    }

    public void cancel(View v){
        PackageManager pm = getApplicationContext().getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        for(ResolveInfo info : resolveInfos) {
            String applicationInfo = info.activityInfo.packageName;
            //...
            System.out.println(applicationInfo);
            //get package name, icon and label from applicationInfo object
        }
        finishAffinity();
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void hideKeyboard(AppCompatActivity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onResponse(JSONObject response) {
        messageField.setText("");
        String token;
        try {
            token = response.getString(AUTHENTICATION_TOKEN_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
            databaseAccess.changeSetting(AUTHENTICATION_TOKEN_KEY, token);
            Services.updateSettings(this, this);
        registerIntent = new Intent(this, UserPinActivity.class);
        registerIntent.putExtra(AUTHENTICATION_TOKEN_KEY, token);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        devicePinField.setText("");
        if (error instanceof ServerError)
            showMessage(true, "PIN incorrect. Please try again");
        else
            showMessage(true, "You must be connected to Internet to register");
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                messageField.setText("");
            }
        }, 3000);
    }

    private void showMessage(boolean isError, String message){
        messageField.setVisibility(View.VISIBLE);
        messageField.setTextColor(isError ? Color.RED : Color.BLACK);
        messageField.setText(message);
    }


    @Override
    public void onUpdatedSettings(boolean success) {
        if (registerIntent != null)
        startActivity(registerIntent);
    }
}
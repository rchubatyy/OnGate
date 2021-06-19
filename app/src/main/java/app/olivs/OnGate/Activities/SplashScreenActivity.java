package app.olivs.OnGate.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.Calendar;

import app.olivs.OnGate.Databases.DatabaseAccess;
import app.olivs.OnGate.Receivers.ScheduledCheckReceiver;

import static app.olivs.OnGate.App.Services.AUTHENTICATION_TOKEN_KEY;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        String token = databaseAccess.getAuthenticationToken();
        Intent intent;
        if ( token == null || token.equals(""))
            {
            intent = new Intent(this, RegisterActivity.class);
            }
        else {
            intent = new Intent(this, UserPinActivity.class);
            intent.putExtra(AUTHENTICATION_TOKEN_KEY, token);
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }


}
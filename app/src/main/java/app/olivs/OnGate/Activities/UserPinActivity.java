package app.olivs.OnGate.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import app.olivs.OnGate.App.Services;
import app.olivs.OnGate.Databases.DatabaseAccess;
import app.olivs.OnGate.R;
import app.olivs.OnGate.Utils.NetworkUtil;

import static app.olivs.OnGate.App.Services.ServiceRequest;
import static app.olivs.OnGate.App.Services.AUTHENTICATION_TOKEN_KEY;

public class UserPinActivity extends AppCompatActivity implements View.OnClickListener, Response.Listener<JSONObject>, Response.ErrorListener {

    private short falseEntriesCount = 0;

    private ConstraintLayout background;
    private AppCompatImageView logo;
    private View.OnLongClickListener adminListener;
    private TextView welcomeMessage, companyName, pleaseEnterPin, link;
    private EditText userPinField;
    private Button ok, delete;
    private DatabaseAccess databaseAccess;
    private boolean isAdmin = false;
    private String companyNameText;

    private static final String linkText = "https://www.olivs.app/ongate";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_user_pin);
        background = findViewById(R.id.background);
        logo = findViewById(R.id.logo);
        adminListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setAdminMode(true);
                return true;
            }
        };
        welcomeMessage = findViewById(R.id.welcomeMessage);
        companyName = findViewById(R.id.companyName);
        pleaseEnterPin = findViewById(R.id.pleaseEnterPIN);
        userPinField = findViewById(R.id.userPIN);
        link = findViewById(R.id.link);
        Button[] keypad = new Button[10];
        ok = findViewById(R.id.btnOK);
        delete = findViewById(R.id.btnDelete);
        for (int i = 0; i < keypad.length; i++) {
            int id = getResources().getIdentifier("btn" + i, "id", getPackageName());
            keypad[i] = findViewById(id);
            Button b = keypad[i];
            b.post(new Runnable() {
                @Override
                public void run() {
                    float size = b.getHeight() >> 2;
                    b.setTextSize(size);
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) userPinField.getLayoutParams();
                    params.width = (int) (size * 8);
                    userPinField.setLayoutParams(params);
                    userPinField.setTextSize(size);
                    userPinField.setPadding(0,(int) -size/2,0, (int) -size/2);
                    ok.setTextSize(size);
                }
            });
        }
        for(Button button: keypad)
            button.setOnClickListener(this);
        ok.setOnClickListener(this);
        delete.setOnClickListener(this);
        logo.setOnLongClickListener(adminListener);
        databaseAccess = DatabaseAccess.getInstance(this);
        companyNameText = databaseAccess.queryDatabase("CompanyName");
        companyName.setText(companyNameText);

        Intent intent = getIntent();
        boolean adminMode = intent.getBooleanExtra("admin",false);
        setAdminMode(adminMode);
    }



    @Override
    public void onResume() {
        super.onResume();
        companyName.setText(companyNameText);
        enableInput();
        falseEntriesCount = 0;
        showPleaseEnterPinNow();
        setAdminMode(false);
        userPinField.setText("");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    /*@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus) {
            // Close every kind of system dialog
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }*/

    @Override
    public void onClick(View view) {
        String pin = userPinField.getText().toString();
        final int btnOK = R.id.btnOK;
        final int btnDelete = R.id.btnDelete;
        switch (view.getId()) {
                case btnOK:
                    try {
                        postUserPin(pin);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case btnDelete:
                    if (!TextUtils.isEmpty(pin)) {
                        String s = pin;
                        s = s.substring(0, s.length() - 1);
                        userPinField.setText(s);
                    }
                    break;
            default:
            char number = view.getResources().getResourceEntryName(view.getId()).charAt(3);
            String newPin = pin + number;
                userPinField.setText(newPin);
                userPinField.setSelection(userPinField.getText().length());
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (error instanceof ServerError) {
            String message = new String (error.networkResponse.data, StandardCharsets.UTF_8);
            if (message.contains("token")){
                databaseAccess.deRegister(this);
            }
        }
        else
            enterNotConnectedMode();
    }

    @Override
    public void onResponse(JSONObject response) {
        String requestStatus, userFullName, companyMessage;
        int userID;
        try {
            requestStatus = response.getString("RequestStatus");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        if (requestStatus.equals("D")){
            databaseAccess.deRegister(this);
        }
        else{
            try {
                userID = response.getInt("UserID");
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            Intent intent = new Intent(this, TakePhotoActivity.class);
            if (userID == 0) {
                userPinField.setText("");
                if (falseEntriesCount<2) {
                    showMessage(true, "PIN incorrect. Please try again");
                    falseEntriesCount++;
                    showPleaseEnterPin();
                }
                else{
                    databaseAccess.incrementEventId();
                    showMessage(false, "Failed to find user");
                    intent.putExtra("UserID",0);
                    intent.putExtra("UserFullName","");
                    startActivity(intent);
                }
            }
            else{
                databaseAccess.incrementEventId();
                try {
                    userFullName = response.getString("UserFullName");
                    companyMessage = response.getString("CompanyMessage");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
                databaseAccess.changeSetting("CompanyMessage", companyMessage);
                intent.putExtra("UserID",userID);
                intent.putExtra("UserFullName",userFullName);
                startActivity(intent);
            }
        }
        if (requestStatus.equals("S")){
            Services.updateSettings(this, null);
        }
    }

    private void postUserPin(String pin) throws JSONException {
        if (pin.equals(""))
            return;
        ok.setOnClickListener(null);
        if(!isAdmin) {
            userPinField.setText("");
            userPinField.setVisibility(View.INVISIBLE);
            showMessage(false, "Searching for user...");
            Intent intent = getIntent();
            String token = intent.getStringExtra(AUTHENTICATION_TOKEN_KEY);
            JSONObject body = new JSONObject();
            body.put("UserPIN",pin);
            body.put(AUTHENTICATION_TOKEN_KEY, token);
            final ServiceRequest request = new ServiceRequest(this, Request.Method.POST, Services.getServiceURL(2), body, this, this);
            if (NetworkUtil.getConnectivityStatus(this)==NetworkUtil.TYPE_NOT_CONNECTED)
                enterNotConnectedMode();
            else
                Volley.newRequestQueue(this).add(request);
        }
        else{
            String adminPin = getAdminPin();
            if (!adminPin.equals(pin)){
                showMessage(true, "Incorrect admin PIN");
                setAdminMode(false);
                showPleaseEnterPin();
            }
            else{
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }

        }
    }

    private void showMessage(boolean isError, String message){
        pleaseEnterPin.setVisibility(View.VISIBLE);
        pleaseEnterPin.setTextColor(isError ? Color.RED : Color.WHITE);
        pleaseEnterPin.setText(message);
    }

    private String getAdminPin(){
        return databaseAccess.queryDatabase("adminPIN");
    }

    private void setAdminMode(boolean isAdmin){
        userPinField.setText("");
        this.isAdmin = isAdmin;
        background.setBackgroundColor(isAdmin ? Color.GRAY : ResourcesCompat.getColor(getResources(), R.color.colorBackground, null));
        welcomeMessage.setText(isAdmin ? "Admin mode" : "Welcome");
        link.setText(isAdmin ? databaseAccess.queryDatabase("SiteName") : linkText);
        background.setOnClickListener( isAdmin ? new View.OnClickListener(
        ) {
            @Override
            public void onClick(View view) {
                setAdminMode(false);
                background.setOnClickListener(null);
            }
        } : null);
    }

    private void showPleaseEnterPin(){
        enableInput();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                showPleaseEnterPinNow();
            }
        }, 2000);
    }
    private void showPleaseEnterPinNow() {
        showMessage(false, "Please enter your PIN");
    }

    private void enableInput(){
        userPinField.setVisibility(View.VISIBLE);
        ok.setOnClickListener(this);
        logo.setOnLongClickListener(adminListener);
    }

    private void enterNotConnectedMode(){
        databaseAccess.incrementEventId();
        Intent newIntent = new Intent(this, TakePhotoActivity.class);
        newIntent.putExtra("UserID", -1);
        startActivity(newIntent);
    }


}
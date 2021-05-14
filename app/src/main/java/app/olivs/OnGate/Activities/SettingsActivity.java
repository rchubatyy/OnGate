package app.olivs.OnGate.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import app.olivs.OnGate.App.Config;
import app.olivs.OnGate.App.Services;
import app.olivs.OnGate.Classes.CheckInRecord;
import app.olivs.OnGate.Databases.DatabaseAccess;
import app.olivs.OnGate.R;
import app.olivs.OnGate.Utils.NetworkUtil;

import static app.olivs.OnGate.App.Services.ServiceRequest;
import static app.olivs.OnGate.App.Services.AUTHENTICATION_TOKEN_KEY;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinnerEvents;
    private DatabaseAccess databaseAccess;
    private ProgressDialog progress;
    private Button btnRun;
    private TextView settings;
    private static final String[] spinnerValue = {
            "Select a command...", "Check Connection", "Sync All Data", "Clear Database", "Deregister This Device", "Close Application", "Update Settings"};
    private int currentCommand = 0;
    private static final String[] settingsList = {"EventID", "AdminPIN", "CompanyName", "CompanyMessage", "LastSync", "ServiceURL", "PhotoResolution"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_settings);
        spinnerEvents = findViewById(R.id.spinnerEvents);
        ArrayAdapter<String> adapterEvents = new ArrayAdapter<String>(SettingsActivity.this,
                android.R.layout.simple_spinner_item, spinnerValue);
        adapterEvents.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEvents.setAdapter(adapterEvents);
        spinnerEvents.setOnItemSelectedListener(this);
        btnRun = findViewById(R.id.btnRun);
        databaseAccess = DatabaseAccess.getInstance(this);
        settings = findViewById(R.id.settingsLabels);
        updateSettingsLabels();
    }

    @Override
    protected void onPause() {
        super.onPause();

        /*ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);*/
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    public void run(View v){
        if (currentCommand != 0)
        executeCommand(currentCommand);
    }

    public void ok(View v){
        finish();
    }

    private void executeCommand(int command){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alert;
        switch (command){
            case 1:
                showProgress("Checking connection...");
                String token = databaseAccess.getAuthenticationToken();
                JSONObject tokenBody = new JSONObject();
                try {
                    tokenBody.put(AUTHENTICATION_TOKEN_KEY, token);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                builder.setPositiveButton("OK", null);
                final ServiceRequest request = new ServiceRequest(this, Request.Method.POST, Services.getServiceURL(6), tokenBody, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progress.dismiss();
                        builder.setTitle("Connected successfully!");
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress.dismiss();
                        builder.setTitle("Connection failed!");
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
                Volley.newRequestQueue(this).add(request);
                break;
            case 2:
                showProgress("Syncing the records...");
                List<CheckInRecord> records = databaseAccess.getAllRecords();
                uploadRecords(records);
                break;
            case 3:
                File photosDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), Config.IMAGE_DIRECTORY_NAME);
                DeleteRecursive(photosDir);
                databaseAccess.clearDatabase();
                builder.setTitle("Database cleared!");
                builder.setPositiveButton("OK", null);
                alert = builder.create();
                alert.show();
                break;
            case 4:
                builder.setTitle("Deregister?");
                builder.setMessage("Are you sure you want to deregister this device?");
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        databaseAccess.deRegister(getApplicationContext());
                    }
                });
                alert = builder.create();
                alert.show();
                break;
            case 5:
                builder.setTitle("Exit?");
                builder.setMessage("Are you sure you want to exit the application?");
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        databaseAccess.close();
                        finishAffinity();
                    }
                });
                alert = builder.create();
                alert.show();
                break;
            case 6:
                /*Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_HOME);
                List<ResolveInfo> pkgAppsList = getPackageManager().queryIntentActivities( mainIntent, 0);
                for (ResolveInfo pack: pkgAppsList)
                System.out.println(pack.toString());*/
                showProgress("Updating settings...");
                Services.updateSettings(this, new Services.OnUpdatedSettingsListener() {
                    @Override
                    public void onUpdatedSettings(boolean success) {
                        progress.dismiss();
                        updateSettingsLabels();
                        builder.setMessage(success? "Setting updated!" : "Failed to update settings");
                        builder.setPositiveButton("OK", null);
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (i==0)
            btnRun.setEnabled(false);
        else{
            btnRun.setEnabled(true);
        currentCommand = i;}
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                child.delete();
                DeleteRecursive(child);
            }

        fileOrDirectory.delete();
    }

    private void uploadRecords(List<CheckInRecord> records) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("OK", null);
        if (NetworkUtil.getConnectivityStatus(this)==NetworkUtil.TYPE_NOT_CONNECTED){
            progress.dismiss();
            builder.setTitle("Missing internet connection to sync");
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }
        if (records.isEmpty()){
            progress.dismiss();
            databaseAccess.updateLastSync();
            builder.setTitle("All records synced!");
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }
            CheckInRecord record = records.get(0);
            Bitmap bm = BitmapFactory.decodeFile(record.getPhotoURL());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] b = baos.toByteArray();
            String encodedImage = "base64," + Base64.encodeToString(b, Base64.DEFAULT);
            String token = databaseAccess.getAuthenticationToken();
            JSONObject body = new JSONObject();
            try {
                body.put(AUTHENTICATION_TOKEN_KEY, token);
                body.put("UserID", 0);
                body.put("PhotoBase64", encodedImage);
                body.put("ActionDateTime", record.getTimestamp());
                body.put("CheckType", record.getType().name());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final ServiceRequest request = new ServiceRequest(this,Request.Method.POST, Services.getServiceURL(3), body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                /*File photo = new File(record.getPhotoURL());
                if (photo.exists())
                    if (photo.delete();*/
                    databaseAccess.updateLastSync();
                    databaseAccess.setRecordSynced(record.getId());
                    if (records.size() > 1) {
                        records.remove(0);
                        uploadRecords(records);
                    } else {
                        updateSettingsLabels();
                        progress.dismiss();
                        builder.setTitle(databaseAccess.getAllRecords().isEmpty() ? "All records synced!" : "Some items didn't sync");
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (records.size() > 1) {
                        records.remove(0);
                        uploadRecords(records);
                    } else {
                        updateSettingsLabels();
                        progress.dismiss();
                        builder.setTitle("Some items didn't sync");
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            });
            Volley.newRequestQueue(this).add(request);
        }

        private void showProgress(String text){
            progress=new ProgressDialog(this);
            progress.setMessage(text);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.show();
        }

        private void updateSettingsLabels(){
            StringBuilder builder = new StringBuilder();
            for (String setting: settingsList){
                String value = databaseAccess.queryDatabase(setting);
                if (setting.equals("CompanyMessage"))
                    value = Html.fromHtml(value).toString().replaceAll("\n", "");
                builder.append(setting);
                builder.append(": ");
                if (value == null)
                    value = "none";
                builder.append(value);
                builder.append("\n");
                builder.append("\n");
            }
            String label = builder.toString();
            settings.setText(label);
            settings.setMovementMethod(new ScrollingMovementMethod());
        }
}
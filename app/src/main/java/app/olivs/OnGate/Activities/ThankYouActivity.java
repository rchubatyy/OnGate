package app.olivs.OnGate.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import app.olivs.OnGate.Databases.DatabaseAccess;
import app.olivs.OnGate.R;

public class ThankYouActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_thank_you);
        Intent intent = getIntent();
        boolean success = intent.getBooleanExtra("SUCCESS", false);
        String fullUserName = intent.getStringExtra("UserFullName");
        String timestamp = intent.getStringExtra("Timestamp");
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        TextView thankYou = findViewById(R.id.thankYou);
        thankYou.setText(String.format("Thank you%s%s", success ? ", " : ". Your EventID is ", fullUserName));
        TextView timestampView = findViewById(R.id.timestamp);
        timestampView.setText(timestamp);
        TextView companyMessage = findViewById(R.id.companyMessage);
        String companyMsgHtml = databaseAccess.queryDatabase("CompanyMessage");
        companyMessage.setText(Html.fromHtml(companyMsgHtml).toString().replaceAll("\n", ""));
        Button newEntry = findViewById(R.id.newEntry);
        newEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

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
package app.olivs.OnGate.App;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {

    private static AppSettings mAppSettings;
    private Context mContext;
    private static final String PREF_SETTINGS = "SETTINGS";
    private static final String PREF_LOG_EMPID = "LOG_EMPID";
    private int logEmpID = 100;

    public AppSettings(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
        load(pref);
        mContext = context;
    }

    void load(SharedPreferences pref) {
        // defaults
        logEmpID = pref.getInt(PREF_LOG_EMPID, logEmpID);
    }

    private void save() {
        SharedPreferences pref = mContext.getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putInt(PREF_LOG_EMPID, logEmpID);
        edit.apply();
    }

    public int setLogEmpID(int empID) {
        logEmpID = empID;
        save();
        return logEmpID;
    }

    public int getLogEmpID() {
        return logEmpID;
    }

    public static AppSettings getInstance(Context context) {
        if (mAppSettings == null) {
            mAppSettings = new AppSettings(context);
        }
        return mAppSettings;
    }
}

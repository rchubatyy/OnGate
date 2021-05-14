
package app.olivs.OnGate.Databases;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import app.olivs.OnGate.Activities.RegisterActivity;
import app.olivs.OnGate.Classes.ActivityState;
import app.olivs.OnGate.Classes.CheckInRecord;
import app.olivs.OnGate.Classes.tblSettings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static app.olivs.OnGate.App.Services.AUTHENTICATION_TOKEN_KEY;

public class DatabaseAccess {
    private static final String TAG = DatabaseAccess.class.getSimpleName();
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;

    /**
     * tblEmpLog
     */
    private static final String KEY_LOGID = "logID";
    private static final String KEY_LOGEMPID = "logEmpID";
    private static final String KEY_LOGEMPNAME = "logEmpName";
    private static final String KEY_LOGEVENTTYPE = "logEventType";
    private static final String KEY_LOGDATETIME = "logDateTime";
    private static final String KEY_LOGPHOTONAME = "logPhotoName";
    private static final String KEY_LOGMAINDBID = "logMainDBid";
    private static final String KEY_ACTIVITYDATE = "activityDate";
    private static final String KEY_TRANSACTIONID = "transactionID";
    private static final String KEY_IMAGEBASE64 = "imageBase64";
    private static final String KEY_ONNEWACTIVITYDATEANDTIME = "onNewActivityDateAndTime";


    /**
     * tblSettings
     */
    private static final String KEY_LOGEMAIL = "loginEmail";
    private static final String KEY_LOGPASSWORD = "loginPassword";
    private static final String KEY_ADMINPIN = "adminPIN";
    private static final String KEY_STATIONID = "stationID";
    private static final String KEY_COMPANYNAME = "companyName";
    private static final String KEY_CURRENTCODETEXTSEQUENCE = "currentCodeTextSequence";
    private static final String KEY_MESSAGEFROMCOMPANY = "messageFromCompany";
    private static final String KEY_TIMEFORLOGIN = "timerForLogin";
    private static final String KEY_SHOWPIN = "showPIN";
    private static final String KEY_DATETIMEFORMAT = "dateTimeFormat";
    private static final String KEY_SHOWBREAK = "showBreak";
    private static final String KEY_DATABASENAME = "dataBaseName";
    private static final String KEY_DEVICEID = "deviceID";
    private static final String KEY_PHOTORESOLUTION = "photoResolution";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_INTERVAL = "interval";
    /**
     * tblErrorLog
     */
    private static final String KEY_errID = "errID";
    private static final String KEY_errNo = "errNo";
    private static final String KEY_errDateTime = "errDateTime";
    private static final String KEY_errEmpID = "errEmpID";
    private static final String KEY_errPIN = "errPIN";
    private static final String KEY_errExceptionID = "errExceptionID";
    private static final String KEY_errExceptionText = "errExceptionText";

    /**
     * Private constructor to aboid object creation from outside classes.
     *
     * @param context
     */
    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    /**
     * Return a singleton instance of DatabaseAccess.
     *
     * @param context the Context
     * @return the instance of DabaseAccess
     */
    public static DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    /**
     * Open the database connection.
     */
    public void open() {
        this.database = openHelper.getWritableDatabase();
    }

    /**
     * Close the database connection.
     */
    public void close() {
        if (database != null) {
            this.database.close();
        }
    }

    /**
     * Check isAdmin.
     *
     * @return isAdmin
     */
    public String isAdmin() {

        //String selectQuery =  "SELECT setValue FROM tblSettings WHERE setName='AdminPIN'";
        String selectQuery = "SELECT adminPin FROM tblSettings";

        Cursor cursor = database.rawQuery(selectQuery, null);

        Log.d(TAG, "adminPin: " + selectQuery);
        String result = "";
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
            Log.d(TAG, "result: " + result);
        } else {
            result = "0";
        }

        return result;
    }

    public List<tblSettings> getSettingsTable() {
        List<tblSettings> settings = new ArrayList<>();
        String selectQuery = "SELECT * FROM tblSettings";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                tblSettings data = new tblSettings();
                //data.setLoginEmail(cursor.getString(0));
                //data.setLoginPassword(cursor.getString(1));
                settings.add(data);
            } while (cursor.moveToNext());
        }

        return settings;
    }

    public ArrayList<tblSettings> getData() {

        ArrayList<tblSettings> arrayList = new ArrayList<>();

        String selectQuery = "SELECT * FROM tblSettings";
        Cursor cursor = database.rawQuery(selectQuery, null);
        Log.d(TAG, "cursor: " + cursor);
        if (cursor.moveToFirst()) {
            do {

                String loginEmail = cursor.getString(1);
                String loginPassword = cursor.getString(2);
                String adminPIN = cursor.getString(3);
                String stationID = cursor.getString(4);
                String companyName = cursor.getString(5);
                String currentCodeTextSequence = cursor.getString(6);
                String messageFromCompany = cursor.getString(7);
                String timerForLogin = cursor.getString(8);
                String showPIN = cursor.getString(9);
                String dateTimeFormat = cursor.getString(10);
                String showBreak = cursor.getString(11);
                String dataBaseName = cursor.getString(12);
                String deviceID = cursor.getString(13);
                String photoResolution = cursor.getString(14);
                String language = cursor.getString(15);

                String interval = cursor.getString(16);

                Log.d(TAG, "loginEmail: " + loginEmail);
                Log.d(TAG, "loginPassword: " + loginPassword);
                Log.d(TAG, "adminPIN: " + adminPIN);
                Log.d(TAG, "stationID: " + stationID);
                Log.d(TAG, "companyName: " + companyName);
                Log.d(TAG, "currentCodeTextSequence: " + currentCodeTextSequence);
                Log.d(TAG, "messageFromCompany: " + messageFromCompany);
                Log.d(TAG, "timerForLogin: " + timerForLogin);
                Log.d(TAG, "showPIN: " + showPIN);
                Log.d(TAG, "dateTimeFormat: " + dateTimeFormat);
                Log.d(TAG, "showBreak: " + showBreak);
                Log.d(TAG, "dataBaseName: " + dataBaseName);
                Log.d(TAG, "deviceID: " + deviceID);
                Log.d(TAG, "photoResolution: " + photoResolution);
                Log.d(TAG, "language: " + language);
                Log.d(TAG, "interval: " + interval);

                //arrayList.add(new tblSettings(lblName,lblValue));

                arrayList.add(new tblSettings(//loginEmail,
                        //loginPassword,
                        adminPIN,
                        stationID,
                        companyName,
                        currentCodeTextSequence,
                        messageFromCompany,
                        timerForLogin,
                        showPIN,
                        dateTimeFormat,
                        showBreak,
                        dataBaseName,
                        deviceID,
                        photoResolution,
                        language,
                        interval));

            } while (cursor.moveToNext());
        }

        cursor.close();

        return arrayList;
    }

    public String updateSettingsTable(String value, String number) {
        String sql = "UPDATE tblSettings SET setValue='" + value + "' WHERE setNo='" + number + "'";
        Log.d(TAG, "lblValue: " + sql);

        String result = "";
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
            Log.e("itemCode", result);
        } else {
            result = "false";
        }
        cursor.close();
        return result;
    }

    public String queryDatabase(String columnName) {
        open();
        String sql = "SELECT " + columnName + " FROM tblSettings";
        String result = "";
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, null);
        }
        catch (SQLiteException e){
            return "";
        }
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
            //Log.e("itemCode", result);
        } else {
            result = "false";
        }
        cursor.close();
        return result;
    }

    public String queryDatabaseText(String language, String name){
        String sql = "SELECT txtValue FROM tblText Where txtName='" + name + "'";
        String result = "";
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
            Log.e("result: ", result);
        } else {
            result = "false";
        }
        cursor.close();
        return result;
    }

    public String queryEmpLog(String logEmpID) {
        String sql = "SELECT logEventType,Max(logDateTime) FROM tblEmpLog WHERE logEmpID ='" + logEmpID + "'";
        String result = "";
        Cursor cursor = database.rawQuery(sql, null);

        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                result = cursor.getString(0);
            } else {
                result = "0";
            }
        } else {
            result = "0";
        }
        assert cursor != null;
        cursor.close();
        return result;
    }

    /**
     * insert data tblEmpLog
     */
    public long insertEmpLog(String logEmpID, String logEmpName, String logEventType, String logDateTime, String logPhotoName, String logMainDBID, String activityDate, String transactionID, String imageBase64, String onNewActivityDateAndTime) {

        Log.v("insertEmpLog: ", "IMHERE");

        ContentValues values = new ContentValues();

        values.put(KEY_LOGEMPID, logEmpID); // logintType
        values.put(KEY_LOGEMPNAME, logEmpName); // dateTime
        values.put(KEY_LOGEVENTTYPE, logEventType); // currentLocation
        values.put(KEY_LOGDATETIME, logDateTime); // remarks
        values.put(KEY_LOGPHOTONAME, logPhotoName); // image
        values.put(KEY_LOGMAINDBID, logMainDBID); // status
        values.put(KEY_ACTIVITYDATE, activityDate); // status
        values.put(KEY_TRANSACTIONID, transactionID); // status
        values.put(KEY_IMAGEBASE64, imageBase64); // status
        values.put(KEY_ONNEWACTIVITYDATEANDTIME, onNewActivityDateAndTime); // status
        Log.e("insertEmpLog: ", "values: " + values);

        long id = database.insert("tblEmpLog", null, values);

        Log.d(TAG, "New user inserted into sqlite: " + id);

        return id;

    }

    /**
     * insert data tblEmpLogOFFLINE
     */
    public long insertEmpLogOffline(String logEmpID, String logEmpName, String logEventType, String logDateTime, String logPhotoName, String logMainDBID, String activityDate, String transactionID, String imageBase64, String onNewActivityDateAndTime) {

        Log.v("insertEmpLog: ", "IMHERE");

        ContentValues values = new ContentValues();

        values.put(KEY_LOGEMPID, logEmpID); // logintType
        values.put(KEY_LOGEMPNAME, logEmpName); // dateTime
        values.put(KEY_LOGEVENTTYPE, logEventType); // currentLocation
        values.put(KEY_LOGDATETIME, logDateTime); // remarks
        values.put(KEY_LOGPHOTONAME, logPhotoName); // image
        values.put(KEY_LOGMAINDBID, logMainDBID); // status
        values.put(KEY_ACTIVITYDATE, activityDate); // status
        values.put(KEY_TRANSACTIONID, transactionID); // status
        values.put(KEY_IMAGEBASE64, imageBase64); // status
        values.put(KEY_ONNEWACTIVITYDATEANDTIME, onNewActivityDateAndTime); // status
        Log.e("insertEmpLog: ", "values: " + values);

        long id = database.insert("tblEmpLog", null, values);

        Log.d(TAG, "New user inserted into sqlite: " + id);

        return id;

    }

    /**
     * BRX ADDED UPDATE
     */
    public boolean updateSettingTable(String loginEmail,
                                      String loginPassword,
                                      String adminPIN,
                                      String stationID,
                                      String companyName,
                                      String currentCodeTextSequence,
                                      String messageFromCompany,
                                      String timerForLogin,
                                      String showPIN,
                                      String dateTimeFormat,
                                      String showBreak,
                                      String dataBaseName,
                                      String deviceID,
                                      String photoResolution,
                                      String language,
                                      String interval) {
        boolean success = false;

        ContentValues values = new ContentValues();

        if (loginEmail != null) {
            values.put(KEY_LOGEMAIL, loginEmail);
        }
        if (loginPassword != null) {
            values.put(KEY_LOGPASSWORD, loginPassword);
        }
        if (adminPIN != null) {
            values.put(KEY_ADMINPIN, adminPIN);
        }
        if (stationID != null) {
            values.put(KEY_STATIONID, stationID);
        }
        if (companyName != null) {
            values.put(KEY_COMPANYNAME, companyName);
        }
        if (companyName != null) {
            values.put(KEY_CURRENTCODETEXTSEQUENCE, currentCodeTextSequence);
        }
        if (messageFromCompany != null) {
            values.put(KEY_MESSAGEFROMCOMPANY, messageFromCompany);
        }
        if (timerForLogin != null) {
            values.put(KEY_TIMEFORLOGIN, timerForLogin);
        }
        if (showPIN != null) {
            values.put(KEY_SHOWPIN, showPIN);
        }
        if (showPIN != null) {
            values.put(KEY_DATETIMEFORMAT, dateTimeFormat);
        }
        if (showBreak != null) {
            values.put(KEY_SHOWBREAK, showBreak);
        }
        if (dataBaseName != null) {
            values.put(KEY_DATABASENAME, dataBaseName);
        }
        if (deviceID != null) {
            values.put(KEY_DEVICEID, deviceID);
        }
        if (photoResolution != null) {
            values.put(KEY_PHOTORESOLUTION, photoResolution);
        }
        if (language != null) {
            values.put(KEY_LANGUAGE, language);
        }

        if (interval != null) {
            values.put(KEY_INTERVAL, interval);
        }

        if (database.update("tblSettings", values, null, null) > 0)
            success = true;
        Log.e("UPDATE RESULT:", success + "");

        return success;
    }


    public String countEmpLog() {
        String sql = "SELECT Count(*) FROM tblEmpLog WHERE logMainDBid = 0";
        String result = "";
        Cursor cursor = database.rawQuery(sql, null);

        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                result = cursor.getString(0);
            } else {
                result = "0";
            }
        } else {
            result = "0";
        }
        assert cursor != null;
        cursor.close();
        return result;
    }

    public String getLogID(String logEmpName) {
        String sql = "SELECT logID FROM tblEmpLog WHERE logEmpName ='" + logEmpName + "'";
        String result = "";
        Cursor cursor = database.rawQuery(sql, null);

        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                result = cursor.getString(0);
            } else {
                result = "0";
            }
        } else {
            result = "0";
        }
        assert cursor != null;
        cursor.close();
        return result;
    }

    public String getInterval() {
        String sql = "SELECT interval FROM tblSettings";
        String result = "";
        Cursor cursor = database.rawQuery(sql, null);

        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                result = cursor.getString(0);
            } else {
                result = "0";
            }
        } else {
            result = "0";
        }
        assert cursor != null;
        cursor.close();
        return result;
    }

    /**
     * insert data tblErrorLog
     */
    public void insertErrorLog(

            String errNo,
            String errDateTime,
            String errEmpID,
            String errPIN,
            String errExceptionID,
            String errExceptionText) {

        Log.v("insertErrorLog: ", "IMHERE");

        ContentValues values = new ContentValues();

        values.put(KEY_errNo, errNo); // dateTime
        values.put(KEY_errDateTime, errDateTime); // currentLocation
        values.put(KEY_errEmpID, errEmpID); // remarks
        values.put(KEY_errPIN, errPIN); // image
        values.put(KEY_errExceptionID, errExceptionID); // status
        values.put(KEY_errExceptionText, errExceptionText); // logintType

        Log.e("insertEmpLog: ", "values: " + values);

        long id = database.insert("tblErrorLog", null, values);

        Log.d(TAG, "New data inserted into tblErrorLog: " + id);

    }

    /**
     * BRX ADDED UPDATE
     */
    public boolean updateTblEmpLog(String logMainDBid, String logID) {

        boolean success = false;

        String sql = "UPDATE tblEmpLog SET logMainDBid='" + logMainDBid + "' WHERE logID='" + logID + "'";

        Log.d(TAG, "lblValue: " + sql);

        String result = "";

        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
            Log.e("itemCode", result);
            success = true;
        } else {
            success = false;
        }
        cursor.close();
        return success;
    }

    public String checkRecord() {
        String sql = "SELECT * FROM tblEmpLog WHERE logMainDBid = 0";
        String result = "";
        Cursor cursor = database.rawQuery(sql, null);
        cursor.moveToLast(); //if you not place this cursor.getCount() always give same integer (1) or current position of cursor.

        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                result = String.valueOf(cursor.getCount());
            } else {
                result = "0";
            }
        } else {
            result = "0";
        }
        cursor.close();
        return result;
    }

    public Cursor getDataLimit() {

        //String selectQuery = "SELECT * FROM tblEmpLog WHERE logMainDBid = 0";
        //String selectQuery = "SELECT logID,logEmpID, logEmpName, logEventType, logDateTime, logPhotoName FROM tblEmpLog WHERE logMainDBid = 0 LIMIT 1";
        String selectQuery = "SELECT * FROM tblEmpLog WHERE logMainDBid = 0 LIMIT 1";
        // String selectQuery = "SELECT * FROM tblEmpLog WHERE logMainDBid = 0";
        Log.d("BRX", "getAllData selectQuery: >" + selectQuery);
        return database.rawQuery(selectQuery, null);
    }


    public Cursor getDataNoLimit() {
        String selectQuery = "SELECT * FROM tblEmpLog WHERE logMainDBid = 0";
        Log.d("BRX", "getAllData selectQuery: >" + selectQuery);
        return database.rawQuery(selectQuery, null);
    }

    public Cursor getAllData() {

        //String selectQuery = "SELECT * FROM tblEmpLog WHERE logMainDBid = 0";
        String selectQuery = "SELECT logID,logEmpID, logEmpName, logEventType, logDateTime, logPhotoName FROM tblEmpLog WHERE logMainDBid = 0";
        Log.d("BRX", "getAllData selectQuery: >" + selectQuery);
        return database.rawQuery(selectQuery, null);
    }

    /**
     * BRX ADDED UPDATE
     */

    public boolean updateAllTblEmpLog(String logMainDBid, String transactionID) {

        boolean success = false;
        // String sql = "UPDATE tblEmpLog SET logMainDBid='" + logMainDBid + "' WHERE logMainDBid = 0";
        String sql = "UPDATE tblEmpLog SET logMainDBid='" + logMainDBid + "' WHERE transactionID='" + transactionID + "'";

        Log.d(TAG, "lblValue: " + sql);

        String result = "";

        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
            Log.e("itemCode", result);
            success = true;
        } else {
            success = false;
        }
        cursor.close();
        return success;
    }

    public boolean clearData() {
        boolean success = false;
        try {
            database.execSQL("DROP TABLE IF EXISTS tblEmpLog");
            success = true;
        } catch (SQLException e) {
            success = false;
        }

        return success;
    }

    public boolean createTableTblEmLog() {

        boolean success = false;

        String sql = "CREATE TABLE tblEmpLog ( `logID` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, `logEmpID` TEXT NOT NULL, `logEmpName` TEXT NOT NULL, `logEventType` TEXT NOT NULL, `logDateTime` TEXT NOT NULL, `logPhotoName` TEXT NOT NULL, `logMainDBid` TEXT NOT NULL, `activityDate` TEXT NOT NULL, `transactionID` TEXT NOT NULL, `imageBase64` TEXT NOT NULL, `onNewActivityDateAndTime` TEXT )";

        Log.d(TAG, "lblValue: " + sql);

        String result = "";

        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
            Log.e("result: ", result);
            success = true;
        } else {
            success = false;
        }
        cursor.close();
        return success;
    }

    public boolean setAuthenticationToken(String token){
        open();
        boolean success = false;

        String sql = "UPDATE tblSettings SET AuthenticationToken = '" + token + "' WHERE id=1";

        Log.d(TAG, "lblValue: " + sql);

        String result = "";

        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
            Log.e("itemCode", result);
            success = true;
        } else {
            success = false;
        }
        cursor.close();
        return success;
    }

    public void changeSetting(String key, String value) {
        open();
        boolean success = false;

        String sql = "UPDATE tblSettings SET "+ key  + " = '" + value + "' WHERE id=1";

        String result = "";

        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
            success = true;
        } else {
            success = false;
        }
        cursor.close();
    }




    public void deRegister(Context context){
        open();
        String sql = "DELETE from tblSettings";
        database.execSQL(sql);
        DatabaseOpenHelper.initSettings(database);
        Intent intent = new Intent(context, RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void clearDatabase(){
        open();
        String sql = "DELETE from tblRecords";
        database.execSQL(sql);
    }



    public String getAuthenticationToken() throws SQLiteException{
        open();
        return queryDatabase(AUTHENTICATION_TOKEN_KEY);
    }

    public void incrementEventId() throws SQLiteException{
        open();
        String sql = "UPDATE tblSettings SET EventID = EventID + 1";
        database.execSQL(sql);
    }

    public void saveRecord(CheckInRecord record){
        open();
        ContentValues values = new ContentValues();
        values.put("UserID", record.getUserID());
        values.put("EventID", record.getEventID());
        values.put("TimeStamp", record.getTimestamp());
        values.put("PhotoName", record.getPhotoURL());
        values.put("Synced", record.isSynced());
        values.put("State", record.getType().name());
        database.insert("tblRecords", null, values);
    }

    public List<CheckInRecord> getAllRecords() {
        open();
        List<CheckInRecord> list = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM tblRecords WHERE Synced = 0", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            CheckInRecord record = new CheckInRecord(cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getInt(5) == 1,
                    ActivityState.valueOf(cursor.getString(6)));
            list.add(record);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public void setRecordSynced(int id){
        open();
        String sql = "UPDATE tblRecords SET Synced = 1 WHERE id = " + id;
        database.execSQL(sql);
    }

    public void updateLastSync(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String time = dateFormat.format(Calendar.getInstance().getTime());
        changeSetting("LastSync",time);
    }


}
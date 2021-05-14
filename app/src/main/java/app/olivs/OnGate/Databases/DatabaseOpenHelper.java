
package app.olivs.OnGate.Databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "database.db";
    private static final int DB_VERSION = 1;

    public DatabaseOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS tblSettings (" +
                "        id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "        AuthenticationToken TEXT," +
                "        EventID INTEGER," +
                "        AdminPIN VARCHAR(6)," +
                "        CompanyName TEXT," +
                "        CompanyMessage TEXT," +
                "        LastSync DATETIME," +
                "        ServiceURL TEXT," +
                "        SiteName TEXT," +
                "        PhotoResolution " +
                "        );") ;
        initSettings(db);
        db.execSQL("       CREATE TABLE IF NOT EXISTS tblRecords (" +
                "        id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "        UserID INTEGER," +
                "        EventID INTEGER," +
                "        TimeStamp DATETIME," +
                "        PhotoName TEXT," +
                "        Synced BOOL," +
                "        State TEXT" +
                "        );" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    static void initSettings(SQLiteDatabase db){
        db.execSQL( "  INSERT INTO tblSettings VALUES " +
                "  (1, '', 0, '123456', '', '', 'never', 'https://webservice4.olivs.cloud/', '', 'n/a');") ;
    }
}
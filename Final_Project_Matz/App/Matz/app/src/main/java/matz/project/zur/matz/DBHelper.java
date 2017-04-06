package matz.project.zur.matz;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by user on 1/29/2017.
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final String TABLE_USERS = "user";
    public static final String COLUMN_USERNAME = "name";
    public static final String COLUMN_PASSWORD = "password";


    private static final String DATABASE_NAME = "students.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_USERS + "( " + COLUMN_USERNAME
            + " text primary key not null, " + COLUMN_PASSWORD
            + " text not null" +
            ");";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}

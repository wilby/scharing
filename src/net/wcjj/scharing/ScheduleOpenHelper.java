package net.wcjj.scharing;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created with IntelliJ IDEA.
 * User: androtheos
 * Date: 8/26/12
 * Time: 8:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScheduleOpenHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Schedule.sql3";
    public static final String DATABASE_CREATE_SQL = "";
    public static final String DATABASE_UPGRADE_SQL = "";

    ScheduleOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL(DATABASE_UPGRADE_SQL);
    }


}

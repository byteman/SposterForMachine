package cn.cloudwalk.sposterformachine.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jing on 16/5/6.
 */
public class DbOpenHelper extends SQLiteOpenHelper{
    private static DbOpenHelper instance;
    private static final int DATABASE_VERSION = 1;
    private static final String USER_TABLE_CREATE = "create table if not exists" +
            " users(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "phone text," +
            "img blob )";
    private static final String REG_TABLE_CREATE = "create table if not exists" +
            " regist(" +
            " id INTEGER PRIMARY KEY AUTOINCREMENT," +
            " phone text," +
            " img blob)";
    private static final String USER_TABLE_DROP = "drop table if exists users";
    private static final String REG_TABLE_DROP = "drop table if exists regist";
    public DbOpenHelper(Context context) {
        super(context,"sposter.db", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(USER_TABLE_CREATE);
        db.execSQL(REG_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(USER_TABLE_DROP);
        db.execSQL(REG_TABLE_DROP);
    }

    public static DbOpenHelper getInstance(Context context){
        if(instance==null){
            instance = new DbOpenHelper(context.getApplicationContext());
        }
        return instance;
    }
}

package cn.cloudwalk.sposterformachine.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Jing on 16/5/6.
 */
public class RegistDao {
    public static final String TABLE_NAME = "regist";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_IMG = "img";
    private DbOpenHelper dbHelper;
    public RegistDao(Context context) {
        dbHelper = DbOpenHelper.getInstance(context);
    }
    //删除一条数据
    public void removeRegist(String phone){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if(db.isOpen()){
            db.delete(TABLE_NAME,COLUMN_PHONE+" = ?",new String[]{phone});
        }
        db.releaseReference();
        db.close();
    }
    //保存一条数据
    public void insertRegist(Regist regist){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID,regist.id);
        values.put(COLUMN_PHONE,regist.phone);
        values.put(COLUMN_IMG,regist.img);
        if(db.isOpen()){
            db.replace(TABLE_NAME,null,values);
        }
        db.releaseReference();
        db.close();
    }
    //更新一条数据
    public void updateRegist(Regist regist,String id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID,regist.id);
        values.put(COLUMN_PHONE,regist.phone);
        values.put(COLUMN_IMG,regist.img);
        if(db.isOpen()){
            int i = db.update(TABLE_NAME,values,COLUMN_ID+"=?",new String[]{id});
            db.close();
        }
        db.releaseReference();
        db.close();
    }
}

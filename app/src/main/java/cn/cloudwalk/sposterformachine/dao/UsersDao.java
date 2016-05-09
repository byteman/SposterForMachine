package cn.cloudwalk.sposterformachine.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Jing on 16/5/6.
 * 处理表users的各种操作
 */
public class UsersDao {
    public static final String TABLE_NAME = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_IMG = "img";
    private DbOpenHelper dbHelper;
    public UsersDao(Context context) {
        dbHelper = DbOpenHelper.getInstance(context);
    }
    //删除一条数据
    public void removeUsers(String phone){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if(db.isOpen()){
            db.delete(TABLE_NAME,COLUMN_PHONE+" = ?",new String[]{phone});
        }
        db.releaseReference();
        db.close();
    }
    //保存一条数据
    public void insertUsers(Users users){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID,users.id);
        values.put(COLUMN_PHONE,users.phone);
        values.put(COLUMN_IMG,users.img);
        if(db.isOpen()){
            db.replace(TABLE_NAME,null,values);
        }
        db.releaseReference();
        db.close();
    }
    //更新一条数据
    public void updateUsers(Users users,String id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID,users.id);
        values.put(COLUMN_PHONE,users.phone);
        values.put(COLUMN_IMG,users.img);
        if(db.isOpen()){
            int i = db.update(TABLE_NAME,values,COLUMN_ID+"=?",new String[]{id});
            db.close();
        }
        db.releaseReference();
        db.close();
    }
}

package com.dataexpo.autogate.comm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.dataexpo.autogate.model.User;

import java.util.ArrayList;

public class DBUtils {
    private final String TAG = DBUtils.class.getSimpleName();
    private final String dbname = "gate_user";
    private final String dbnamePath = "/gate.db";
    private SQLiteDatabase db;

    private static class HolderClass {
        private static final DBUtils instance = new DBUtils();
    }

    /**
     * 单例模式
     */
    public static DBUtils getInstance() {
        return HolderClass.instance;
    }

    /**
     * 创建数据表
     * @param contenxt 上下文对象
     */
    public void create(Context contenxt) {
        String path = contenxt.getCacheDir().getPath() + dbnamePath;
        Log.i(TAG, "path========="+ path);
        db = SQLiteDatabase.openOrCreateDatabase(path, null);
        String sql = "create table if not exists " + dbname +
                "(id integer primary key autoincrement," +
                "name nchar(50), " +
                "company nchar(50), " +
                "position nchar(50), " +
                "cardcode nchar(50), " +
                "gender int, " +
                "code nchar(50))";
        db.execSQL(sql);//创建表
    }

    /**
     * 添加数据
     * bsid 添加的数据ID
     * name 添加数据名称
     */
    public long insertData(String name, String company, String position, String cardcode, int gender, String code) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("company", company);
        contentValues.put("cardcode", cardcode);
        contentValues.put("position", position);
        contentValues.put("gender", gender);
        contentValues.put("code", code);
        return db.insert(dbname, null, contentValues);
    }

    /**
     * 查询数据
     * 返回List
     */
    public ArrayList<User> listAll() {
        ArrayList<User> list = new ArrayList<>();
        Cursor cursor = db.query(dbname, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String company = cursor.getString(cursor.getColumnIndex("company"));
            String cardcode = cursor.getString(cursor.getColumnIndex("cardcode"));
            String position = cursor.getString(cursor.getColumnIndex("position"));
            int gender = cursor.getInt(cursor.getColumnIndex("gender"));
            String code = cursor.getString(cursor.getColumnIndex("code"));
            list.add(new User(id, name, company, cardcode, position, gender, code));
        }

        cursor.close();
        return list;
    }

    /**
     * 根据ID删除数据
     * id 删除id
     */
    public int delData(int id) {
        Log.e(TAG, "id==============" + id);
        int inde = db.delete(dbname, "id = ?", new String[]{String.valueOf(id)});
        Log.e(TAG, "删除了==============" + inde);
        return inde;
    }

    /**
     * 根据
     *
     */
    public int delDataAll() {
        int inde = db.delete(dbname,null,null);
        Log.e("--Main--", "删除了==============" + inde);
        return inde;
    }

    /**
     * 根据ID修改数据
     * id 修改条码的id
     * bsid 修改的ID
     * name 修改的数据库
     */
    public int modifyData(int id, int bsid, String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("bsid", id);
        int index = db.update(dbname, contentValues, "id = ?", new String[]{String.valueOf(id)});
        Log.e("--Main--", "修改了===============" + index);
        return index;
    }

    /**
     * 查询code单个数据
     * @param code
     * @return
     */
    public boolean findByCode(String code) {
        //查询数据库
        Cursor cursor = db.query(dbname, null, "code = ?", new String[]{code}, null, null, null);
        while (cursor.moveToNext()) {
            return true;
        }
        return false;
    }
}
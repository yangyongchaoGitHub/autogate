package com.dataexpo.autogate.comm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.dataexpo.autogate.model.User;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class DBUtils {
    private final String TAG = DBUtils.class.getSimpleName();
    public final static String TABLE_USER = "gate_user";
    public final static String TABLE_CARD_RECORD = "gate_card_record";
    public final static String TABLE_FACE_RECORD = "gate_face_record";
    public final static String TABLE_RFID_GATE = "gate_rfid";

    private final String dbnamePath = "/gate.db";
    private SQLiteDatabase db;
    private String selectionArgs[] = new String[1];
    private String where1 = " = ?";

    public void beginTransaction() {
        db.beginTransaction();
    }

    public void endTransaction() {
        db.endTransaction();
    }

    public void setTransactionSuccessful() {
        db.setTransactionSuccessful();
    }

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
        String path = contenxt.getDir("databases", MODE_PRIVATE).getPath() + dbnamePath;
        //String path = contenxt.getCacheDir().getPath() + dbnamePath;
        Log.i(TAG, "db path========="+ path);
        db = SQLiteDatabase.openOrCreateDatabase(path, null);

        String sql_user = "create table if not exists " + TABLE_USER +
                "(id integer primary key autoincrement," +
                "pid int, " +
                "expoId int, " +
                "name nchar(64), " +
                "company nchar(64), " +
                "position nchar(32), " +
                "cardcode nchar(32), " +
                "gender int, " +
                "code nchar(64), " +
                "image_name nchar(64), " +
                "image_base64 nchar(64), " +
                "image_type int, " +
                "image_sync int, " +
                "ctime long, " +
                "update_time long, " +
                "userinfo nchar(64), " +
                "bregist_face int, " +
                "face_token varchar(128), " +
                "auth int, " +
                "feature blob)";

        String sql_card_record = "create table if not exists " + TABLE_CARD_RECORD +
                "(id integer primary key autoincrement," + // id
                "number char(32), " +                      // 卡号
                "eucode char(32), " +                      // eucode
                "direction int, " +                        // 进出方向
                "model int, " +                            // 通道模式 0:普通模式; 1验证模式
                "time char(32), " +                        // 记录的日期
                "pid int, " +                              // 用户的euid
                "permissionid int, " +                     // 权限id
                "address char(32), " +                     // 门禁地址
                "status int" +                             // 是否成功通过 1失败， 2通过
                ")";

        String sql_face_record = "create table if not exists " + TABLE_FACE_RECORD +
                "(id integer primary key autoincrement," +
                "user_id integer, " +
                "code nchar(64), " +
                "time char(32), " +
                "imagename nchar(32), " +
                "company nchar(64), " +
                "cardcode nchar(32)," +
                "name nchar(64))";

        String sql_rfid_gate = "create table if not exists " + TABLE_RFID_GATE +
                "(id integer primary key autoincrement," +
                "ip char(32), " +
                "name char(64), " +
                "remark nchar(512), " +
                "port char(32))";

        db.execSQL(sql_user);//创建表 user
        db.execSQL(sql_card_record);//创建表 card_record
        db.execSQL(sql_face_record);//创建表 face_record
        db.execSQL(sql_rfid_gate);//创建表 rfid_gate
    }

    //添加数据基础方法
    public long insert(String tebleName, String nullColumnHack, ContentValues contentValues) {
        try {
            return  db.insert(tebleName, nullColumnHack, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     * 查询全部的基本方法
     */
    public Cursor listAll(String table_name, String[] columns, String selection,
                          String[] selectionArgs, String groupBy, String having,
                          String orderBy) {

        return db.query(table_name, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    public Cursor rowQuery(String sql, String[] selectionArgs) {
        return db.rawQuery(sql, selectionArgs);
    }
    /**
     *
     * @param table_name 表名
     * @param start 起始索引  2 --》从第3行开始返回
     * @param size 获取数据长度  5 -- 返回5行数据
     * @return 查询引用返回
     */
    public Cursor limitWith(String table_name, int start, int size) {
        return db.query(table_name, null, null, null, null, null, null, start + "," + size);
    }

    /**
     * 根据ID删除数据
     * id 删除id
     */
    public int delData(String table, int id) {
        //Log.e(TAG, "id==============" + id);
        int inde = db.delete(table, "id = ?", new String[]{String.valueOf(id)});
        //Log.e(TAG, "删除了==============" + inde);
        return inde;
    }

    /**
     * 删除整个table
     *
     */
    public int delDataAll(String table) {
        //Log.e("--Main--", "删除了==============" + inde);
        return db.delete(table,null,null);
    }

    public int delDataBy(String table, String whereClause, String[] whereArgs) {
        //Log.e("--Main--", "删除了==============" + inde);
        return db.delete(table, whereClause, whereArgs);
    }

    /**
     * 根据ID修改数据
     * id 修改条码的id
     */
    public int modifyData(String table, int id, ContentValues contentValues) {
        int index = db.update(table, contentValues, "id = ?", new String[]{String.valueOf(id)});
        //Log.e("--Main--", "修改了===============" + index);
        return index;
    }

    public int update(String table, ContentValues contentValues, String whereClause, String[] args) {
        int index = db.update(table, contentValues, whereClause, args);
        //Log.e("--Main--", "修改了===============" + index);
        return index;
    }

    public int count(String sql) {
        Cursor cursor = db.rawQuery(sql, null);
        int res = 0;
        try {
            cursor.moveToFirst();
            res = Integer.parseInt(cursor.getString(0));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }


        return res;
    }

    public int count(String sql, String[] args) {
        Cursor cursor = db.rawQuery(sql, args);
        int res = 0;
        try {
            cursor.moveToFirst();
            res = Integer.parseInt(cursor.getString(0));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        return res;
    }

    /**
     * 查询单个数据是否存在
     * @param code
     * @return
     */
    public Cursor findBy(String table, String where, String code) {
        selectionArgs[0] = code;
        //查询数据库
       return db.query(table, null, where + where1, selectionArgs, null, null, null);
    }


}
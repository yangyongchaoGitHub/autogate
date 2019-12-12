package com.dataexpo.autogate.face.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FaceGroupService {
    private static final String TAG = FaceGroupService.class.getSimpleName();
    private FaceGroupService() {};

    private static class HolderClass {
        private static final FaceGroupService instance = new FaceGroupService();
    }

    /**
     * 单例模式
     */
    public static FaceGroupService getInstance() {
        return FaceGroupService.HolderClass.instance;
    }

//    public void addGroup() {
//        Cursor cursor = null;
//
//        String where = "group_id = ? ";
//        String[] whereValue = { group.getGroupId() };
//        // 查询该groupId是否在数据库中，如果在，则不添加
//        cursor = db.query(DBHelper.TABLE_USER_GROUP, null, where, whereValue,
//                null, null, null);
//        if (cursor == null) {
//            return false;
//        }
//
//        if (cursor.getCount() > 0) {
//            return true;
//        }
//
//        mDatabase = mDBHelper.getWritableDatabase();
//        ContentValues cv = new ContentValues();
//        cv.put("group_id", group.getGroupId());
//        cv.put("desc", group.getDesc() == null ? "" : group.getDesc());
//        cv.put("update_time", System.currentTimeMillis());
//        cv.put("ctime", System.currentTimeMillis());
//
//        long rowId = -1;
//        try {
//            rowId = mDatabase.insert(DBHelper.TABLE_USER_GROUP, null, cv);
//        } catch (Exception e) {
//            Log.e(TAG, "addGroup e = " + e.getMessage());
//            e.printStackTrace();
//        }
//        if (rowId < 0) {
//            return false;
//        }
//        Log.i(TAG, "insert group success:" + rowId);
//        closeCursor(cursor);
//        return true;
//    }
}

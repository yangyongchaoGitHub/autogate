package com.dataexpo.autogate.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.dataexpo.autogate.comm.DBUtils;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.model.TestData;
import com.dataexpo.autogate.model.User;

import java.io.File;
import java.util.ArrayList;

public class UserService {
    private static final String TAG = UserService.class.getSimpleName();
    private UserService() {};

    private static class HolderClass {
        private static final UserService instance = new UserService();
    }

    /**
     * 单例模式
     */
    public static UserService getInstance() {
        return UserService.HolderClass.instance;
    }

    public User findUserByCode(User user) {
        Cursor cursor = DBUtils.getInstance().findBy(DBUtils.TABLE_USER, "code", user.code);
        User user_response = null;

        if (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String company = cursor.getString(cursor.getColumnIndex("company"));
            String cardcode = cursor.getString(cursor.getColumnIndex("cardcode"));
            String position = cursor.getString(cursor.getColumnIndex("position"));
            int gender = cursor.getInt(cursor.getColumnIndex("gender"));
            String code = cursor.getString(cursor.getColumnIndex("code"));
            user_response = new User (id, name, company, cardcode, position, gender, code);
        }
        cursor.close();
        return user_response;
    }

    public User findUserByCardCode(User user) {
        Cursor cursor = DBUtils.getInstance().findBy(DBUtils.TABLE_USER, "cardcode", user.cardCode);
        User user_response = null;

        if (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String company = cursor.getString(cursor.getColumnIndex("company"));
            String cardcode = cursor.getString(cursor.getColumnIndex("cardcode"));
            String position = cursor.getString(cursor.getColumnIndex("position"));
            int gender = cursor.getInt(cursor.getColumnIndex("gender"));
            String code = cursor.getString(cursor.getColumnIndex("code"));
            user_response = new User (id, name, company, cardcode, position, gender, code);
        }
        cursor.close();
        return user_response;
    }

    public long insert(User user) {
        if (checkByCode(user)) {
            Log.i(TAG, "用户： " + user.code + " 重复！！！！！");
            return 0l;
        }
        byte[] images = FileUtils.base64ToBytes(user.image);
        if (images.length > 0) {
            FileUtils.writeByteFile(images, FileUtils.getBatchImportDirectory().getPath() + "/" + user.code + ".jpg");
        }
        return insert(user.name, user.company, user.position, user.cardCode, user.gender, user.code);
    }

    private boolean checkByCode(User user) {
        Cursor cursor = DBUtils.getInstance().findBy(DBUtils.TABLE_USER, "code", user.code);
        boolean result = cursor.moveToNext();
        cursor.close();
        return result;
    }

    public long insert(TestData data) {
        byte[] image = FileUtils.base64ToBytes(data.image);
        if (image.length > 0) {
            Log.i(TAG, "insert test data: " + data.code);
            FileUtils.writeByteFile(image, FileUtils.getBatchImportDirectory().getPath() + "/" + data.code + ".jpg");
        }

        return 0l;
    }

    /**
     * 添加数据
     */
    public long insert(String name, String company, String position, String cardcode, int gender, String code) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("company", company);
        contentValues.put("cardcode", cardcode);
        contentValues.put("position", position);
        contentValues.put("gender", gender);
        contentValues.put("code", code);
        return DBUtils.getInstance().insert(DBUtils.TABLE_USER, null, contentValues);
    }

    /**
     * 查询全部用户数据
     * 返回List
     */
    public ArrayList<User> listAll() {
        ArrayList<User> list = new ArrayList<>();
        Cursor cursor = DBUtils.getInstance().listAll(DBUtils.TABLE_USER, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String company = cursor.getString(cursor.getColumnIndex("company"));
            String cardcode = cursor.getString(cursor.getColumnIndex("cardcode"));
            String position = cursor.getString(cursor.getColumnIndex("position"));
            int gender = cursor.getInt(cursor.getColumnIndex("gender"));
            String code = cursor.getString(cursor.getColumnIndex("code"));
            list.add(new User(id, name, company, position, cardcode, gender, code));
        }

        cursor.close();
        return list;
    }
}

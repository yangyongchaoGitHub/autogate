package com.dataexpo.autogate.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.dataexpo.autogate.comm.DBUtils;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.face.api.FaceApi;
import com.dataexpo.autogate.face.model.SingleBaseConfig;
import com.dataexpo.autogate.model.TestData;
import com.dataexpo.autogate.model.User;

import java.util.ArrayList;

import static com.dataexpo.autogate.face.manager.ImportFileManager.IMPORT_REPEAT;
import static com.dataexpo.autogate.face.manager.ImportFileManager.IMPORT_SUCCESS;
import static com.dataexpo.autogate.model.User.IMAGE_TYPE_JPG;
import static com.dataexpo.autogate.model.User.IMAGE_TYPE_PNG;

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

    //注册用户的同时保存用户的照片到指定的目录 用户实体中有图片数据
    public long insert(User user) {
        String suffix = ".jpg";
        //此处也是检测是否有重复code的用户
//        if (haveByCode(user)) {
//            //TODO: 可以存放到另一个表，手动纠正
//            Log.i(TAG, "用户： " + user.code + " 重复！！！！！");
//            return 0L;
//        }

        byte[] images = FileUtils.base64ToBytes(user.image);

        user.ctime = Utils.timeNow_();

        if (images != null && images.length > 0) {
        //if (1 == 0) {
            //若没有设置image_name则将用户code赋给image_name
            if ("".equals(user.image_name)) {
                user.image_name = user.code;
            }

            //根据数据传输中的类型判断图片类型
            if (user.image_type == IMAGE_TYPE_JPG) {
                suffix = ".jpg";
            } else if (user.image_type == IMAGE_TYPE_PNG){
                suffix = ".png";
            }
            //Log.i(TAG, "insert 3 " + (new Date()).getTime());
            String path = FileUtils.getRegistedDirectory().getPath() + "/" + user.image_name + suffix;

            FileUtils.writeByteFile(images, path);

            //Log.i(TAG, "save image path:" + path);
            //TODO : 在此进行注册用户人脸
            if (SingleBaseConfig.getBaseConfig().getLocal_sync_regist()) {
                int result = FaceApi.getInstance().registFaceByImage(user, path);
                Log.i(TAG, " registFace result: " + result);
                if (result == IMPORT_SUCCESS) {
                    user.bregist_face = 1;
                }
            }
        }
        //存入数据库
        return insertDB(user);
    }

    //注册用户的同时保存用户的照片到指定的目录 用户实体没有图片数据  图片此时存在于其他目录

    /**
     *
     * @param user 用户实体
     * @param path 导入图片的路径
     * @param suffix 后缀类型
     * @param fileName 导入的图片文件名
     * @return 返回0 注册用户成功并且注册了人脸
     *         返回1 注册用户成功，注册人脸失败
     *         返回2 注册用户失败
     */
    public long insert(User user, String path, int suffix, String fileName) {
        long res = 0;
        user.ctime = Utils.timeNow_();

        if (path != null && path.length() > 0) {
            if ("".equals(user.image_name)) {
                user.image_name = fileName;
            }

            user.image_type = suffix;

            //Log.i(TAG, "save image path:" + path);
            //TODO : 在此进行注册用户人脸
            //TODO : 注册时如果人脸识别超过阈值时不会注册人脸并且不会copy图片到注册成功用户的目录
            if (SingleBaseConfig.getBaseConfig().getLocal_sync_regist()) {
                int result = FaceApi.getInstance().registFaceByImage(user, path);
                Log.i(TAG, " registFace result: " + result);
                if (result == IMPORT_SUCCESS) {
                    user.bregist_face = 1;
                    FileUtils.copyFile(path, FileUtils.getRegistedDirectory().getPath() + "/" + fileName);
                } else {
                    user.feature = null;
                    res = 1;
                }
            }
        }
        //存入数据库
        if (insertDB(user) <= 0) {
            res = 2;
        }
        return res;
    }

    public void updateBase(User user) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", user.name);
        contentValues.put("company", user.company);
        contentValues.put("position", user.position);
        contentValues.put("cardcode", user.cardCode);
        contentValues.put("gender", user.gender);
        contentValues.put("image_name", user.image_name);
        contentValues.put("image_type", user.image_type);
        contentValues.put("update_time", user.updateTime);
        contentValues.put("userinfo", user.userInfo);
        contentValues.put("bregist_face", user.bregist_face);
        DBUtils.getInstance().modifyData(DBUtils.TABLE_USER, user.id, contentValues);
    }

    public void updateFeature(User user) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("feature", user.feature);
        contentValues.put("bregist_face", user.bregist_face);
        contentValues.put("face_token", user.getFaceToken());
        DBUtils.getInstance().modifyData(DBUtils.TABLE_USER, user.id, contentValues);
    }

    public synchronized boolean haveByCode(User user) {
        int count = DBUtils.getInstance().count("select count(*) from " + DBUtils.TABLE_USER + " where code = '" + user.code + "'");
        return count > 0;
//
//        Cursor cursor = DBUtils.getInstance().findBy(DBUtils.TABLE_USER, "code", user.code);
//        boolean result = cursor.moveToNext();
//        cursor.close();
//        return result;
    }

    private long insert(TestData data) {
        byte[] image = FileUtils.base64ToBytes(data.image);
        if (image.length > 0) {
            Log.i(TAG, "insert test data: " + data.code);
            FileUtils.writeByteFile(image, FileUtils.getRegistedDirectory().getPath() + "/" + data.code + ".jpg");
        }

        return 0l;
    }

    /**
     * 添加数据
     */
    public synchronized long insertDB(User user) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", user.name);
        contentValues.put("company", user.company);
        contentValues.put("position", user.position);
        contentValues.put("cardcode", user.cardCode);
        contentValues.put("gender", user.gender);
        contentValues.put("code", user.code);
        contentValues.put("image_name", user.image_name);
        contentValues.put("image_type", user.image_type);
        contentValues.put("ctime", user.ctime);
        contentValues.put("update_time", user.updateTime);
        contentValues.put("userinfo", user.userInfo);
        contentValues.put("bregist_face", user.bregist_face);
        contentValues.put("feature", user.feature);
        contentValues.put("face_token", user.getFaceToken());
        //Log.i(TAG, user.feature.length + " is length !!!!!!");
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
            User user_response = resolve(cursor);
            if (user_response != null) {
                list.add(user_response);
            }
        }

        cursor.close();
        return list;
    }

    public ArrayList<User> findUserNoFaceRegist() {
        ArrayList<User> list = new ArrayList<>();
        Cursor cursor = DBUtils.getInstance().listAll(DBUtils.TABLE_USER, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            User user_response = resolve(cursor);
            if (user_response != null && user_response.bregist_face != 0) {
                list.add(user_response);
            }
        }

        cursor.close();
        return list;
    }

    public User findUserByCode(User user) {
        Cursor cursor = DBUtils.getInstance().findBy(DBUtils.TABLE_USER, "code", user.code);
        User user_response = null;

        if (cursor != null && cursor.moveToNext()) {
            user_response = resolve(cursor);
            cursor.close();
        }
        return user_response;
    }

    public User findUserById(int id) {
        Cursor cursor = DBUtils.getInstance().findBy(DBUtils.TABLE_USER, "id", id + "");
        User user_response = null;

        if (cursor != null && cursor.moveToNext()) {
            user_response = resolve(cursor);
            cursor.close();
        }
        return user_response;
    }

    public User findUserByCardCode(User user) {
        Cursor cursor = DBUtils.getInstance().findBy(DBUtils.TABLE_USER, "cardcode", user.cardCode);
        User user_response = null;

        if (cursor.moveToNext()) {
            user_response = resolve(cursor);
        }
        cursor.close();
        return user_response;
    }

    private User resolve(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        User user_response = new User();
        user_response.id = cursor.getInt(cursor.getColumnIndex("id"));
        user_response.name = cursor.getString(cursor.getColumnIndex("name"));
        user_response.company = cursor.getString(cursor.getColumnIndex("company"));
        user_response.position = cursor.getString(cursor.getColumnIndex("position"));
        user_response.cardCode = cursor.getString(cursor.getColumnIndex("cardcode"));
        user_response.gender = cursor.getInt(cursor.getColumnIndex("gender"));
        user_response.code = cursor.getString(cursor.getColumnIndex("code"));
        user_response.image_name = cursor.getString(cursor.getColumnIndex("image_name"));
        user_response.image_type = cursor.getInt(cursor.getColumnIndex("image_type"));
        user_response.ctime = cursor.getLong(cursor.getColumnIndex("ctime"));
        user_response.updateTime = cursor.getLong(cursor.getColumnIndex("update_time"));
        user_response.userInfo = cursor.getString(cursor.getColumnIndex("userinfo"));
        user_response.bregist_face = cursor.getInt(cursor.getColumnIndex("bregist_face"));
        user_response.feature = cursor.getBlob(cursor.getColumnIndex("feature"));
        user_response.setFaceToken(cursor.getString(cursor.getColumnIndex("face_token")));
        return user_response;
    }
}

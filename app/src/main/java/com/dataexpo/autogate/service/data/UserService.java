package com.dataexpo.autogate.service.data;

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
import com.dataexpo.autogate.model.service.UserEntityVo;

import java.util.ArrayList;
import java.util.List;

import static com.dataexpo.autogate.comm.Utils.EXPO_ID;
import static com.dataexpo.autogate.face.manager.ImportFileManager.IMPORT_SUCCESS;
import static com.dataexpo.autogate.model.User.IMAGE_TYPE_JPG;
import static com.dataexpo.autogate.model.User.IMAGE_TYPE_PNG;
import static com.dataexpo.autogate.model.User.IMG_SYNC_CHANGE;
import static com.dataexpo.autogate.model.User.IMG_SYNC_END;
import static com.dataexpo.autogate.model.User.IMG_SYNC_NONE;

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
        return insertDB(user, null);
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
                    return res;
                }
            }
        }
        //存入数据库
        if (insertDB(user, null) <= 0) {
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
        contentValues.put("auth", user.auth);
        DBUtils.getInstance().modifyData(DBUtils.TABLE_USER, user.id, contentValues);
    }

    public void updateFromServer(User user) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", user.name);
        contentValues.put("company", user.company);
        contentValues.put("position", user.position);
        contentValues.put("cardcode", user.cardCode);
        contentValues.put("code", user.code);
        contentValues.put("image_base64", user.image_base64);
        contentValues.put("image_sync", user.image_sync);
        contentValues.put("update_time", Utils.timeNow_());
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
    public synchronized long insertDB(User user, Integer expoId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("pid", user.pid);
        contentValues.put("expoId", expoId);
        contentValues.put("name", user.name);
        contentValues.put("company", user.company);
        contentValues.put("position", user.position);
        contentValues.put("cardcode", user.cardCode);
        contentValues.put("gender", user.gender);
        contentValues.put("code", user.code);
        contentValues.put("image_name", user.image_name);
        contentValues.put("image_base64", user.image_base64);
        contentValues.put("image_type", user.image_type);
        contentValues.put("image_sync", user.image_sync);
        contentValues.put("ctime", user.ctime);
        contentValues.put("update_time", user.updateTime);
        contentValues.put("userinfo", user.userInfo);
        contentValues.put("bregist_face", user.bregist_face);
        contentValues.put("feature", user.feature);
        contentValues.put("face_token", user.getFaceToken());
        contentValues.put("auth", user.auth);
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

    public ArrayList<User> limitList(int start, int size) {
        ArrayList<User> list = new ArrayList<>();
        Cursor cursor = DBUtils.getInstance().limitWith(DBUtils.TABLE_USER, start, size);
        try {
            while (cursor.moveToNext()) {
                User user = resolve(cursor);
                if (user != null) {
                    list.add(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
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

        if (cursor == null) {
            return user_response;
        }

        try {
            if (cursor.moveToNext()) {
                user_response = resolve(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        return user_response;
    }

    public User findUserById(int id) {
        Cursor cursor = DBUtils.getInstance().findBy(DBUtils.TABLE_USER, "id", id + "");
        User user_response = null;

        if (cursor == null) {
            return user_response;
        }

        try {
            if (cursor.moveToNext()) {
                user_response = resolve(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        return user_response;
    }

    public User findUserByCardCode(User user) {
        Cursor cursor = DBUtils.getInstance().findBy(DBUtils.TABLE_USER, "cardcode", user.cardCode);
        User user_response = null;

        try {
            if (cursor.moveToNext()) {
                user_response = resolve(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        return user_response;
    }

    private User resolve(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        User user_response = new User();
        user_response.id = cursor.getInt(cursor.getColumnIndex("id"));
        user_response.pid = cursor.getInt(cursor.getColumnIndex("pid"));
        user_response.expoId = cursor.getInt(cursor.getColumnIndex("expoId"));
        user_response.name = cursor.getString(cursor.getColumnIndex("name"));
        user_response.company = cursor.getString(cursor.getColumnIndex("company"));
        user_response.position = cursor.getString(cursor.getColumnIndex("position"));
        user_response.cardCode = cursor.getString(cursor.getColumnIndex("cardcode"));
        user_response.gender = cursor.getInt(cursor.getColumnIndex("gender"));
        user_response.code = cursor.getString(cursor.getColumnIndex("code"));
        user_response.image_name = cursor.getString(cursor.getColumnIndex("image_name"));
        user_response.image_base64 = cursor.getString(cursor.getColumnIndex("image_base64"));
        user_response.image_type = cursor.getInt(cursor.getColumnIndex("image_type"));
        user_response.image_sync = cursor.getInt(cursor.getColumnIndex("image_sync"));
        user_response.ctime = cursor.getLong(cursor.getColumnIndex("ctime"));
        user_response.updateTime = cursor.getLong(cursor.getColumnIndex("update_time"));
        user_response.userInfo = cursor.getString(cursor.getColumnIndex("userinfo"));
        user_response.bregist_face = cursor.getInt(cursor.getColumnIndex("bregist_face"));
        user_response.feature = cursor.getBlob(cursor.getColumnIndex("feature"));
        user_response.setFaceToken(cursor.getString(cursor.getColumnIndex("face_token")));
        user_response.auth = cursor.getInt(cursor.getColumnIndex("auth"));
        return user_response;
    }

    public List<User> findUserByEuId(String minId, String maxId) {
        Log.i(TAG, "min: " + minId + " " + maxId);
        Cursor cursor = DBUtils.getInstance().listAll(DBUtils.TABLE_USER, null,
                "pid >= ? and pid <= ?", new String[]{minId, maxId}, null, null, null);
//        Cursor cursor = DBUtils.getInstance().rowSelect("select * from gate_user where pid>=? and pid<=?",
//                new String[]{minId, maxId});

        List<User> users = new ArrayList<>();
        User user_response = null;

        while (cursor.moveToNext()) {
            user_response = resolve(cursor);
            users.add(user_response);
        }
        cursor.close();
        return users;
    }

    /**
     * 添加来自服务器的数据
     * @param sUpdateList
     */
    public void addDataFromService(List<UserEntityVo> sUpdateList, Integer expoId) {
        DBUtils.getInstance().beginTransaction();
        try {
            User user;
            for (UserEntityVo ue: sUpdateList) {
                user = new User();

                if (ue.getImageBase64() != null && !"".equals(ue.getImageBase64())) {
                    user.image_sync = IMG_SYNC_CHANGE;
                }

                ServerUserToLocal(ue, user);
                insertDB(user, expoId);
            }
            DBUtils.getInstance().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtils.getInstance().endTransaction();
        }
    }

    /**
     * 修改服务器变更的数据
     * @param updateList
     */
    public void updateFromService(List<User> updateList) {
        for (User u: updateList) {
            updateFromServer(u);
        }
    }

    /**
     * 删除服务器中已不存在的数据
     * @param users
     */
    public void removeServiceNoExist(List<User> users) {
        for (User u: users) {
            DBUtils.getInstance().delData(DBUtils.TABLE_USER, u.id);
        }
    }

    /**
     * 删除全部
     */
    public int removeAll() {
        return DBUtils.getInstance().delDataAll(DBUtils.TABLE_USER);
    }

    /**
     * 删除所有不在这个项目的数据
     * @param parseInt
     */
    public void removeAllByExpoId(int parseInt) {
        DBUtils.getInstance().delDataBy(DBUtils.TABLE_USER, "expoId != ?", new String[]{parseInt + ""});
    }

    /**
     * 获取一个未同步人像的数据
     */
    public User findNoSyncImgOne() {
        Cursor cursor = DBUtils.getInstance().rowQuery("select * from gate_user where image_sync = ? limit 1", new String[]{"1"});
        User user_response = null;

        try {
            if (cursor.moveToNext()) {
                user_response = resolve(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return user_response;
    }

    /**
     * 获取本地用户数据总数
     */
    public int countLocal() {
        return DBUtils.getInstance().count("select count(id) from " + DBUtils.TABLE_USER);
    }

    public int countOfImg() {
        return FileUtils.getImgCount();
    }

    /**
     * 获取未同步人像的数量
     * @return
     */
    public int countImageLocalNoSync() {
        return DBUtils.getInstance().count("select count(id) from " +
                DBUtils.TABLE_USER + " where image_sync = ?", new String[]{"1"});
    }

    /**
     * 取消同步图片状态
     * @param startEuId
     */
    public int updateToNoSyncImg(Integer startEuId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("image_sync", 0);
        return DBUtils.getInstance().update(DBUtils.TABLE_USER, contentValues, "pid = ?", new String[]{startEuId+ ""});
    }

    /**
     * 情况所有人像
     */
    public void clearAllImg() {
        FileUtils.clearImg();
    }

    /**
     * 取消同步图片状态
     * @param startEuId
     */
    public int updateToSyncOk(Integer startEuId, String imageName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("image_sync", 2);
        contentValues.put("image_name", imageName);
        return DBUtils.getInstance().update(DBUtils.TABLE_USER, contentValues, "pid = ?", new String[]{startEuId+ ""});
    }

    public int updateAllImgNoSync() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("image_sync", 0);
        return DBUtils.getInstance().update(DBUtils.TABLE_USER, contentValues, null, null);
    }

    /**
     * 把服务器数据转化为本地数据
     * @param ue
     * @param user
     */
    private void ServerUserToLocal(UserEntityVo ue, User user) {
        user.pid = ue.getEuId();
        user.name = ue.getName();
        user.company = ue.getCompany();
        user.position = ue.getPosition();
        user.cardCode = ue.getCardCode();
        user.code = ue.getEucode();
    }

    /**
     * 检查服务器数据和本地数据是否一致
     * @param u
     * @param ue
     * @return
     */
    public boolean compareUser(User u, UserEntityVo ue) {
        boolean result = true;
        if (ue.getName() != null && !"".equals(ue.getName()) && !ue.getName().equals(u.name)) {
            u.name = ue.getName();
            result = false;
        }

        if (ue.getCompany() != null && !"".equals(ue.getCompany()) && !ue.getCompany().equals(u.company)) {
            u.company = ue.getCompany();
            result = false;
        }

        if (ue.getPosition() != null && !"".equals(ue.getPosition()) && !ue.getPosition().equals(u.position)) {
            u.position = ue.getPosition();
            result = false;
        }

        if (ue.getCardCode() != null && !"".equals(ue.getCardCode()) && !ue.getCardCode().equals(u.cardCode)) {
            u.cardCode = ue.getCardCode();
            result = false;
        }

        if (ue.getEucode() != null && !"".equals(ue.getEucode()) && !ue.getEucode().equals(u.code)) {
            u.code = ue.getEucode();
            result = false;
        }

        //Log.i(TAG, " " + ue.getImageBase64());

        //服务器端存在图片
        if (ue.getImageBase64() != null && !"".equals(ue.getImageBase64())) {
            //Log.i(TAG, "compareUser" + ue.getImageBase64());
            //本地无图片 或者本地图片和服务器图片base64不一致
            if (u.image_sync == IMG_SYNC_NONE || !ue.getImageBase64().equals(u.image_base64)) {
                u.image_base64 = ue.getImageBase64();
                u.image_name = "";
                u.image_sync = IMG_SYNC_CHANGE;
                result = false;
            }
        }

        //如果本地有，服务器没有，那也要删除
        if (ue.getImageBase64() != null && "".equals(ue.getImageBase64())) {
            if (u.image_sync == IMG_SYNC_END) {
                if (FileUtils.isFileExist(FileUtils.getUserPic(ue.getEucode())) != null) {
                    FileUtils.deleteFile(FileUtils.getUserPic(ue.getEucode()));
                }
            }
        }

        return result;
    }
}

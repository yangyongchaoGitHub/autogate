package com.dataexpo.autogate.face.api;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;


import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.model.Feature;
import com.dataexpo.autogate.comm.DBUtils;
import com.dataexpo.autogate.face.manager.FaceSDKManager;
import com.dataexpo.autogate.face.manager.ImportFileManager;
import com.dataexpo.autogate.face.model.Group;
import com.dataexpo.autogate.face.service.FaceGroupService;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FaceApi {
    private static final String TAG = FaceApi.class.getSimpleName();
    private static FaceApi instance;
    private ExecutorService es = Executors.newSingleThreadExecutor();
    private Future future;

    private int mUserNum;
    private boolean isinitSuccess = false;


    private FaceApi() {

    }

    public static synchronized FaceApi getInstance() {
        if (instance == null) {
            instance = new FaceApi();
        }
        return instance;
    }

//    /**
//     * 添加用户组
//     */
//    public boolean groupAdd(Group group) {
//        if (group == null || TextUtils.isEmpty(group.getGroupId())) {
//            return false;
//        }
//        Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
//        Matcher matcher = pattern.matcher(group.getGroupId());
//        if (!matcher.matches()) {
//            return false;
//        }
//        boolean ret = FaceGroupService.getInstance().addGroup(group);
//
//        return ret;
//    }

//    /**
//     * 查询用户组（默认最多取1000个组）
//     */
//    public List<Group> getGroupList(int start, int length) {
//        if (start < 0 || length < 0) {
//            return null;
//        }
//        if (length > 1000) {
//            length = 1000;
//        }
//        List<Group> groupList = DBManager.getInstance().queryGroups(start, length);
//        return groupList;
//    }
//
//    /**
//     * 根据groupId查询用户组
//     */
//    public List<Group> getGroupListByGroupId(String groupId) {
//        if (TextUtils.isEmpty(groupId)) {
//            return null;
//        }
//        return DBManager.getInstance().queryGroupsByGroupId(groupId);
//    }
//
//    /**
//     * 根据groupId删除用户组
//     */
//    public boolean groupDelete(String groupId) {
//        if (TextUtils.isEmpty(groupId)) {
//            return false;
//        }
//        boolean ret = DBManager.getInstance().deleteGroup(groupId);
//        return ret;
//    }
//
//    /**
//     * 添加用户
//     */
//    public boolean userAdd(User user) {
//        if (user == null || TextUtils.isEmpty(user.getGroupId())) {
//            return false;
//        }
//        Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
//        Matcher matcher = pattern.matcher(user.getUserId());
//        if (!matcher.matches()) {
//            return false;
//        }
//        boolean ret = DBManager.getInstance().addUser(user);
//
//        return ret;
//    }
//
//    /**
//     * 根据groupId查找用户
//     */
//    public List<User> getUserList(String groupId) {
//        if (TextUtils.isEmpty(groupId)) {
//            return null;
//        }
//        List<User> userList = DBManager.getInstance().queryUserByGroupId(groupId);
//        return userList;
//    }
//
//    /**
//     * 根据groupId、userName查找用户
//     */
//    public List<User> getUserListByUserName(String groupId, String userName) {
//        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(userName)) {
//            return null;
//        }
//        List<User> userList = DBManager.getInstance().queryUserByUserName(groupId, userName);
//        return userList;
//    }
//
//    /**
//     * 根据_id查找用户
//     */
//    public User getUserListById(int _id) {
//        if (_id < 0) {
//            return null;
//        }
//        List<User> userList = DBManager.getInstance().queryUserById(_id);
//        if (userList != null && userList.size() > 0) {
//            return userList.get(0);
//        }
//        return null;
//    }

//    /**
//     * 更新用户
//     */
//    public boolean userUpdate(User user) {
//        if (user == null) {
//            return false;
//        }
//
//        boolean ret = DBManager.getInstance().updateUser(user);
//        return ret;
//    }

//    /**
//     * 更新用户
//     */
//    public boolean userUpdate(String groupId, String userName, String imageName, byte[] feature) {
//        if (groupId == null || userName == null || imageName == null || feature == null) {
//            return false;
//        }
//
//        boolean ret = DBManager.getInstance().updateUser(groupId, userName, imageName, feature);
//        return ret;
//    }

//    /**
//     * 删除用户
//     */
//    public boolean userDelete(String userId, String groupId) {
//        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(groupId)) {
//            return false;
//        }
//
//        boolean ret = DBManager.getInstance().deleteUser(userId, groupId);
//        return ret;
//    }

    /**
     * 是否是有效姓名
     *
     * @param username 用户名
     * @return 有效或无效信息
     */
    public String isValidName(String username) {
        if (username == null || "".equals(username.trim())) {
            return "姓名为空";
        }

        // 姓名过长
        if (username.length() > 10) {
            return "姓名过长";
        }

        // 含有特殊符号
        String regex = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）—"
                + "—+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(username);
        if (m.find()) {
            return "姓名中含有特殊符号";
        }
        return "0";
    }

    /**
     * 提取特征值
     */
    public float getFeature(Bitmap bitmap, byte[] feature, BDFaceSDKCommon.FeatureType featureType) {
        if (bitmap == null) {
            return -1;
        }

        BDFaceImageInstance imageInstance = new BDFaceImageInstance(bitmap);
        // 最大检测人脸，获取人脸信息
        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetect()
                .detect(BDFaceSDKCommon.DetectType.DETECT_VIS, imageInstance);
        float ret = -1;
        if (faceInfos != null && faceInfos.length > 0) {
            FaceInfo faceInfo = faceInfos[0];
            // 人脸识别，提取人脸特征值
            ret = FaceSDKManager.getInstance().getFaceFeature().feature(
                    featureType, imageInstance,
                    faceInfo.landmarks, feature);
        }
        imageInstance.destory();
        return ret;
    }

    public boolean updateUserInfoByRegisterFace(User user) {
        UserService.getInstance().updateFeature(user);
        return true;
    }
//
//    public boolean registerUserIntoDBmanager(String groupName, String userName, String picName,
//                                             String userInfo, byte[] faceFeature) {
//        boolean isSuccess = false;
//
//        Group group = new Group();
//        group.setGroupId(groupName);
//
//        User user = new User();
//        user.setGroupId(groupName);
//        /*
//         * 用户id（由数字、字母、下划线组成），长度限制128B
//         * uid为用户的id,百度对uid不做限制和处理，应该与您的帐号系统中的用户id对应。
//         */
//        final String uid = UUID.randomUUID().toString();
//        user.setUserId(uid);
//        user.setUserName(userName);
//        user.setFeature(faceFeature);
//        user.setImageName(picName);
//        if (userInfo != null) {
//            user.setUserInfo(userInfo);
//        }
//        // 添加用户信息到数据库
//        boolean importUserSuccess = FaceApi.getInstance().userAdd(user);
//        if (importUserSuccess) {
//            // 如果添加到数据库成功，则添加用户组信息到数据库
//            // 如果当前图片组名和上一张图片组名相同，则不添加数据库到组表
//            if (FaceApi.getInstance().groupAdd(group)) {
//                isSuccess = true;
//            } else {
//                isSuccess = false;
//            }
//        } else {
//            isSuccess = false;
//        }
//
//        return isSuccess;
//    }

    /**
     * 获取底库数量
     *
     * @return
     */
    public int getmUserNum() {
        return mUserNum;
    }

    public boolean isinitSuccess() {
        return isinitSuccess;
    }

    public int registFaceByImage(User user, String path) {
        return ImportFileManager.getInstance().importByImage(user, path);
    }

    public int registFaceByBitmap(User user, Bitmap bitmap) {
        return ImportFileManager.getInstance().importByBitMap(user, bitmap);
    }

    /**
     * 数据库发现变化时候，重新把数据库中的人脸信息添加到内存中，id+feature
     */
    public void initDatabases(final boolean isFeaturePush) {
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }

        isinitSuccess = false;
        future = es.submit(new Runnable() {
            @Override
            public void run() {
                ArrayList<Feature> features = new ArrayList<>();
                List<User> listUser = UserService.getInstance().listAll();
                Log.i(TAG, " all user size: " + listUser.size());
                for (int j = 0; j < listUser.size(); j++) {
                    if (listUser.get(j).feature.length > 0) {
                        Log.i(TAG, "loop 1 ");
                        Feature feature = new Feature();
                        Log.i(TAG, "loop 2 ");
                        feature.setId(listUser.get(j).id);
                        Log.i(TAG, "loop 3 ");
                        feature.setFeature(listUser.get(j).feature);
                        Log.i(TAG, "loop 4 ");
                        features.add(feature);
                        Log.i(TAG, "loop 5 ");
//                    String lo = "";
//                    for (int i = 0; i<100; i++) {
//                        lo += listUser.get(j).feature[i] + " ";
//                    }
                        Log.i(TAG, "after ");
                    }
                }
                if (isFeaturePush && features.size() > 0) {
                    Log.i(TAG, "features size : " + features.size());
                    Log.i(TAG, FaceSDKManager.getInstance().getFaceFeature().featurePush(features) + " push result");
                }

                mUserNum = features.size();
                isinitSuccess = true;
            }
        });
    }

//    //获取所有图片
//    public List getAllPics(Context context) {
//        List<String> list = new ArrayList<>();
//        ContentResolver contentResolver = context.getContentResolver();
//        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//        //Cursor cursor = contentResolver.query(Uri.parse("/storage/emulated/0/DCIM/Camera"),
//                new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA},
//                MediaStore.Images.Media.MIME_TYPE + "=? or " +
//                        MediaStore.Images.Media.MIME_TYPE + "=?",
//                new String[]{"image/jpeg", "image/png"},
//                MediaStore.Images.Media._ID + " DESC");
//
//        if (cursor != null){
//            while (cursor.moveToNext()) {
//                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
//                list.add(path);
//
//                LogUtils.i(TAG, "path is:" + path);
//            }
//            cursor.close();
//        }
//        return list;
//    }
}

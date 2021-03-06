package com.dataexpo.autogate.face.manager;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.face.api.FaceApi;
import com.dataexpo.autogate.face.listener.OnImportListener;
import com.dataexpo.autogate.face.model.LivenessModel;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.service.MainApplication;
import com.dataexpo.autogate.service.data.UserService;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.dataexpo.autogate.model.User.IMAGE_TYPE_JPG;
import static com.dataexpo.autogate.model.User.IMAGE_TYPE_PNG;
import static com.dataexpo.autogate.service.MainApplication.IMPORT_LOCALFILE;
import static com.dataexpo.autogate.service.MainApplication.IMPORT_MQTT;

/**
 * 导入相关管理类
 */

public class ImportFileManager {
    private static final String TAG = "ImportFileManager";
    public static final String GROUP_DEFAULT = "default";

    //导入成功
    public static final int IMPORT_SUCCESS = 0;
    //文件不存在
    public static final int IMPORT_FILE_EMPTY = 1;
    //监测人脸数据失败，可能原因人脸图像太小
    public static final int IMPORT_FEATURE_FAIL = 2;
    //没有找到人脸
    public static final int IMPORT_NOFACE = 3;
    //重复的导入
    public static final int IMPORT_REPEAT = 4;

    private Future mFuture;
    private ExecutorService mExecutorService;
    private OnImportListener mImportListener;
    // 是否需要导入
    private volatile boolean mIsNeedImport;

    private int mTotalCount;
    private int mFinishCount;
    private int mSuccessCount;
    private int mFailCount;

    private static class HolderClass {
        private static final ImportFileManager instance = new ImportFileManager();
    }

    public static ImportFileManager getInstance() {
        return ImportFileManager.HolderClass.instance;
    }

    // 私有构造，实例化ExecutorService
    private ImportFileManager() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }
    }

    public void setOnImportListener(OnImportListener importListener) {
        mImportListener = importListener;
    }

    /**
     * 开始批量导入
     */
    public void batchImport() {
        // 获取导入目录 /sdcard/Face-Import
        File batchFaceDir = FileUtils.getBatchImportDirectory();
        // 遍历该目录下的所有文件
        String[] files = batchFaceDir.list();
        if (files == null || files.length == 0) {
            Log.i(TAG, "导入数据的文件夹没有数据");
            if (mImportListener != null) {
                mImportListener.showToastMessage("导入数据的文件夹没有数据");
            }
            return;
        }

        // 判断Face.zip是否存在
        File zipFile = FileUtils.isFileExist(batchFaceDir.getPath(), "ag-import.zip");
        if (zipFile == null) {
            Log.i(TAG, "导入数据的文件夹没有ag-import.zip");
            if (mImportListener != null) {
                mImportListener.showToastMessage("搜索失败，请检查操作步骤并重试");
            }
            return;
        }

        // 开启线程解压、导入
        asyncImport(batchFaceDir, zipFile);
    }


    /**
     *
     * @param bitmap 图片路径
     * @return 返回注册结果
     * 通过图片进行人脸注册
     */
    public int importByBitMap(User user, Bitmap bitmap) {
        int result = IMPORT_FILE_EMPTY;
        float ret;

        if (bitmap != null) {
            byte[] bytes = new byte[512];

            // 走人脸SDK接口，通过人脸检测、特征提取拿到人脸特征值
            ret = FaceApi.getInstance().getFeature(bitmap, bytes,
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);

            if (ret == -1) {
                Log.e(TAG, "：未检测到人脸，可能原因：人脸太小 ");
                //mImportListener.showToastMessage("导入数据的文件夹没有数据");
                result = IMPORT_FEATURE_FAIL;

            } else if (ret == 128f) {
                if (faceImport(bitmap, bytes)) {
                    user.feature = bytes;
                    user.bregist_face = IMPORT_SUCCESS;
                    result = IMPORT_SUCCESS;
                    String lo = "";
                    for (int i = 0; i<200; i++) {
                        lo += user.feature[i] + " ";
                    }
                    Log.i(TAG, "befor " + lo);

                    boolean importDBSuccess = FaceApi.getInstance().updateUserInfoByRegisterFace(user);
                    Log.i(TAG, "update feature! end " + importDBSuccess);
                } else {
                    result = IMPORT_REPEAT;
                }
            } else {
                result = IMPORT_NOFACE;
            }

            // 图片回收
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        return result;
    }

    /**
     *
     * @param imagePath 图片路径
     * @return 返回注册结果
     * 通过图片进行人脸注册
     */
    public int importByImage(User user, String imagePath) {
        int result = IMPORT_FILE_EMPTY;
        float ret;

        // 根据图片的路径将图片转成Bitmap
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

        if (bitmap != null) {
            byte[] bytes = new byte[512];

            // 走人脸SDK接口，通过人脸检测、特征提取拿到人脸特征值
            ret = FaceApi.getInstance().getFeature(bitmap, bytes,
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);

            if (ret == -1) {
                Log.e(TAG, "：未检测到人脸，可能原因：人脸太小 ");
                //mImportListener.showToastMessage("导入数据的文件夹没有数据");
                result = IMPORT_FEATURE_FAIL;

            } else if (ret == 128f) {
                if (faceImport(bitmap, bytes)) {
                    user.feature = bytes;
                    user.bregist_face = IMPORT_SUCCESS;
                    result = IMPORT_SUCCESS;
                    String lo = "";
                    for (int i = 0; i<200; i++) {
                        lo += user.feature[i] + " ";
                    }
                    Log.i(TAG, "befor " + lo);

                    //boolean importDBSuccess = FaceApi.getInstance().updateUserInfoByRegisterFace(user);
                    //Log.i(TAG, "update feature! end " + importDBSuccess);
                } else {
                    result = IMPORT_REPEAT;
                }
            } else {
                result = IMPORT_NOFACE;
            }

            // 图片回收
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        return result;
    }
//    /**
//     * 通过选择相册批量导入用户
//     *
//     */
//    public void asyncImportByGallery(final List<String> images) {
//        mFinishCount = 0;
//        mSuccessCount = 0;
//        mFailCount = 0;
//
//        if (mExecutorService == null) {
//            mExecutorService = Executors.newSingleThreadExecutor();
//        }
//
//        mFuture = mExecutorService.submit(new Runnable() {
//            @Override
//            public void run() {
//                String userName;
//                float ret;
//                boolean importDBSuccess;
//                String picName;
//                boolean mSuccess = false;
//                final int total = images.size();
//
//                for (int i = 0; i < images.size(); i++) {
//                    mFinishCount++;
//                    mFailCount++;
//                    // 根据图片的路径将图片转成Bitmap
//                    Bitmap bitmap = BitmapFactory.decodeFile(images.get(i));
//
//                    //使用系统时间作为用户名
//                    userName = Utils.timeNow();
//                    picName = userName + ".jpg";
//
//                    if (bitmap != null) {
//                        byte[] bytes = new byte[512];
//
//                        // 走人脸SDK接口，通过人脸检测、特征提取拿到人脸特征值
//                        ret = FaceApi.getInstance().getFeature(bitmap, bytes,
//                                BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);
//
//                        Log.i(TAG, "live_photo gallery = " + ret);
//
//                        if (ret == -1) {
//                            LogUtils.e(TAG, "：未检测到人脸，可能原因：人脸太小 ");
//                            //mImportListener.showToastMessage("导入数据的文件夹没有数据");
//
//                        } else if (ret == 128f) {
//                            mSuccess = faceImport(bitmap, userName, picName, null, bytes);
//
//                        } else {
//                            LogUtils.e(TAG, "：未检测到人脸");
//                        }
//
//                        //计数
//                        if (mSuccess) {
//                            mFailCount--;
//                            mSuccessCount++;
//                        }
//
//                        Log.i(TAG, "FinishCount: " + mFinishCount + " successCount: " + mSuccessCount + " failcount: " + mFailCount);
//
//                        if (mImportListener != null) {
//                            mImportListener.onImporting(mFinishCount, mSuccessCount, mFailCount, total);
//                        }
//
//                        // 图片回收
//                        if (!bitmap.isRecycled()) {
//                            bitmap.recycle();
//                        }
//
//                    } else {
//                        LogUtils.e(TAG, "：该图片转成Bitmap失败");
//                    }
//                }
//            }
//        });
//    }

    /**
     * 开启线程解压、导入
     *
     * @param batchFaceDir 导入的目录
     * @param zipFile      压缩文件
     */
    private void asyncImport(final File batchFaceDir, final File zipFile) {
        mIsNeedImport = true;
        mFinishCount = 0;
        mSuccessCount = 0;
        mFailCount = 0;

        if (mExecutorService == null) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }

        mFuture = mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                //TODO: 此处代码应在UI界面进行判断
                //设置导入状态，避免mqtt和本地导入同时进行导致不必要的数据冲突
                if (MainApplication.getInstance().getImportStatus() == IMPORT_MQTT) {
                    //发出当前正在通过MQTT接受用户数据，请求是否进行本地导入
                    MainApplication.getInstance().setImportStatus(IMPORT_LOCALFILE);
                }

                try {
                    if (mImportListener != null) {
                        mImportListener.startUnzip();
                    }

                    // 解压
                    boolean zipSuccess = FileUtils.unZipFolder(zipFile.getAbsolutePath(), batchFaceDir.toString());
                    if (!zipSuccess) {
                        if (mImportListener != null) {
                            mImportListener.showToastMessage("解压失败");
                        }
                        return;
                    }

                    // 删除zip文件
//                    FileUtils.deleteFile(zipFile.getPath());
//                    Log.i(TAG, "解压成功");

                    // 解压成功之后再次遍历该目录是不是存在文件
                    File batchPicDir = FileUtils.getBatchImportDirectory();
                    File[] files = batchPicDir.listFiles();

                    // 如果该目录下没有文件，则提示获取图片失败
                    if (files == null) {
                        if (mImportListener != null) {
                            mImportListener.showToastMessage("获取图片失败，不存在图片文件");
                        }
                        return;
                    }

                    // 如果该目录下有文件，则判断该文件是目录还是文件
                    File[] picFiles;   // 定义图片文件数组

                    File batchPicDirData = FileUtils.getBatchImportDatas();
                    picFiles = batchPicDirData.listFiles();
//                    if (files[0].isDirectory()) {
//                        picFiles = files[0].listFiles();
//                    } else {
//                        picFiles = files;
//                    }

                    // 解压并读取图片成功，开始显示进度条
                    if (mImportListener != null) {
                        mImportListener.showProgressView();
                    }

                    if (picFiles == null) {
                        return;
                    }

                    mTotalCount = picFiles.length;

                    for (File picFile : picFiles) {
                        if (!mIsNeedImport) {
                            break;
                        }

                        //图片后缀
                        int suffix = -1;
                        // 获取图片名
                        String picName = picFile.getName();
                        // 判断图片后缀
                        Log.i(TAG, "-----------------pic name: " +picName);
                        if (picName.endsWith(".jpg")) {
                            suffix = IMAGE_TYPE_JPG;
                            Log.i(TAG, " ---------------end with jpg!!!!");
                        } else if (picName.endsWith(".png")) {
                            suffix = IMAGE_TYPE_PNG;
                        }

                        if (suffix == -1) {
                            Log.i(TAG, "图片后缀不满足要求");
                            mFinishCount++;
                            mFailCount++;
                            // 更新进度
                            updateProgress(mFinishCount, mSuccessCount, mFailCount,
                                    ((float) mFinishCount / (float) mTotalCount));
                            continue;
                        }

                        // 获取不带后缀的图片名
                        String picNameNoEx = FileUtils.getFileNameNoEx(picName);
                        // 通过既定的图片名格式，按照“-”分割，获取组名和用户名
                        String[] picNames = picNameNoEx.split("-");
                        // 如果分割失败，则该图片命名不满足要求
                        if (picNames.length != 5) {
                            //Log.e(TAG, "图片命名格式不符合要求");
                            if (mImportListener != null) {
                                mImportListener.showToastMessage("图片命名格式不符合要求");
                            }
                            mFinishCount++;
                            mFailCount++;
                            continue;
                        }

                        User user = new User(picNames[0], picNames[1], picNames[2], picNames[3], picNames[4]);

                        User localUser = UserService.getInstance().findUserByCode(user);
                        if (localUser != null) {
                            Log.i(TAG, "用户： " + user.code + " 重复！！！！！");
                            continue;
                        }

                        long success = UserService.getInstance().insert(user, picFile.getAbsolutePath(), suffix, picName);

                        // 判断成功与否
                        if (success == 0) {
                            mSuccessCount++;
                            //删除指定目录中的原图片文件  因为已经复制到指定的注册用户目录
                            FileUtils.deleteFile(picFile.getAbsolutePath());

                        } else if (success == 1) {
                            mSuccessCount++;
                            Log.e(TAG, "人脸注册失败图片:" + picName);
                        } else {
                            mFailCount++;
                            Log.e(TAG, "失败图片:" + picName);
                        }
                        mFinishCount++;
                        // 导入中（用来显示进度）
                        Log.i(TAG, "mFinishCount = " + mFinishCount
                                + " progress = " + ((float) mFinishCount / (float) mTotalCount));
                        if (mImportListener != null) {
                            mImportListener.onImporting(mFinishCount, mSuccessCount, mFailCount,
                                    ((float) mFinishCount / (float) mTotalCount));
                        }
                    }

                    // 导入完成
                    if (mImportListener != null) {
                        mImportListener.showToastMessage("inject ok!!!! ");

                        mImportListener.endImport(mFinishCount, mSuccessCount, mFailCount);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "exception = " + e.getMessage());
                    e.printStackTrace();
                }
                //重置人脸库
                FaceApi.getInstance().initDatabases(true);
            }
        });
    }

    /**
     *  人脸模板特这值获取成功
     *
     *  检测是否存在重复
     * @param bitmap  图片实体
     * @param bytes 特征值
     *
     */
    private boolean faceImport(Bitmap bitmap, byte[] bytes) {
        BDFaceImageInstance imageInstance = new BDFaceImageInstance(bitmap);
        LivenessModel livenessModel = new LivenessModel();
        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetect()
                .track(BDFaceSDKCommon.DetectType.DETECT_VIS, imageInstance);

        if (faceInfos!= null && faceInfos.length > 0) {
            Log.i(TAG, "faceImport getFaceDetect count " + faceInfos.length);
        } else {
            Log.i(TAG, "注册时获取特征值出错");
            return false;
        }

        FaceSDKManager.getInstance().onFeatureCheck(imageInstance, faceInfos[0].landmarks,
                livenessModel, 3);

        if (livenessModel.getUser() != null) {
            Log.i(TAG, "人脸库已存在该用户");
        } else {
            Log.i(TAG, "人脸库无此用户,可注册");
            return true;
        }
        return false;
    }


    private void updateProgress(int finishCount, int successCount, int failureCount, float progress) {
        if (mImportListener != null) {
            mImportListener.onImporting(finishCount, successCount, failureCount, progress);
        }
    }

    /**
     * 释放功能，用于关闭线程操作
     */
    public void release() {
        if (mFuture != null && !mFuture.isCancelled()) {
            mFuture.cancel(true);
            mFuture = null;
        }

        if (mExecutorService != null) {
            mExecutorService = null;
        }
    }
}

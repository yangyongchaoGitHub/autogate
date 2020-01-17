package com.dataexpo.autogate.face.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.FaceDetect;
import com.baidu.idl.main.facesdk.FaceFeature;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.FaceLive;
import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceOcclusion;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.model.BDFaceSDKConfig;
import com.baidu.idl.main.facesdk.model.Feature;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.dataexpo.autogate.face.api.FaceApi;
import com.dataexpo.autogate.face.callback.FaceDetectCallBack;
import com.dataexpo.autogate.face.callback.FaceFeatureCallBack;
import com.dataexpo.autogate.face.listener.SdkInitListener;
import com.dataexpo.autogate.face.model.GlobalSet;
import com.dataexpo.autogate.face.model.LivenessModel;
import com.dataexpo.autogate.face.model.SingleBaseConfig;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.service.UserService;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.BDFaceAnakinRunMode.BDFACE_ANAKIN_RUN_AT_SMALL_CORE;
import static com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.BDFaceLogInfo.BDFACE_LOG_ERROR_MESSAGE;
import static com.dataexpo.autogate.face.model.GlobalSet.FEATURE_SIZE;

public class FaceSDKManager {
    private static final String TAG = FaceSDKManager.class.getSimpleName();
    private static final int AUTH_AUTO = 0;
    private static final int AUTH_INLINE = 1;
    private static final int AUTH_OFFLINE = 2;

    public static final int SDK_MODEL_LOAD_SUCCESS = 0;
    public static final int SDK_UNACTIVATION = 1;
    public static final int SDK_UNINIT = 2;
    public static final int SDK_INITING = 3;
    public static final int SDK_INITED = 4;
    public static final int SDK_INIT_FAIL = 5;
    public static final int SDK_INIT_SUCCESS = 6;

    public static volatile int initStatus = SDK_UNACTIVATION;

    private FaceAuth faceAuth;
    private FaceDetect faceDetect;
    private FaceFeature faceFeature;
    private FaceLive faceLiveness;

    private ExecutorService es = Executors.newSingleThreadExecutor();
    private Future future;
    private ExecutorService es2 = Executors.newSingleThreadExecutor();
    private Future future2;

    private FaceSDKManager() {
        faceAuth = new FaceAuth();
        faceAuth.setActiveLog(BDFACE_LOG_ERROR_MESSAGE);
        faceAuth.setAnakinConfigure(BDFACE_ANAKIN_RUN_AT_SMALL_CORE, 2);

        faceDetect = new FaceDetect();
        faceFeature = new FaceFeature();
        faceLiveness = new FaceLive();
    }

    private static class HolderClass {
        private static final FaceSDKManager instance = new FaceSDKManager();
    }

    public static FaceSDKManager getInstance() {
        return HolderClass.instance;
    }

    public FaceDetect getFaceDetect() {
        return faceDetect;
    }

    public FaceFeature getFaceFeature() {
        return faceFeature;
    }

    public FaceLive getFaceLiveness() {
        return faceLiveness;
    }

    public void init(final Context context, final SdkInitListener listener) {
        init(context, listener, AUTH_AUTO);
    }
    /**
     * 新鉴权接口，可以使用离线或者在线模式进行鉴权
     * @param context
     * @param listener
     * @param type 选择在线或者离线模式
     */
    public void init(final Context context, final SdkInitListener listener, int type) {
        PreferencesUtil.initPrefs(context.getApplicationContext());
        String licenseOfflineKey = PreferencesUtil.getString("activate_offline_key", "");
        //在MI8上
        //final String licenseOnlineKey = PreferencesUtil.getString("activate_online_key", "HPYX-AEQ9-PGNU-GQTA");
        // 在firework上
        //String licenseOnlineKey = PreferencesUtil.getString("activate_online_key", "ZRHX-HNLO-DWNC-LONJ");
        //在firewpork新设置的key
        //String licenseOnlineKey = PreferencesUtil.getString("activate_online_key", "TRXE-J7XX-LUXZ-PZ3U");
        //在开发板上
        //String licenseOnlineKey = PreferencesUtil.getString("activate_online_key", "");
        //开发板原有的key    3PSM-KOHY-AQLG-BBWI
        //String licenseOnlineKey = PreferencesUtil.getString("activate_online_key", "3PSM-KOHY-AQLG-BBWI");
        //43寸大屏板
        String licenseOnlineKey = PreferencesUtil.getString("activate_online_key", "KB8L-D2LN-E2UN-FGCF");
        //String licenseOnlineKey = PreferencesUtil.getString("activate_online_key", "");

//        if (!TextUtils.isEmpty(licenseOfflineKey) && TextUtils.isEmpty(licenseOnlineKey)) {
//            licenseOnlineKey = licenseOfflineKey;
//        }
        //licenseOnlineKey = "TRXE-J7XX-LUXZ-PZ3U";
        //licenseOfflineKey = "TRXE-J7XX-LUXZ-PZ3U";

        Log.i("FaceSDKManager", "offLineKey:" + licenseOfflineKey + " onLineKey: " + licenseOnlineKey);

        // 如果licenseKey 不存在提示授权码为空，并跳转授权页面授权
        if (TextUtils.isEmpty(licenseOfflineKey) && TextUtils.isEmpty(licenseOnlineKey)) {
            Log.i(TAG, "未授权设备，请完成授权激活");
            if (listener != null) {
                listener.initLicenseFail(-1, "授权码不存在，请重新输入！");
            }
            return;
        }

        if (listener != null) {
            listener.initStart();
        }

        if (!TextUtils.isEmpty(licenseOfflineKey) && (type == AUTH_OFFLINE || type == AUTH_AUTO)) {
            initOffLine(context, listener, licenseOfflineKey);

        } else if (!TextUtils.isEmpty(licenseOnlineKey) && (type == AUTH_INLINE || type == AUTH_AUTO)) {
            initOnLine(context, listener, licenseOnlineKey);

        } else {
            if (listener != null) {
                listener.initLicenseFail(-1, "授权码不存在，请重新输入！");
            }
        }
    }

    public void initOnLine(final Context context, final SdkInitListener listener, String key) {
        //ToastUtils.toast(context, "进行在线激活!!! " + key);
        Log.i("FaceSDKManager", "进行在线激活 " + key);
        // 在线激活
        faceAuth.initLicenseOnLine(context, key, new Callback() {
            @Override
            public void onResponse(int code, String response) {
                if (code == 0) {
                    initStatus = SDK_INIT_SUCCESS;
                    if (listener != null) {
                        listener.initLicenseSuccess();
                    }
                    initModel(context, listener);
                } else {
                    if (listener != null) {
                        listener.initLicenseFail(code, response);
                    }
                }
            }
        });
    }

    public void initOffLine(final Context context, final SdkInitListener listener, String key) {
        initOffLine(context, listener, key, false);
    }

    public void initOffLine(final Context context, final SdkInitListener listener, String key, final boolean tryOnline) {
        //ToastUtils.toast(context, "进行离线激活!!! " + key);
        Log.i("FaceSDKManager", "进行离线激活");
        // 离线激活
        faceAuth.initLicenseOffLine(context, new Callback() {
            @Override
            public void onResponse(int code, String response) {
                Log.i("FaceSDKManager", "进行离线激活 code:" + code + " " + response);
                if (code == 0) {
                    initStatus = SDK_INIT_SUCCESS;
                    if (listener != null) {
                        listener.initLicenseSuccess();
                    }
                    initModel(context, listener);
                } else {
                    if (listener != null) {
                        listener.initLicenseFail(code, response);
                    }
                    if (tryOnline) {
                        init(context, listener, AUTH_INLINE);
                    }
                }
            }
        });
    }

    /**
     * 检测-活体-特征- 全流程
     *
     * @param bitmap             用来检测的图片数据
     * @param nirData            红外YUV 数据流
     * @param depthData          深度depth 数据流
     * @param srcHeight          可见光YUV 数据流-高度
     * @param srcWidth           可见光YUV 数据流-宽度
     * @param liveCheckMode      活体检测模式【不使用活体：1】；【RGB活体：2】；【RGB+NIR活体：3】；【RGB+Depth活体：4】
     * @param featureCheckMode   特征抽取模式【不提取特征：1】；【提取特征：2】；【提取特征+1：N检索：3】；
     * @param faceDetectCallBack
     */
    public void onBitmapDetectCheck(final Bitmap bitmap,
                                    final byte[] nirData,
                                    final byte[] depthData,
                                    final int srcHeight,
                                    final int srcWidth,
                                    final int liveCheckMode,
                                    final int featureCheckMode,
                                    final FaceDetectCallBack faceDetectCallBack) {
        if (future != null && !future.isDone()) {
            return;
        }

        future = es.submit(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                // 创建检测结果存储数据
                LivenessModel livenessModel = new LivenessModel();
                // 创建检测对象，如果原始数据YUV，转为算法检测的图片BGR
                // TODO: 用户调整旋转角度和是否镜像，手机和开发版需要动态适配
                BDFaceImageInstance rgbInstance = new BDFaceImageInstance(bitmap);

                // TODO: getImage() 获取送检图片,如果检测数据有问题，可以通过image view 展示送检图片
                livenessModel.setBdFaceImageInstance(rgbInstance.getImage());

                // 检查函数调用，返回检测结果
                long startDetectTime = System.currentTimeMillis();
                FaceInfo[] faceInfos = faceDetect.track(BDFaceSDKCommon.DetectType.DETECT_VIS, rgbInstance);

                livenessModel.setRgbDetectDuration(System.currentTimeMillis() - startDetectTime);
                //LogUtils.e(TIME_TAG, "detect vis time = " + livenessModel.getRgbDetectDuration());

                // 检测结果判断
                if (faceInfos != null && faceInfos.length > 0) {
                    livenessModel.setTrackFaceInfo(faceInfos);
                    livenessModel.setFaceInfo(faceInfos[0]);
                    livenessModel.setLandmarks(faceInfos[0].landmarks);
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectDarwCallback(livenessModel);
                    }
                    // 活体检测
                    if (onQualityCheck(livenessModel, faceDetectCallBack)) {
                        onLivenessCheck(rgbInstance, nirData, depthData, srcHeight,
                                srcWidth, livenessModel.getLandmarks(),
                                livenessModel, startTime, liveCheckMode, featureCheckMode, faceDetectCallBack);
                    } else {
                        // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
                        rgbInstance.destory();
                    }
                } else {
                    // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
                    rgbInstance.destory();
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectCallback(null);
                        faceDetectCallBack.onFaceDetectDarwCallback(null);
                        faceDetectCallBack.onTip(0, "未检测到人脸");
                    }
                }
            }
        });
    }

    /**
     * 检测-活体-特征- 全流程
     *
     * @param rgbData            可见光YUV 数据流
     * @param nirData            红外YUV 数据流
     * @param depthData          深度depth 数据流
     * @param srcHeight          可见光YUV 数据流-高度
     * @param srcWidth           可见光YUV 数据流-宽度
     * @param liveCheckMode      活体检测模式【不使用活体：1】；【RGB活体：2】；【RGB+NIR活体：3】；【RGB+Depth活体：4】
     * @param featureCheckMode   特征抽取模式【不提取特征：1】；【提取特征：2】；【提取特征+1：N检索：3】；
     * @param faceDetectCallBack
     */
    public void onDetectCheck(final byte[] rgbData,
                              final byte[] nirData,
                              final byte[] depthData,
                              final int srcHeight,
                              final int srcWidth,
                              final int liveCheckMode,
                              final int featureCheckMode,
                              final FaceDetectCallBack faceDetectCallBack) {

        if (future != null && !future.isDone()) {
            return;
        }

        future = es.submit(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                // 创建检测结果存储数据
                LivenessModel livenessModel = new LivenessModel();
                // 创建检测对象，如果原始数据YUV，转为算法检测的图片BGR
                // TODO: 用户调整旋转角度和是否镜像，手机和开发版需要动态适配
                BDFaceImageInstance rgbInstance = new BDFaceImageInstance(rgbData, srcHeight,
                        srcWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_420,
                        SingleBaseConfig.getBaseConfig().getDetectDirection(),
                        SingleBaseConfig.getBaseConfig().getMirrorRGB());

                // TODO: getImage() 获取送检图片,如果检测数据有问题，可以通过image view 展示送检图片
                livenessModel.setBdFaceImageInstance(rgbInstance.getImage());

                // 检查函数调用，返回检测结果
                long startDetectTime = System.currentTimeMillis();
                FaceInfo[] faceInfos = faceDetect.track(BDFaceSDKCommon.DetectType.DETECT_VIS, rgbInstance);

                livenessModel.setRgbDetectDuration(System.currentTimeMillis() - startDetectTime);
                //LogUtils.e(TIME_TAG, "detect vis time = " + livenessModel.getRgbDetectDuration());

                // 检测结果判断
                if (faceInfos != null && faceInfos.length > 0) {
                    livenessModel.setTrackFaceInfo(faceInfos);
                    livenessModel.setFaceInfo(faceInfos[0]);
                    livenessModel.setLandmarks(faceInfos[0].landmarks);
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectDarwCallback(livenessModel);
                    }
                    // 活体检测
                    if (onQualityCheck(livenessModel, faceDetectCallBack)) {
                        onLivenessCheck(rgbInstance, nirData, depthData, srcHeight,
                                srcWidth, livenessModel.getLandmarks(),
                                livenessModel, startTime, liveCheckMode, featureCheckMode, faceDetectCallBack);
                    } else {
                        // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
                        rgbInstance.destory();
                    }
                } else {
                    // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
                    rgbInstance.destory();
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectCallback(null);
                        faceDetectCallBack.onFaceDetectDarwCallback(null);
                        faceDetectCallBack.onTip(0, "未检测到人脸");
                    }
                }
            }
        });
    }

    /**
     * 检测-活体-特征- 全流程
     *
     * @param rgbData            可见光YUV 数据流
     * @param nirData            红外YUV 数据流
     * @param depthData          深度depth 数据流
     * @param srcHeight          可见光YUV 数据流-高度
     * @param srcWidth           可见光YUV 数据流-宽度
     * @param liveCheckMode      活体检测模式【不使用活体：1】；【RGB活体：2】；【RGB+NIR活体：3】；【RGB+Depth活体：4】
     * @param featureCheckMode   特征抽取模式【不提取特征：1】；【提取特征：2】；【提取特征+1：N检索：3】；
     * @param faceDetectCallBack
     */
    public void onDetectRgbNir(final byte[] rgbData,
                              final byte[] nirData,
                              final byte[] depthData,
                              final int srcHeight,
                              final int srcWidth,
                              final int liveCheckMode,
                              final int featureCheckMode,
                              final FaceDetectCallBack faceDetectCallBack) {

        if (future != null && !future.isDone()) {
            return;
        }

        future = es.submit(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "rgb: " + rgbData + " nir " + nirData);

                long startTime = System.currentTimeMillis();
                // 创建检测结果存储数据
                LivenessModel livenessModel = new LivenessModel();
                // 创建检测对象，如果原始数据YUV，转为算法检测的图片BGR
                // TODO: 用户调整旋转角度和是否镜像，手机和开发版需要动态适配
                BDFaceImageInstance rgbInstance = new BDFaceImageInstance(rgbData, srcHeight,
                        srcWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_420,
                        SingleBaseConfig.getBaseConfig().getDetectDirection(),
                        SingleBaseConfig.getBaseConfig().getMirrorRGB());

                // TODO: getImage() 获取送检图片,如果检测数据有问题，可以通过image view 展示送检图片
                livenessModel.setBdFaceImageInstance(rgbInstance.getImage());

                // 检查函数调用，返回检测结果
                long startDetectTime = System.currentTimeMillis();
                FaceInfo[] faceInfos = faceDetect.track(BDFaceSDKCommon.DetectType.DETECT_VIS, rgbInstance);

                livenessModel.setRgbDetectDuration(System.currentTimeMillis() - startDetectTime);
                //LogUtils.e(TIME_TAG, "detect vis time = " + livenessModel.getRgbDetectDuration());



                // 检测结果判断
                if (faceInfos != null && faceInfos.length > 0) {
                    livenessModel.setTrackFaceInfo(faceInfos);
                    livenessModel.setFaceInfo(faceInfos[0]);
                    livenessModel.setLandmarks(faceInfos[0].landmarks);
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectDarwCallback(livenessModel);
                        faceDetectCallBack.onTip(3, "rgb have face");
                    }
                    Log.i(TAG, "活体检测1  ");
                    // 活体检测
                    //if (onQualityCheck(livenessModel, faceDetectCallBack)) {
                        onLivenessCheck(rgbInstance, nirData, depthData, srcHeight,
                                srcWidth, livenessModel.getLandmarks(),
                                livenessModel, startTime, liveCheckMode, featureCheckMode, faceDetectCallBack);
                    //}
                } else {
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onTip(2, "rgb no face!!!");
                    }

                    Log.i(TAG, "活体检测2  ");
                    onLivenessCheck(rgbInstance, nirData, depthData, srcHeight,
                            srcWidth, livenessModel.getLandmarks(),
                            livenessModel, startTime, liveCheckMode, featureCheckMode, faceDetectCallBack);
                }


//                if () {
//
//                    if (faceDetectCallBack != null) {
//                        faceDetectCallBack.onFaceDetectCallback(null);
//                        faceDetectCallBack.onFaceDetectDarwCallback(null);
//                        faceDetectCallBack.onTip(0, "未检测到人脸");
//                    }
//                }
                // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
                rgbInstance.destory();
            }
        });
    }

    /**
     * 检测-活体-特征-人脸检索流程
     *
     * @param rgbData            可见光YUV 数据流
     * @param nirData            红外YUV 数据流
     * @param depthData          深度depth 数据流
     * @param srcHeight          可见光YUV 数据流-高度
     * @param srcWidth           可见光YUV 数据流-宽度
     * @param liveCheckMode      活体检测类型【不使用活体：1】；【RGB活体：2】；【RGB+NIR活体：3】；【RGB+Depth活体：4】
     * @param faceDetectCallBack
     */
    public void onDetectCheck(final byte[] rgbData,
                              final byte[] nirData,
                              final byte[] depthData,
                              final int srcHeight,
                              final int srcWidth,
                              final int liveCheckMode,
                              final FaceDetectCallBack faceDetectCallBack) {

        // 【【提取特征+1：N检索：3】
        onDetectCheck(rgbData, nirData, depthData, srcHeight, srcWidth, liveCheckMode, 3, faceDetectCallBack);
    }


    /**
     * 活体-特征-人脸检索全流程
     *
     * @param rgbInstance        可见光底层送检对象
     * @param nirData            红外YUV 数据流
     * @param depthData          深度depth 数据流
     * @param srcHeight          可见光YUV 数据流-高度
     * @param srcWidth           可见光YUV 数据流-宽度
     * @param landmark           检测眼睛，嘴巴，鼻子，72个关键点
     * @param livenessModel      检测结果数据集合
     * @param startTime          开始检测时间
     * @param liveCheckMode      活体检测模式【不使用活体：1】；【RGB活体：2】；【RGB+NIR活体：3】；【RGB+Depth活体：4】
     * @param featureCheckMode   特征抽取模式【不提取特征：1】；【提取特征：2】；【提取特征+1：N检索：3】；
     * @param faceDetectCallBack
     */
    public void onLivenessCheck(final BDFaceImageInstance rgbInstance,
                                final byte[] nirData,
                                final byte[] depthData,
                                final int srcHeight,
                                final int srcWidth,
                                final float[] landmark,
                                final LivenessModel livenessModel,
                                final long startTime,
                                final int liveCheckMode,
                                final int featureCheckMode,
                                final FaceDetectCallBack faceDetectCallBack) {

        if (future2 != null && !future2.isDone()) {
            // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
            rgbInstance.destory();
            return;
        }

        future2 = es2.submit(new Runnable() {
            @Override
            public void run() {
                // 获取LivenessConfig liveCheckMode 配置选项：【不使用活体：1】；【RGB活体：2】；【RGB+NIR活体：3】；【RGB+Depth活体：4】

                // TODO 活体检测
                float rgbScore = -1;
                if (liveCheckMode != 1) {
                    long startRgbTime = System.currentTimeMillis();
                    rgbScore = FaceSDKManager.getInstance().getFaceLiveness().silentLive(
                            BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_RGB,
                            rgbInstance, landmark);
                    livenessModel.setRgbLivenessScore(rgbScore);
                    livenessModel.setRgbLivenessDuration(System.currentTimeMillis() - startRgbTime);
                    Log.e(TAG, "live rgb time = " + livenessModel.getRgbLivenessDuration());
                }

                float nirScore = -1;
                if (liveCheckMode == 3 && nirData != null) {
                    // 创建检测对象，如果原始数据YUV-IR，转为算法检测的图片BGR
                    // TODO: 用户调整旋转角度和是否镜像，手机和开发版需要动态适配
                    BDFaceImageInstance nirInstance = new BDFaceImageInstance(nirData, srcHeight,
                            srcWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_420,
                            SingleBaseConfig.getBaseConfig().getDetectDirection(),
                            SingleBaseConfig.getBaseConfig().getMirrorNIR());

                    // 避免RGB检测关键点在IR对齐活体稳定，增加红外检测
                    long startIrDetectTime = System.currentTimeMillis();
                    FaceInfo[] faceInfosIr = faceDetect.detect(BDFaceSDKCommon.DetectType.DETECT_NIR, nirInstance);
                    livenessModel.setIrLivenessDuration(System.currentTimeMillis() - startIrDetectTime);
                    Log.e(TAG, "detect ir time = " + livenessModel.getIrLivenessDuration());

                    if (faceInfosIr != null && faceInfosIr.length > 0) {
                        FaceInfo faceInfoIr = faceInfosIr[0];
                        long startNirTime = System.currentTimeMillis();
                        nirScore = FaceSDKManager.getInstance().getFaceLiveness().silentLive(
                                BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_NIR,
                                nirInstance, faceInfoIr.landmarks);
                        livenessModel.setIrLivenessScore(nirScore);
                        livenessModel.setIrLivenessDuration(System.currentTimeMillis() - startNirTime);
                        Log.e(TAG, "live ir time = " + livenessModel.getIrLivenessDuration());
                    }
                    nirInstance.destory();
                }

                float depthScore = -1;
                if (liveCheckMode == 4 && depthData != null) {
                    // TODO: 用户调整旋转角度和是否镜像，适配Atlas 镜头，目前宽和高400*640，其他摄像头需要动态调整,人脸72 个关键点x 坐标向左移动80个像素点

                    float[] depthLandmark = new float[landmark.length];
                    System.arraycopy(landmark, 0, depthLandmark, 0, landmark.length);
                    for (int i = 0; i < 144; i = i + 2) {
                        depthLandmark[i] -= 80;
                    }

                    // 创建检测对象，如果原始数据Depth
                    BDFaceImageInstance depthInstance = new BDFaceImageInstance(depthData, 640,
                            400, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_DEPTH,
                            0, 0);

                    long startDepthTime = System.currentTimeMillis();
                    depthScore = FaceSDKManager.getInstance().getFaceLiveness().silentLive(
                            BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_DEPTH,
                            depthInstance, depthLandmark);
                    livenessModel.setDepthLivenessScore(depthScore);
                    livenessModel.setDepthtLivenessDuration(System.currentTimeMillis() - startDepthTime);
                    Log.e(TAG, "live depth time = " + livenessModel.getDepthtLivenessDuration());
                    depthInstance.destory();
                }

                // TODO 特征提取+人脸检索
                if (liveCheckMode == 1) {
                    onFeatureCheck(rgbInstance, landmark, livenessModel, featureCheckMode);
                } else {
                    if (liveCheckMode == 2 && rgbScore > SingleBaseConfig.getBaseConfig().getRgbLiveScore()) {
                        onFeatureCheck(rgbInstance, landmark, livenessModel, featureCheckMode);
                    } else if (liveCheckMode == 3 && rgbScore > SingleBaseConfig.getBaseConfig().getRgbLiveScore()
                            && nirScore > SingleBaseConfig.getBaseConfig().getNirLiveScore()) {
                        onFeatureCheck(rgbInstance, landmark, livenessModel, featureCheckMode);
                    } else if (liveCheckMode == 4 && rgbScore > SingleBaseConfig.getBaseConfig().getRgbLiveScore()
                            && depthScore > SingleBaseConfig.getBaseConfig().getDepthLiveScore()) {
                        onFeatureCheck(rgbInstance, landmark, livenessModel, featureCheckMode);
                    }
                }

                // 流程结束,记录最终时间
                livenessModel.setAllDetectDuration(System.currentTimeMillis() - startTime);
                Log.e(TAG, "all process time = " + livenessModel.getAllDetectDuration());
                // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
                rgbInstance.destory();
                // 显示最终结果提示
                if (faceDetectCallBack != null) {
                    faceDetectCallBack.onFaceDetectCallback(livenessModel);
                }
            }
        });
    }

    /**
     * 特征提取-人脸识别比对
     *
     * @param rgbInstance      可见光底层送检对象
     * @param landmark         检测眼睛，嘴巴，鼻子，72个关键点
     * @param livenessModel    检测结果数据集合
     * @param featureCheckMode 特征抽取模式【不提取特征：1】；【提取特征：2】；【提取特征+1：N检索：3】；
     */
    public void onFeatureCheck(BDFaceImageInstance rgbInstance,
                               float[] landmark,
                               LivenessModel livenessModel,
                               final int featureCheckMode) {

        // 如果不抽取特征，直接返回
        if (featureCheckMode == 1) {
            return;
        }

        byte[] feature = new byte[512];
        long startFeatureTime = System.currentTimeMillis();

        float featureSize = FaceSDKManager.getInstance().getFaceFeature().feature(
                BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO, rgbInstance, landmark, feature);

        livenessModel.setFeatureDuration(System.currentTimeMillis() - startFeatureTime);
        Log.e(TAG, "feature live time = " + livenessModel.getFeatureDuration());
        livenessModel.setFeature(feature);

        // 如果只提去特征，不做检索，此处返回
        if (featureCheckMode == 2) {
            livenessModel.setFeatureCode(featureSize);
            return;
        }

        // 如果提取特征+检索，调用search 方法
        if (featureSize == FEATURE_SIZE / 4) {
            // 特征提取成功
            // TODO 阈值可以根据不同模型调整
            long startFeature = System.currentTimeMillis();
            ArrayList<Feature> featureResult = FaceSDKManager.getInstance().getFaceFeature().featureSearch(feature,
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO,
                    1, true);

            if (featureResult != null) {
                Log.i(TAG, "检索结果个数：" + featureResult.size());
            } else {
                Log.i(TAG, "人脸库检索为空");
            }

            // TODO 返回top num = 1 个数据集合，此处可以任意设置，会返回比对从大到小排序的num 个数据集合
            if (featureResult != null && featureResult.size() > 0) {
                // 获取第一个数据
                Feature topFeature = featureResult.get(0);
                // 判断第一个阈值是否大于设定阈值，如果大于，检索成功
                if (topFeature != null) {
                    Log.i(TAG, "score: " + topFeature.getScore());
                }

                if (topFeature != null && topFeature.getScore() >
                        SingleBaseConfig.getBaseConfig().getThreshold()) {
                    // 当前featureEntity 只有id+feature 索引，在数据库中查到完整信息
                    Log.i(TAG, "featureResult userid: " + topFeature.getId());
                    User user = UserService.getInstance().findUserById(topFeature.getId());
                    if (user != null) {
                        livenessModel.setUser(user);
                        livenessModel.setFeatureScore(topFeature.getScore());
                    }
                }
            }
            livenessModel.setCheckDuration(System.currentTimeMillis() - startFeature);
            Log.e(TAG, "feature search time = " + livenessModel.getCheckDuration());
        }
    }

    /**
     * 单独调用 特征提取
     *
     * @param imageInstance       可见光底层送检对象
     * @param landmark            检测眼睛，嘴巴，鼻子，72个关键点
     * @param featureCheckMode    特征提取模式
     * @param faceFeatureCallBack 回掉方法
     */
    public void onFeatureCheck(BDFaceImageInstance imageInstance, float[] landmark,
                               BDFaceSDKCommon.FeatureType featureCheckMode,
                               final FaceFeatureCallBack faceFeatureCallBack) {

        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(imageInstance.data,
                imageInstance.height, imageInstance.width,
                imageInstance.imageType, 0, 0);

        byte[] feature = new byte[512];
        float featureSize = FaceSDKManager.getInstance().getFaceFeature().feature(
                featureCheckMode, rgbInstance, landmark, feature);
        if (featureSize == FEATURE_SIZE / 4) {
            // 特征提取成功
            if (faceFeatureCallBack != null) {
                faceFeatureCallBack.onFaceFeatureCallBack(featureSize, feature);
            }

        }
        // 流程结束销毁图片
        rgbInstance.destory();
    }

    /**
     * 质量检测结果过滤，如果需要质量检测，
     * 需要调用 SingleBaseConfig.getBaseConfig().setQualityControl(true);设置为true，
     * 再调用  FaceSDKManager.getInstance().initConfig() 加载到底层配置项中
     *
     * @param livenessModel
     * @param faceDetectCallBack
     * @return
     */
    public boolean onQualityCheck(final LivenessModel livenessModel,
                                  final FaceDetectCallBack faceDetectCallBack) {

        if (!SingleBaseConfig.getBaseConfig().isQualityControl()) {
            return true;
        }

        if (livenessModel != null && livenessModel.getFaceInfo() != null) {

            // 角度过滤
            if (Math.abs(livenessModel.getFaceInfo().yaw) > SingleBaseConfig.getBaseConfig().getYaw()) {
                faceDetectCallBack.onTip(-1, "人脸左右偏转角超出限制");
                return false;
            } else if (Math.abs(livenessModel.getFaceInfo().roll) > SingleBaseConfig.getBaseConfig().getRoll()) {
                faceDetectCallBack.onTip(-1, "人脸平行平面内的头部旋转角超出限制");
                return false;
            } else if (Math.abs(livenessModel.getFaceInfo().pitch) > SingleBaseConfig.getBaseConfig().getPitch()) {
                faceDetectCallBack.onTip(-1, "人脸上下偏转角超出限制");
                return false;
            }

            // 模糊结果过滤
            float blur = livenessModel.getFaceInfo().bluriness;
            if (blur > SingleBaseConfig.getBaseConfig().getBlur()) {
                faceDetectCallBack.onTip(-1, "图片模糊");
                return false;
            }

            // 光照结果过滤
            float illum = livenessModel.getFaceInfo().illum;
            if (illum < SingleBaseConfig.getBaseConfig().getIllumination()) {
                faceDetectCallBack.onTip(-1, "图片光照不通过");
                return false;
            }


            // 遮挡结果过滤
            if (livenessModel.getFaceInfo().occlusion != null) {
                BDFaceOcclusion occlusion = livenessModel.getFaceInfo().occlusion;

                if (occlusion.leftEye > SingleBaseConfig.getBaseConfig().getLeftEye()) {
                    // 左眼遮挡置信度
                    faceDetectCallBack.onTip(-1, "左眼遮挡");
                } else if (occlusion.rightEye > SingleBaseConfig.getBaseConfig().getRightEye()) {
                    // 右眼遮挡置信度
                    faceDetectCallBack.onTip(-1, "右眼遮挡");
                } else if (occlusion.nose > SingleBaseConfig.getBaseConfig().getNose()) {
                    // 鼻子遮挡置信度
                    faceDetectCallBack.onTip(-1, "鼻子遮挡");
                } else if (occlusion.mouth > SingleBaseConfig.getBaseConfig().getMouth()) {
                    // 嘴巴遮挡置信度
                    faceDetectCallBack.onTip(-1, "嘴巴遮挡");
                } else if (occlusion.leftCheek > SingleBaseConfig.getBaseConfig().getLeftCheek()) {
                    // 左脸遮挡置信度
                    faceDetectCallBack.onTip(-1, "左脸遮挡");
                } else if (occlusion.rightCheek > SingleBaseConfig.getBaseConfig().getRightCheek()) {
                    // 右脸遮挡置信度
                    faceDetectCallBack.onTip(-1, "右脸遮挡");
                } else if (occlusion.chin > SingleBaseConfig.getBaseConfig().getChinContour()) {
                    // 下巴遮挡置信度
                    faceDetectCallBack.onTip(-1, "下巴遮挡");
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 初始化模型，目前包含检查，活体，识别模型；因为初始化是顺序执行，可以在最好初始化回掉中返回状态结果
     *
     * @param context
     * @param listener
     */
    public void initModel(final Context context, final SdkInitListener listener) {
        //ToastUtils.toast(context, "模型初始化中，请稍后片刻");

        initConfig();

        final long startInitModelTime = System.currentTimeMillis();
        faceDetect.initModel(context,
                GlobalSet.DETECT_VIS_MODEL,
                GlobalSet.DETECT_NIR_MODE,
                GlobalSet.ALIGN_MODEL,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        //  ToastUtils.toast(context, code + "  " + responseData);
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceDetect.initQuality(context,
                GlobalSet.BLUR_MODEL,
                GlobalSet.OCCLUSION_MODEL, new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceDetect.initAttrEmo(context, GlobalSet.ATTRIBUTE_MODEL, GlobalSet.EMOTION_MODEL, new Callback() {
            @Override
            public void onResponse(int code, String response) {
                if (code != 0 && listener != null) {
                    listener.initModelFail(code, response);
                }
            }
        });

        faceLiveness.initModel(context,
                GlobalSet.LIVE_VIS_MODEL,
                GlobalSet.LIVE_NIR_MODEL,
                GlobalSet.LIVE_DEPTH_MODEL,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        //  ToastUtils.toast(context, code + "  " + responseData);
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceFeature.initModel(context,
                GlobalSet.RECOGNIZE_IDPHOTO_MODEL,
                GlobalSet.RECOGNIZE_VIS_MODEL,
                "",
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        long endInitModelTime = System.currentTimeMillis();
                        Log.e(TAG, "init model time = " + (endInitModelTime - startInitModelTime));
                        if (code != 0) {
                            //ToastUtils.toast(context, "模型加载失败");
                            if (listener != null) {
                                listener.initModelFail(code, response);
                            }
                        } else {
                            initStatus = SDK_MODEL_LOAD_SUCCESS;
                            // 模型初始化成功，加载人脸数据
                            initDataBases(context);
                            Log.i(TAG, "模型加载完毕，欢迎使用");
                            //ToastUtils.toast(context, "模型加载完毕，欢迎使用");
                            if (listener != null) {
                                listener.initModelSuccess();
                            }
                        }
                    }
                });
    }

    /**
     * 初始化配置
     *
     * @return
     */
    public boolean initConfig() {
        if (faceDetect != null) {
            BDFaceSDKConfig config = new BDFaceSDKConfig();
            // TODO: 最小人脸个数检查，默认设置为1,用户根据自己需求调整
            config.maxDetectNum = 1;

            // TODO: 默认为80px。可传入大于30px的数值，小于此大小的人脸不予检测，生效时间第一次加载模型
            config.minFaceSize = SingleBaseConfig.getBaseConfig().getMinimumFace();
            // 是否进行属性检测，默认关闭
            config.isAttribute = SingleBaseConfig.getBaseConfig().isAttribute();

            // TODO: 模糊，遮挡，光照三个质量检测和姿态角查默认关闭，如果要开启，设置页启动
            config.isCheckBlur = config.isOcclusion
                    = config.isIllumination = config.isHeadPose
                    = SingleBaseConfig.getBaseConfig().isQualityControl();

            faceDetect.loadConfig(config);
            return true;
        }
        return false;
    }

    public void initDataBases(Context context) {
        // 初始化数据库
        //DBManager.getInstance().init(context);
        // 数据变化，更新内存
        FaceApi.getInstance().initDatabases(true);
    }
}

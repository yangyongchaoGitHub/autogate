package com.dataexpo.autogate.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.company.NetSDK.CB_fSnapRev;
import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.dahuacameracommon.CapturePictureModule;
import com.dataexpo.autogate.dahuacameracommon.EncodeModule;
import com.dataexpo.autogate.dahuacameracommon.IPLoginModule;
import com.dataexpo.autogate.dahuacameracommon.LivePreviewModule;
import com.dataexpo.autogate.dahuacameracommon.OSDModule;
import com.dataexpo.autogate.dahuacameracommon.PTZControl;
import com.dataexpo.autogate.face.callback.FaceDetectCallBack;
import com.dataexpo.autogate.face.camera.AutoTexturePreviewView;
import com.dataexpo.autogate.face.camera.CameraPreviewManager;
import com.dataexpo.autogate.face.listener.SdkInitListener;
import com.dataexpo.autogate.face.manager.FaceSDKManager;
import com.dataexpo.autogate.face.model.LivenessModel;
import com.dataexpo.autogate.hikvision.control.DevManageGuider;
import com.dataexpo.autogate.hikvision.control.SDKGuider;
import com.dataexpo.autogate.listener.FaceOnSecCallback;
import com.dataexpo.autogate.listener.OnFrameCallback;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.service.MainApplication;
import com.dataexpo.autogate.service.data.UserService;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;

import retrofit2.Retrofit;

import static com.dataexpo.autogate.model.User.AUTH_SUCCESS;

/**
 * 原分屏是小屏，现分屏是大屏，所以做次调整 Reverse配合Reverse
 * 此屏显示摄像头内容，后台管理内容
 */
public class GateActivityReverse extends BascActivity implements View.OnClickListener, SurfaceHolder.Callback, OnFrameCallback, FaceOnSecCallback {
    private static final String TAG = GateActivityReverse.class.getSimpleName();
    private Context mContext;

    //使用大华IPC摄像机的内容
    public static int MODEL_DAHUA = 1;
    //使用自制视频流的内容
    public static int MODEL_CAMERA_STEAM = 2;

    private ImageView iv_camera;
    private ImageView iv_success_curr;
    private ImageView iv_success_last1;
    private ImageView iv_success_last2;
    private ImageView iv_success_last3;
    private ImageView iv_success_last4;
    private ImageView iv_success_last5;
    private ImageView iv_success_last6;
    private ImageView iv_success_last7;
    private ImageView iv_success_last8;

    private TextView tv_name;
    private TextView tv_company;
    private TextView tv_deputation;
    private TextView tv_auth;
    private TextView tv_success_last_name1;
    private TextView tv_success_last_name2;
    private TextView tv_success_last_name3;
    private TextView tv_success_last_name4;
    private TextView tv_success_last_name5;
    private TextView tv_success_last_name6;
    private TextView tv_success_last_name7;
    private TextView tv_success_last_name8;

    private ImageButton ib_back;
    private ImageButton tv_back;

    private ImageView[] ivs = new ImageView[8];
    private TextView[] tvs = new TextView[8];
    private User[] users = new User[8];
    private SurfaceView sf;

    private int bShowModel = MODEL_DAHUA;
    private IPLoginModule mLoginModule;
    private MainApplication app;
    private OSDModule osdModule;
    private EncodeModule encodeModule;
    private LivePreviewModule mLiveModule;
    private PTZControl ptzControl;
    private CapturePictureModule mCapturePictureModule;

    private ImageView iv_snap;
    private FaceOnSecCallback callback;

    private User faceCurrUser = null;
    private User GateCurrUser = null;


    private AutoTexturePreviewView mAutoCameraPreviewView;

    private static final int PREFER_WIDTH = 1280;
    private static final int PERFER_HEIGH = 720;

    private Retrofit mRetrofit;
    //TODO: 海康威视
    private int hId = -1; //登录返回的id
    private int playingHandle = -1; //是否播放成功

    private QueryImgThread queryImgThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presentation_phone_camera);
        mContext = this;
        initView();

        for (int i = 0; i < users.length; i++) {
            users[i] = null;
        }

        app = MainApplication.getInstance();
        if (app != null && app.getService() != null) {
            app.getService().setFrameCallback(this);
        }

        //TODO: 大华摄像头
//        mLoginModule = new IPLoginModule();
//        LoginTask loginTask = new LoginTask();
//        loginTask.execute();

        mRetrofit = MainApplication.getmRetrofit();

        //initLicense();

        //TODO: 海康威视
        initeSdk();
        //initHkvs();

        LoginHKTask loginTask = new LoginHKTask();
        loginTask.execute();
        Log.i(TAG, "onCreate end ");

        if (queryImgThread == null) {
            queryImgThread = new QueryImgThread();
            queryImgThread.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //TODO: 大华摄像头
//        if(null != mLoginModule) {
//            mLoginModule.logout();
//            mLoginModule = null;
//        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume  ");
        super.onResume();
        if (MainApplication.getInstance().getService() != null) {
            MainApplication.getInstance().getService().addGateObserver(this);
        }
        //TODO: 海康威视
        //viewVideo();

        //开启摄像头预览
        //startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CameraPreviewManager.getInstance().waitPreview();
        if (MainApplication.getInstance().getService() != null) {
            MainApplication.getInstance().getService().removeGateObserver(this);
        }
    }

    @Override
    protected void onDestroy() {
        if(playingHandle != -1){
            SDKGuider.g_sdkGuider.m_comPreviewGuider.RealPlay_Stop_jni(playingHandle);
            playingHandle = -1;
        }
        super.onDestroy();
        queryImgThread.exist();
        //unbindService(mConnection);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gosetting:
                //startActivity(new Intent(mContext, MainSettingActivity.class));
                break;
            case R.id.btn_exit:
                //finish();
                break;

            case R.id.btn_import:
                //通过指定的方式导入压缩的用户数据
                //ImportFileManager.getInstance().batchImport();
                break;

            case R.id.ib_back:
            case R.id.tv_back:
                this.finish();

                break;
            default:
        }
    }

    private void initView() {
        iv_camera = findViewById(R.id.iv_phone_camera);
        iv_success_curr = findViewById(R.id.iv_success_curr);
        iv_success_last1 = findViewById(R.id.iv_success_last1);
        iv_success_last2 = findViewById(R.id.iv_success_last2);
        iv_success_last3 = findViewById(R.id.iv_success_last3);
        iv_success_last4 = findViewById(R.id.iv_success_last4);
        iv_success_last5 = findViewById(R.id.iv_success_last5);
        iv_success_last6 = findViewById(R.id.iv_success_last6);
        iv_success_last7 = findViewById(R.id.iv_success_last7);
        iv_success_last8 = findViewById(R.id.iv_success_last8);
        ivs[0] = iv_success_last1;
        ivs[1] = iv_success_last2;
        ivs[2] = iv_success_last3;
        ivs[3] = iv_success_last4;
        ivs[4] = iv_success_last5;
        ivs[5] = iv_success_last6;
        ivs[6] = iv_success_last7;
        ivs[7] = iv_success_last8;
        tv_name = findViewById(R.id.tv_presentation_name);
        tv_company = findViewById(R.id.tv_presentation_company);
        tv_deputation = findViewById(R.id.tv_presentation_deputation);
        tv_auth = findViewById(R.id.tv_presentation_auth);
        tv_success_last_name1 = findViewById(R.id.iv_success_last_name1);
        tv_success_last_name2 = findViewById(R.id.iv_success_last_name2);
        tv_success_last_name3 = findViewById(R.id.iv_success_last_name3);
        tv_success_last_name4 = findViewById(R.id.iv_success_last_name4);
        tv_success_last_name5 = findViewById(R.id.iv_success_last_name5);
        tv_success_last_name6 = findViewById(R.id.iv_success_last_name6);
        tv_success_last_name7 = findViewById(R.id.iv_success_last_name7);
        tv_success_last_name8 = findViewById(R.id.iv_success_last_name8);
        tvs[0] = tv_success_last_name1;
        tvs[1] = tv_success_last_name2;
        tvs[2] = tv_success_last_name3;
        tvs[3] = tv_success_last_name4;
        tvs[4] = tv_success_last_name5;
        tvs[5] = tv_success_last_name6;
        tvs[6] = tv_success_last_name7;
        tvs[7] = tv_success_last_name8;

        iv_snap = findViewById(R.id.iv_test_snap);
        //初始化surfaceview
        sf = findViewById(R.id.surface_presentation);
        sf.getHolder().addCallback(this);
        //sf.setZOrderOnTop(true);
        //final Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getUserPic("c1000001"));
        if (bShowModel == MODEL_DAHUA) {
            iv_camera.setVisibility(View.INVISIBLE);
        }

        findViewById(R.id.ib_back).setOnClickListener(this);
        findViewById(R.id.tv_back).setOnClickListener(this);
    }

    @Override
    public void responseData(ReportData mReports) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                User user = new User();
                user.cardCode = mReports.getNumber();

                if ("FFFFFFFFFFFFFFFF".equals(mReports.getNumber())) {
                    return;
                }

                User res = UserService.getInstance().findUserByCardCode(user);

                if (res != null) {
                    //有此用户
                    //String path = FileUtils.getUserPic(res.image_name);
                    String path = FileUtils.getUserPic(res.code);
                    final Bitmap bitmap = BitmapFactory.decodeFile(path);
                    Log.i(TAG, " responseData image path: " + path);
                    iv_success_curr.setImageBitmap(bitmap);

                    tv_name.setText(res.name);
                    tv_company.setText(res.company);
                    tv_deputation.setText(res.position);
                    tv_auth.setText(AUTH_SUCCESS == res.auth ? "授权通过" : "授权未通过");
                    int len = users.length;

                    for (int i = len - 1; i > 0; i--) {
                        users[i] = users[i - 1];
                    }

                    users[0] = res;

                    for (int i = 0; i < len; i++) {
                        if (users[i] != null) {
                            ivs[i].setImageBitmap(BitmapFactory.decodeFile(FileUtils.getUserPic(users[i].code)));
                            tvs[i].setText(users[i].name);
                        }
                    }
                } else {
//                    tv_name.setText("未注册！");
                }
            }
        });
    }

    class QueryImgThread extends Thread {
        private boolean running = true;
        @Override
        public void run() {
            while (running) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (MainApplication.getInstance().isbQueryImgEnd()) {
                    try {
                        sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < users.length; i++) {
                                if (users[i] != null) {
                                    ivs[i].setImageBitmap(BitmapFactory.decodeFile(FileUtils.getUserPic(users[i].code)));
                                    tvs[i].setText(users[i].name);
                                }
                            }
                        }
                    });
                    MainApplication.getInstance().setbQueryImgEnd(false);
                }
            }
        }

        public void exist() {
            running = false;
        }
    }

    //TODO: 海康威视
    private boolean initeSdk() {
        // init net sdk
        if (!HCNetSDK.getInstance().NET_DVR_Init()) {
            Log.e(TAG, "HCNetSDK init is failed!");
            return false;
        }
        HCNetSDK.getInstance().NET_DVR_SetLogToFile(3, "/mnt/sdcard/sdklog/",
                true);
        return true;
    }

    //TODO: 海康威视
    private void initHkvs() {

    }

    private void viewVideo() {
        if (playingHandle != -1) {
            SDKGuider.g_sdkGuider.m_comPreviewGuider.RealPlay_Stop_jni(playingHandle);
        }

        NET_DVR_PREVIEWINFO struPlayInfo = new NET_DVR_PREVIEWINFO();
        struPlayInfo.lChannel = 1;
        struPlayInfo.dwStreamType = 0;
        struPlayInfo.bBlocked = 1;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sf = findViewById(R.id.surface_presentation);
                struPlayInfo.hHwnd = sf.getHolder();
                playingHandle = SDKGuider.g_sdkGuider.m_comPreviewGuider.RealPlay_V40_jni(hId, struPlayInfo, null);
                if (playingHandle < 0) {
                    Toast.makeText(GateActivityReverse.this, "播放预览失败, Err:" + SDKGuider.g_sdkGuider.GetLastError_jni(), Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(GateActivityReverse.this, "NET_DVR_RealPlay_V40 Succ ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //TODO: 海康威视
    private boolean loginDevice(String ip, String port, String loginName, String pswd) {
        DevManageGuider.DeviceItem deviceItem = SDKGuider.g_sdkGuider.m_comDMGuider.new DeviceItem();
        deviceItem.m_struNetInfo = SDKGuider.g_sdkGuider.m_comDMGuider.new DevNetInfo(
                ip,
                port,
                loginName,
                pswd);

        deviceItem.m_szDevName = deviceItem.m_struNetInfo.m_szIp;

        if (SDKGuider.g_sdkGuider.m_comDMGuider.login_v40_jna(deviceItem.m_szDevName, deviceItem.m_struNetInfo)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "登录设备成功!", Toast.LENGTH_LONG).show();

                }
            });

            SDKGuider.g_sdkGuider.m_comDMGuider.setCurrSelectDevIndex(0);
            return true;
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "登录设备失败，请查看设备是否联网 "+ SDKGuider.g_sdkGuider.GetLastError_jni(), Toast.LENGTH_LONG).show();

                }
            });
        }
        return false;
    }

    //设置当前显示的图片
    public void setCurrImage(Bitmap bitmap, User user) {
        iv_success_curr.post(new Runnable() {
            @Override
            public void run() {
                //set big
                iv_success_curr.setImageBitmap(bitmap);
//                if (user.cardCode.equals("E0040150C714EA6A")) {
//                    user.name = "孟州";
//                    user.company = "数展科技";
//                    user.position = "深圳代表团";
//                } else if (user.cardCode.equals("E0040150C715D092")) {
//                    user.name = "张红超";
//                    user.company = "数展科技";
//                    user.position = "深圳代表团";
//                } else if (user.cardCode.equals("E0040150C71459F3")) {
//                    user.name = "杨勇朝";
//                    user.company = "数展科技";
//                    user.position = "深圳代表团";
//                }
                tv_name.setText(user.name);
                tv_company.setText(user.company);
                tv_deputation.setText(user.position);
                tv_auth.setText(AUTH_SUCCESS == user.auth ? "授权通过" : "授权未通过");

                //设置底部轮换
                int len = users.length;
                for (int i = len - 1; i > 0; i--) {
                    users[i] = users[i - 1];
                }

                users[0] = user;

                for (int i = 0; i < len; i++) {
                    if (users[i] != null) {
                        ivs[i].setImageBitmap(BitmapFactory.decodeFile(FileUtils.getUserPic(users[i].image_name)));
                        tvs[i].setText(users[i].name);
                    }
                }
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        sf.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        if (-1 == playingHandle) {
            return;
        }
        Surface surface = holder.getSurface();
        if (surface.isValid()) {
            if (-1 == SDKGuider.g_sdkGuider.m_comPreviewGuider.RealPlaySurfaceChanged_jni(playingHandle, 0, holder))
                Toast.makeText(GateActivityReverse.this,"NET_DVR_PlayBackSurfaceChanged"+ SDKGuider.g_sdkGuider.GetLastError_jni(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "Player setVideoWindow release!" + hId);
        if (-1 == hId) {
            return;
        }
        if (holder.getSurface().isValid()) {
            if (-1 == SDKGuider.g_sdkGuider.m_comPreviewGuider.RealPlaySurfaceChanged_jni(playingHandle, 0, null))
            {
                Toast.makeText(GateActivityReverse.this,"NET_DVR_RealPlaySurfaceChanged"+ SDKGuider.g_sdkGuider.GetLastError_jni(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void push(Bitmap bitmap, User user, Bitmap cameraBitmap) {

    }

    //TODO: 百度人脸
    private void checkCloseResult(final LivenessModel livenessModel, final Bitmap cameraBitmap) {
        // 当未检测到人脸UI显示
        User user = null;
        if (livenessModel != null && livenessModel.getFaceInfo() != null) {
            user = livenessModel.getUser();
            if (user != null) {
                //是否是同一个用户在镜头前
                final Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getUserPic(user.image_name));
                Log.i(TAG, " face responseData image path: " + FileUtils.getUserPic(user.image_name));

                Log.i(TAG, "识别成功");
                User finalUser = user;

                iv_camera.post(new Runnable() {
                    @Override
                    public void run() {
                        String path = FileUtils.getUserPic(finalUser.image_name);
                        final Bitmap bitmap = BitmapFactory.decodeFile(path);
                        Log.i(TAG, " responseData image path: " + path);
                        setCurrImage(bitmap, finalUser);
                        if (callback != null) {
                            callback.push(bitmap, finalUser, cameraBitmap);
                        }
                    }
                });

            } else {
                Log.i(TAG, "识别失败");
            }
        } else {
            Log.i(TAG, "识别失败,未检测到人脸");
        }
    }

    //TODO: 百度人脸
    @Override
    public void onFrame(byte[] image) {
        final Bitmap finalBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

//        if (bShowModel == MODEL_CAMERA_STEAM) {
//            iv_camera.post(new Runnable() {
//                @Override
//                public void run() {
//                    iv_camera.setImageBitmap(finalBitmap);
//                }
//            });
//
//            goDetect(finalBitmap);
//        }
    }

    //TODO: 百度人脸
    private void goDetect(Bitmap bitmap) {
        FaceSDKManager.getInstance().onBitmapDetectCheck(bitmap, null, null,
                0, 0, 1, 3, new FaceDetectCallBack() {
                    @Override
                    public void onFaceDetectCallback(LivenessModel livenessModel) {
                        // 输出结果
                        checkCloseResult(livenessModel, bitmap);
                    }

                    @Override
                    public void onTip(int code, String msg) {
                        //displayTip(code, msg);
                    }

                    @Override
                    public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                        //showFrame(livenessModel);
                    }
                });
    }

    //TODO: 百度人脸识别sdk初始化
    private void initLicense() {
        Log.i(TAG, "initLicense!!!!");

        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().init(mContext, new SdkInitListener() {
                @Override
                public void initStart() {
                    Log.i(TAG, "init sdk start");
                }

                @Override
                public void initLicenseSuccess() {
                    Log.i(TAG, "initLicenseSuccess");
                }

                @Override
                public void initLicenseFail(int errorCode, String msg) {
                    // 如果授权失败，跳转授权页面
                    //ToastUtils.toast(mContext, errorCode + " " + msg);
                    // startActivity(new Intent(mContext, FaceAuthActicity.class));
                    Log.i(TAG, "initLicenseFail");
                }

                @Override
                public void initModelSuccess() {
                    Log.i(TAG, "initModelSuccess");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    Log.i(TAG, "initModelFail");
                }
            });
        }
    }

    //TODO: 大华摄像头登录
    private class LoginTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(String... params) {
            Log.i(TAG, " LoginTAsk doInBackground!!!");
            return mLoginModule.login("192.168.1.108", "37777", "admin", "dataexpo123");
        }
        @Override
        protected void onPostExecute(Boolean result){
            if (result) {
                app.setLoginHandle(mLoginModule.getLoginHandle());
                app.setDeviceInfo(mLoginModule.getDeviceInfo());

                mLiveModule = new LivePreviewModule(mContext);
                osdModule = new OSDModule(mContext);
                encodeModule = new EncodeModule(mContext);
                ptzControl = new PTZControl();
                mLiveModule.initSurfaceView(sf);
                if (mLiveModule.getHandle() == 0) {
//                    mLiveModule.startPlay(
//                            mSelectChannel.getSelectedItemPosition(),
//                            mSelectStream.getSelectedItemPosition(),
//                            mRealView);
                    Log.i(TAG, "live port = " + mLiveModule.getPlayPort());
                    //开始播放
                    mLiveModule.startPlay(0, 0, sf);

                    mCapturePictureModule = new CapturePictureModule(mContext);
                    //CapturePictureModule.localCapturePicture(mLiveModule.getPlayPort(), "123");

                    //设置远程抓图
                    CapturePictureModule.setSnapRevCallBack(new CB_fSnapRev() {
                        @Override
                        public void invoke(long l, byte[] bytes, int i, int i1, int i2) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            if (bitmap != null) {
                                iv_snap.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        iv_snap.setImageBitmap(bitmap);
                                        if (bShowModel == MODEL_DAHUA) {
                                            //使用回调的图片 进行人脸识别
                                            //goDetect(bitmap);
                                        }
                                    }
                                });
//                                Bitmap newBmp = zoomBitmap(bitmap, mPictureView.getWidth(), mPictureView.getHeight());
//                                mPictureView.setImageBitmap(newBmp);
//                                savePicture(data, data.length, msg.arg1);
                            }
                        }
                    });
                    mCapturePictureModule.timerCapturePicture(0);
                }
            } else {
                Log.i(TAG, "login error");
                //ToolKits.showMessage(IPLoginActivity.this, getErrorCode(getResources(), mLoginModule.errorCode()));
            }
        }
    }

    //TODO: 海康威视摄像头登录
    private class LoginHKTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(String... params) {
            Log.i(TAG, " LoginTAsk doInBackground!!!");
            String ip = "192.168.1.64";
            String port = "8000";
            String loginName = "admin";
            String pswd = "dataexpo123";

            return loginDevice(ip, port, loginName, pswd);
        }

        @Override
        protected void onPostExecute(Boolean result){
            if (result) {
                DevManageGuider.DeviceItem deviceInfo = SDKGuider.g_sdkGuider.m_comDMGuider.getCurrSelectDev();
                if(deviceInfo == null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GateActivityReverse.this,"获取设备失败，请查看摄像头是否联网",Toast.LENGTH_SHORT).show();

                        }
                    });
                    return;
                }
                hId = deviceInfo.m_lUserID;
                Log.i(TAG, "login id: " + hId);
                viewVideo();
            }
        }
    }
}
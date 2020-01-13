package com.dataexpo.autogate.activity;

import android.app.Presentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.dahuacameracommon.EncodeModule;
import com.dataexpo.autogate.dahuacameracommon.IPLoginModule;
import com.dataexpo.autogate.dahuacameracommon.LivePreviewModule;
import com.dataexpo.autogate.dahuacameracommon.NetSDKLib;
import com.dataexpo.autogate.dahuacameracommon.OSDModule;
import com.dataexpo.autogate.dahuacameracommon.PTZControl;
import com.dataexpo.autogate.face.callback.FaceDetectCallBack;
import com.dataexpo.autogate.face.manager.FaceSDKManager;
import com.dataexpo.autogate.face.model.LivenessModel;
import com.dataexpo.autogate.listener.OnFrameCallback;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.service.MainApplication;

public class SecondaryPhoneCameraPresentation extends Presentation implements OnFrameCallback, SurfaceHolder.Callback {
    private static final String TAG = SecondaryPhoneCameraPresentation.class.getSimpleName();
    //使用大华IPC摄像机的内容
    public static int MODEL_DAHUA = 1;
    //使用自制视频流的内容
    public static int MODEL_CAMERA_STEAM = 2;
    private Context context;
    private ImageView iv_camera;
    private ImageView iv_success_curr;
    private SurfaceView sf;

    private int bShowModel = MODEL_DAHUA;
    private IPLoginModule mLoginModule;
    private MainApplication app;
    private OSDModule osdModule;
    private EncodeModule encodeModule;
    private LivePreviewModule mLiveModule;
    private PTZControl ptzControl;

    public SecondaryPhoneCameraPresentation(Context outerContext, Display display) {
        super(outerContext, display);
        context = outerContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presentation_phone_camera);
        initView();
        /// 注意: 必须调用 init 接口初始化 INetSDK.jar 仅需要一次初始化
        NetSDKLib.getInstance().init();
        app = MainApplication.getInstance();
        if (app != null && app.getService() != null) {
            app.getService().setFrameCallback(this);
        }
        mLoginModule = new IPLoginModule();

        LoginTask loginTask = new LoginTask();
        loginTask.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(null != mLoginModule) {
            mLoginModule.logout();
            mLoginModule = null;
        }
    }

    private void initView() {
        iv_camera = findViewById(R.id.iv_phone_camera);
        iv_success_curr = findViewById(R.id.iv_success_curr);
        //初始化surfaceview
        sf = findViewById(R.id.surface_presentation);
        sf.getHolder().addCallback(this);

        final Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getUserPic("c1000001"));
        if (bShowModel == MODEL_DAHUA) {
            iv_camera.setVisibility(View.INVISIBLE);
        }

        iv_success_curr.post(new Runnable() {
            @Override
            public void run() {
                iv_success_curr.setImageBitmap(bitmap);
            }
        });
    }

    @Override
    public void onFrame(byte[] image) {
        //Log.i(TAG, "image length :" + image.length);
        final Bitmap finalBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

        if (bShowModel == MODEL_CAMERA_STEAM) {
            iv_camera.post(new Runnable() {
                @Override
                public void run() {
                    iv_camera.setImageBitmap(finalBitmap);
                }
            });
//            FaceSDKManager.getInstance().onBitmapDetectCheck(finalBitmap, null, null,
//                    0, 0, 1, 3, new FaceDetectCallBack() {
//                        @Override
//                        public void onFaceDetectCallback(LivenessModel livenessModel) {
//                            // 输出结果
//                            checkCloseResult(livenessModel);
//                        }
//
//                        @Override
//                        public void onTip(int code, String msg) {
//                            //displayTip(code, msg);
//                        }
//
//                        @Override
//                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
//                            //showFrame(livenessModel);
//                        }
//                    });
        }
    }

    private void checkCloseResult(final LivenessModel livenessModel) {
        // 当未检测到人脸UI显示
        User user = null;
        if (livenessModel != null && livenessModel.getFaceInfo() != null) {
            user = livenessModel.getUser();
            if (user != null) {
                //是否是同一个用户在镜头前
                final Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getUserPic(user.image_name));
                Log.i(TAG, " face responseData image path: " + FileUtils.getUserPic(user.image_name));


                Log.i(TAG, "识别成功");


            } else {
                Log.i(TAG, "识别失败");
            }
        } else {
            Log.i(TAG, "识别失败,未检测到人脸");
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    /// LoginTask
    private class LoginTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(String... params) {
            return mLoginModule.login("192.168.1.108", "37777", "admin", "dataexpo123");
        }
        @Override
        protected void onPostExecute(Boolean result){
            if (result) {

                app.setLoginHandle(mLoginModule.getLoginHandle());
                app.setDeviceInfo(mLoginModule.getDeviceInfo());

                mLiveModule = new LivePreviewModule(context);
                osdModule = new OSDModule(context);
                encodeModule = new EncodeModule(context);
                ptzControl = new PTZControl();
                mLiveModule.initSurfaceView(sf);
                if (mLiveModule.getHandle() == 0) {
//                    mLiveModule.startPlay(
//                            mSelectChannel.getSelectedItemPosition(),
//                            mSelectStream.getSelectedItemPosition(),
//                            mRealView);
                    mLiveModule.startPlay(0, 0, sf);
                }
            } else {
                Log.i("IPLoginActivity", "login error");
                //ToolKits.showMessage(IPLoginActivity.this, getErrorCode(getResources(), mLoginModule.errorCode()));
            }
        }
    }
}

package com.dataexpo.autogate.activity;

import android.app.Presentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.company.NetSDK.CB_fSnapRev;
import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.dahuacameracommon.CapturePictureModule;
import com.dataexpo.autogate.dahuacameracommon.EncodeModule;
import com.dataexpo.autogate.dahuacameracommon.IPLoginModule;
import com.dataexpo.autogate.dahuacameracommon.LivePreviewModule;
import com.dataexpo.autogate.dahuacameracommon.NetSDKLib;
import com.dataexpo.autogate.dahuacameracommon.OSDModule;
import com.dataexpo.autogate.dahuacameracommon.PTZControl;
import com.dataexpo.autogate.face.callback.FaceDetectCallBack;
import com.dataexpo.autogate.face.manager.FaceSDKManager;
import com.dataexpo.autogate.face.model.LivenessModel;
import com.dataexpo.autogate.listener.FaceOnSecCallback;
import com.dataexpo.autogate.listener.OnFrameCallback;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.service.MainApplication;

import static com.dataexpo.autogate.model.User.AUTH_SUCCESS;

public class SecondaryPhoneCameraPresentation extends Presentation implements OnFrameCallback, SurfaceHolder.Callback {
    private static final String TAG = SecondaryPhoneCameraPresentation.class.getSimpleName();
    //使用大华IPC摄像机的内容
    public static int MODEL_DAHUA = 1;
    //使用自制视频流的内容
    public static int MODEL_CAMERA_STEAM = 2;
    private Context context;
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

    public SecondaryPhoneCameraPresentation(Context outerContext, Display display) {
        super(outerContext, display);
        context = outerContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate!! ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presentation_phone_camera);
        for (int i = 0; i < users.length; i++) {
            users[i] = null;
        }
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

        //final Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getUserPic("c1000001"));
        if (bShowModel == MODEL_DAHUA) {
            iv_camera.setVisibility(View.INVISIBLE);
        }
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

            goDetect(finalBitmap);
        }
    }

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
            Log.i(TAG, " LoginTAsk doInBackground!!!");
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
                    Log.i(TAG, "live port = " + mLiveModule.getPlayPort());
                    mLiveModule.startPlay(0, 0, sf);

                    mCapturePictureModule = new CapturePictureModule(context);
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
                                            goDetect(bitmap);
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
                Log.i("IPLoginActivity", "login error");
                //ToolKits.showMessage(IPLoginActivity.this, getErrorCode(getResources(), mLoginModule.errorCode()));
            }
        }
    }

    public void setFaceDataCallback(FaceOnSecCallback callback) {
        this.callback = callback;
    }
}
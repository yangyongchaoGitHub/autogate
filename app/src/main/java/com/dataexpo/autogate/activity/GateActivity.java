package com.dataexpo.autogate.activity;

import android.app.Presentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.DBUtils;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.face.callback.CameraDataCallback;
import com.dataexpo.autogate.face.callback.FaceDetectCallBack;
import com.dataexpo.autogate.face.camera.AutoTexturePreviewView;
import com.dataexpo.autogate.face.camera.CameraPreviewManager;
import com.dataexpo.autogate.face.listener.SdkInitListener;
import com.dataexpo.autogate.face.manager.FaceSDKManager;
import com.dataexpo.autogate.face.model.LivenessModel;
import com.dataexpo.autogate.listener.OnServeiceCallback;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.service.FaceService;
import com.dataexpo.autogate.service.MainApplication;
import com.dataexpo.autogate.service.MainService;
import com.dataexpo.autogate.service.UserService;

public class GateActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = GateActivity.class.getSimpleName();
    private Context mContext;
    private ImageView iv_head;
    private ImageView iv_face;
    private TextView tv_name;
    private TextView tv_direction;
    private User faceCurrUser = null;
    private User GateCurrUser = null;

    private ServiceConnection mConnection;
    private AutoTexturePreviewView mAutoCameraPreviewView;

    private static final int PREFER_WIDTH = 1280;
    private static final int PERFER_HEIGH = 720;

    private SecondaryPhoneCameraPresentation presentation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate);
        mContext = this;
        initView();
        DBUtils.getInstance().create(mContext);
        //初始化双屏
        initDisplay();
        /**
         * test code add a user
         */

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 10000; i < 40000; i++) {
//                    UserService.getInstance().insert("test" + i, "dataexpo.Ltd", "development",
//                            "E0040150C7145" + i, 1, "u23" + i);
//                }
//            }
//        }).start();

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MainService.MsgBinder msgBinder = (MainService.MsgBinder) service;
                MainApplication.getInstance().setService(msgBinder.getService());
                msgBinder.getService().setCallback(onServeiceCallback);

                if (MainApplication.getInstance().getService() != null) {
                    MainApplication.getInstance().getService().removeGateObserver(GateActivity.this);
                    MainApplication.getInstance().getService().addGateObserver(GateActivity.this);
                    initDisplay();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };

        onServeiceCallback = new OnServeiceCallback() {
            @Override
            public void onCallback(int action) {
//                if (action == ACTION_TIMEOUT) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.i(TAG, "onServeiceCallback " + MainWindow.this.hasWindowFocus());
//
//                            startActivity(new Intent(mContext, ScreensaverActivity.class));
//
//                            Toast.makeText(mContext, "this is on long ting", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
            }
        };

        bindService(new Intent(getApplicationContext(), MainService.class), mConnection, Context.BIND_AUTO_CREATE);
        initLicense();
    }

    private void initDisplay() {
        DisplayManager mDisplayManager;//屏幕管理类
        Display[] displays;//屏幕数组
        mDisplayManager = (DisplayManager) this.getSystemService(Context.DISPLAY_SERVICE);

        displays = mDisplayManager.getDisplays();
        Log.i(TAG, "display: " + displays.length);


        Window window = null;

        if (displays.length > 1) {
            presentation = new SecondaryPhoneCameraPresentation(getApplicationContext(), displays[1]);//displays[1]是副屏
        }

        if (presentation != null) {
            window = presentation.getWindow();
            if (window != null) {
                window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                presentation.show();
            }
        }
    }

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

    private void startCamera() {
        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        CameraPreviewManager.getInstance().startPreview(mContext, mAutoCameraPreviewView,
                PREFER_WIDTH, PERFER_HEIGH, new CameraDataCallback() {
                    @Override
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        // 摄像头预览数据进行人脸检测
                        dealRgb(data, width, height);
                    }
                });

//        if (mLiveType == TYPE_RGBANDNIR_LIVE) {
//            if (mCamera_ir == null) {
//                mCamera_ir = Camera.open(1);
//                pt_ir.setCamera(mCamera_ir, PREFER_WIDTH, PERFER_HEIGH);
//            }
//            mCamera_ir.setPreviewCallback(new Camera.PreviewCallback() {
//                @Override
//                public void onPreviewFrame(byte[] data, Camera camera) {
//                    dealIr(data);
//                }
//            });
//        }

//        CameraPreviewManagerSingal cpms = new CameraPreviewManagerSingal();
//        cpms.setCameraFacing(CAMERA_FACING_FRONT);
//        cpms.startPreview(mContext, mAutoCameraPreviewView_ir, 640, 480, new CameraDataCallback() {
//            @Override
//            public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
//
//            }
//        });
    }

    private void initView() {
        iv_head = findViewById(R.id.iv_gate);
        iv_face = findViewById(R.id.iv_face);
        tv_name = findViewById(R.id.tv_gage_name);
        tv_direction = findViewById(R.id.tv_direction);

        findViewById(R.id.btn_gosetting).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        mAutoCameraPreviewView = findViewById(R.id.auto_camera_preview_view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gosetting:
                startActivity(new Intent(mContext, MainSettingActivity.class));
                break;
            case R.id.btn_exit:
                //finish();
                break;
                default:
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MainApplication.getInstance().getService() != null) {
            MainApplication.getInstance().getService().addGateObserver(this);
        }
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
        super.onDestroy();
        unbindService(mConnection);
    }

    @Override
    public void responseData(final ReportData mReports) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mReports != null) {
                    Log.i(TAG, "responseData card: " + mReports.getNumber() + " time " + mReports.getTime());
                    User user = new User();
                    user.cardCode = mReports.getNumber();

                    if ("FFFFFFFFFFFFFFFF".equals(mReports.getNumber())) {
                        iv_head.setVisibility(View.INVISIBLE);
                        tv_direction.setVisibility(View.INVISIBLE);
                        tv_name.setText("非法通过！");
                        return;
                    }

                    User res = UserService.getInstance().findUserByCardCode(user);

                    if (res != null) {
                        //有此用户
                        String path = FileUtils.getUserPic(res.image_name);
                        final Bitmap bitmap = BitmapFactory.decodeFile(path);
                        Log.i(TAG, " responseData image path: " + path);

                        iv_head.setImageBitmap(bitmap);
                        iv_head.setVisibility(View.VISIBLE);
                        tv_direction.setVisibility(View.VISIBLE);
                        tv_direction.setText("In".equals(mReports.getDirection()) ? "进" : "出");
                        tv_name.setText(res.name);

                        //调用分屏的显示接口
                        presentation.setCurrImage(bitmap, res);

                    } else {
                        iv_head.setVisibility(View.INVISIBLE);
                        tv_direction.setVisibility(View.INVISIBLE);
                        tv_name.setText("未注册！");
                    }
                }
            }
        });
    }

    private void dealRgb(byte[] data, int width, int height) {
//        if (mLiveType == TYPE_NO_LIVE ||
//                mLiveType == TYPE_RGB_LIVE) {
            FaceSDKManager.getInstance().onDetectCheck(data, null, null,
                    height, width, 1, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(LivenessModel livenessModel) {
                            // 输出结果
                            checkCloseResult(livenessModel);
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
//        } else if (mLiveType == TYPE_RGBANDNIR_LIVE) {
//            rgbData = data;
//            checkData();
//        }
    }

    private void checkCloseResult(final LivenessModel livenessModel) {
        // 当未检测到人脸UI显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                User user = null;
                if (livenessModel != null && livenessModel.getFaceInfo() != null) {
                    user = livenessModel.getUser();
                    if (user != null) {
                        //是否是同一个用户在镜头前
                        final Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getUserPic(user.image_name));
                        Log.i(TAG, " face responseData image path: " + FileUtils.getUserPic(user.image_name));

                        iv_face.setImageBitmap(bitmap);
                        iv_face.setVisibility(View.VISIBLE);
                        Log.i(TAG, "识别成功");

                        //保存用户人脸识别记录
                        if (faceCurrUser == null || faceCurrUser.id != user.id) {
                            faceCurrUser = user;
                            FaceService.getInstance().saveFaceRecord(livenessModel);
                        }
                    } else {
                        iv_face.setVisibility(View.INVISIBLE);
                        Log.i(TAG, "识别失败");
                    }
                } else {
                    iv_face.setVisibility(View.INVISIBLE);
                    //Log.i(TAG, "识别失败,未检测到人脸");
                }
            }
        });
    }
}

package com.dataexpo.autogate.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
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
import com.dataexpo.autogate.service.MainApplication;
import com.dataexpo.autogate.service.MainService;
import com.dataexpo.autogate.service.UserService;

import java.util.Vector;

import static com.dataexpo.autogate.face.model.BaseConfig.TYPE_RGBANDNIR_LIVE;
import static com.dataexpo.autogate.service.GateService.LED_GREEN;
import static com.dataexpo.autogate.service.GateService.LED_RED;

public class GateActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = GateActivity.class.getSimpleName();
    private Context mContext;
    private ImageView iv_head;
    private TextView tv_name;

    private ServiceConnection mConnection;
    private AutoTexturePreviewView mAutoCameraPreviewView;

    private static final int PREFER_WIDTH = 1280;
    private static final int PERFER_HEIGH = 720;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate);
        mContext = this;
        initView();
        DBUtils.getInstance().create(mContext);
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
        tv_name = findViewById(R.id.tv_gage_name);
        findViewById(R.id.btn_gosetting).setOnClickListener(this);
        mAutoCameraPreviewView = findViewById(R.id.auto_camera_preview_view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gosetting:
                startActivity(new Intent(mContext, MainSettingActivity.class));
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
        startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
    public void response(int status, final Vector<ReportData> mReports) {
        if (mReports != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mReports.size() > 0) {
                        ReportData data = mReports.get(0);
                        User user = new User();
//                        user.cardCode = "E0040150C714EA6B";
//                        user.code = "u23123";
//                        user.name = "测试用户";
                        user.cardCode = data.getNumber();
                        if ("FFFFFFFFFFFFFFFF".equals(data.getNumber())) {
                            Log.i(TAG, " GATE  FFFFFFFFFFFFFFFF!!!!!!!!!!!!!!");
                            MainApplication.getInstance().getService().ledCtrl(LED_RED);
                            iv_head.setVisibility(View.INVISIBLE);
                            tv_name.setText("非法通过！");
                            return;
                        }

                        User res = UserService.getInstance().findUserByCardCode(user);
                        if (res != null) {
                            //有此用户
                            final Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getUserPic(res.image_name));
                            Log.i(TAG, " response image path: " + FileUtils.getUserPic(res.image_name));

                            iv_head.setImageBitmap(bitmap);
                            iv_head.setVisibility(View.VISIBLE);
                            tv_name.setText(res.name);
                            MainApplication.getInstance().getService().ledCtrl(LED_GREEN);
                        }
                    }
                }
            });
        }
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
                if (livenessModel == null || livenessModel.getFaceInfo() == null) {
                    //Log.i(TAG, "未检测到人脸");
                    return;
                } else {
                    User user = livenessModel.getUser();
                    if (user == null) {
                        Log.i(TAG, "识别失败");

                    } else {
                        Log.i(TAG, "识别成功");
                    }
                }
            }
        });
    }
}

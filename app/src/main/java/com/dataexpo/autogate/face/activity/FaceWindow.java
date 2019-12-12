package com.dataexpo.autogate.face.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.dataexpo.autogate.R;
import com.dataexpo.autogate.activity.BascActivity;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.face.callback.CameraDataCallback;
import com.dataexpo.autogate.face.callback.FaceDetectCallBack;
import com.dataexpo.autogate.face.camera.AutoTexturePreviewView;
import com.dataexpo.autogate.face.camera.CameraPreviewManager;
import com.dataexpo.autogate.face.listener.SdkInitListener;
import com.dataexpo.autogate.face.manager.FaceSDKManager;
import com.dataexpo.autogate.face.manager.ImportFileManager;
import com.dataexpo.autogate.face.model.LivenessModel;
import com.dataexpo.autogate.face.model.SingleBaseConfig;
import com.dataexpo.autogate.face.model.User;
import com.dataexpo.autogate.face.utils.ConfigUtils;
import com.dataexpo.autogate.face.utils.DensityUtils;
import com.dataexpo.autogate.face.utils.FaceOnDrawTexturViewUtil;
import com.dataexpo.autogate.view.CircleImageView;
import com.dataexpo.autogate.view.LoginDialog;
import com.dataexpo.autogate.view.PreviewTexture;

import static com.dataexpo.autogate.face.model.BaseConfig.*;


public class FaceWindow extends BascActivity implements View.OnClickListener, LoginDialog.OnDialogClickListener {
    private static final String TAG = FaceWindow.class.getSimpleName();
    // 图片越大，性能消耗越大，也可以选择640*480， 1280*720
//    private static final int PREFER_WIDTH = 640;
//    private static final int PERFER_HEIGH = 480;
    private static final int PREFER_WIDTH = 1280;
    private static final int PERFER_HEIGH = 720;

    // 关闭Debug 模式
    private AutoTexturePreviewView mAutoCameraPreviewView;
    private TextureView mAutoCameraPreviewView_ir;
    PreviewTexture pt_ir = null;

    private Camera mCamera_ir = null;

    private volatile byte[] rgbData = null;
    private volatile byte[] irData = null;

    private TextView mDetectText;
    private CircleImageView mDetectImage;
    private TextView mTrackText;
    private TextureView mFaceDetectImageView;
    private Paint paint;
    private RectF rectF;
    private RelativeLayout relativeLayout;
    private int mLiveType;
    private float mRgbLiveScore;
    private float mNirLiveScore;
    private RelativeLayout info_rl;
    private Button btn_login;
    private LoginDialog mDialog;
    private EditText et_login;
    private boolean isConfigExit;
    private boolean isInitConfig;
    private ServiceConnection mConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_show);

        isConfigExit = ConfigUtils.isConfigExit();
        isInitConfig = ConfigUtils.initConfig();
        if (isInitConfig && isConfigExit) {
            Toast.makeText(mContext, "初始配置加载成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "初始配置失败,将重置文件内容为默认配置", Toast.LENGTH_SHORT).show();
            ConfigUtils.modityJson();
        }

        initView();
        //initLicense();
        // 屏幕的宽
        int displayWidth = DensityUtils.getDisplayWidth(mContext);
        // 屏幕的高
        int displayHeight = DensityUtils.getDisplayHeight(mContext);
        Log.i(TAG, "displayWidth: " + displayWidth + " displayHeight: " + displayHeight);

        // 当屏幕的宽大于屏幕高时
        if (displayHeight < displayWidth) {
            // 获取高
            int height = displayHeight;
            // 获取宽
            int width = (int) (displayHeight * ((9.0f / 16.0f)));
            // 设置布局的宽和高
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
            // 设置布局居中
            params.gravity = Gravity.CENTER;
            relativeLayout.setLayoutParams(params);
        }

        mDialog = new LoginDialog(mContext);
        mDialog.setDialogClickListener(this);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

//        mConnection = new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName name, IBinder service) {
//                BgService.MsgBinder msgBinder = (BgService.MsgBinder) service;
//                MainApplication.getInstance().setService(msgBinder.getService());
//                msgBinder.getService().setCallback(onServeiceCallback);
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//
//            }
//        };
//
//        onServeiceCallback = new OnServeiceCallback() {
//            @Override
//            public void onCallback(int action) {
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
//            }
//        };
//
//        //startService(new Intent(getApplicationContext(), BgService.class));
//        bindService(new Intent(getApplicationContext(), BgService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    private void initView() {
        // 活体阈值
        mRgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();

        //红外活体阈值
        mNirLiveScore = SingleBaseConfig.getBaseConfig().getNirLiveScore();
        // 获取整个布局
        relativeLayout = findViewById(R.id.all_relative);

        //获取底部的界面布局
        info_rl = findViewById(R.id.layout_info);
        //获取注册按钮
        //btn_login = findViewById(R.id.btn_logig);
        findViewById(R.id.btn_login).setOnClickListener(this);

        // 画人脸框
        paint = new Paint();
        rectF = new RectF();
        mFaceDetectImageView = findViewById(R.id.draw_detect_face_view);
        mFaceDetectImageView.setOpaque(false);
        mFaceDetectImageView.setKeepScreenOn(true);

        // 返回
        //Button mButReturn = findViewById(R.id.btn_back);
        //mButReturn.setOnClickListener(this);
        // 设置
        //Button mBtSetting = findViewById(R.id.btn_setting);
        //mBtSetting.setOnClickListener(this);

        // 单目摄像头RGB 图像预览
        mAutoCameraPreviewView = findViewById(R.id.auto_camera_preview_view);
        mAutoCameraPreviewView.setVisibility(View.VISIBLE);

        mAutoCameraPreviewView_ir = findViewById(R.id.auto_camera_preview_view_ir);
        pt_ir = new PreviewTexture(mContext, mAutoCameraPreviewView_ir);

        mDetectText = findViewById(R.id.detect_text);
        mDetectImage = findViewById(R.id.detect_reg_image_item);
        mTrackText = findViewById(R.id.track_txt);

        // 调试按钮
        //findViewById(R.id.debug_btn).setOnClickListener(this);
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

    @Override
    protected void onResume() {
        super.onResume();
        // 活体状态
        mLiveType = SingleBaseConfig.getBaseConfig().getType();

//        if (mLiveType == TYPE_NO_LIVE || mLiveType == TYPE_RGB_LIVE) {
//            //关灯
//            LEDServer.getInstance().close();
//        } else {
//            //开灯
//            LEDServer.getInstance().enable();
//        }
        startTestCloseDebugRegisterFunction();

//        if (MainApplication.getInstance().getService() != null) {
//            MainApplication.getInstance().getService().setCallback(onServeiceCallback);
//        }
    }

    private void startTestCloseDebugRegisterFunction() {
        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        CameraPreviewManager.getInstance().startPreview(mContext, mAutoCameraPreviewView,
                PREFER_WIDTH, PERFER_HEIGH, new CameraDataCallback() {
                    @Override
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        // 摄像头预览数据进行人脸检测
                        dealRgb(data, width, height);
                    }
                });

        if (mLiveType == TYPE_RGBANDNIR_LIVE) {
            if (mCamera_ir == null) {
                mCamera_ir = Camera.open(1);
                pt_ir.setCamera(mCamera_ir, PREFER_WIDTH, PERFER_HEIGH);
            }
            mCamera_ir.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    dealIr(data);
                }
            });
        }


//        CameraPreviewManagerSingal cpms = new CameraPreviewManagerSingal();
//        cpms.setCameraFacing(CAMERA_FACING_FRONT);
//        cpms.startPreview(mContext, mAutoCameraPreviewView_ir, 640, 480, new CameraDataCallback() {
//            @Override
//            public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
//
//            }
//        });
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
    }

    private void dealRgb(byte[] data, int width, int height) {
        if (mLiveType == TYPE_NO_LIVE ||
                mLiveType == TYPE_RGB_LIVE) {
            FaceSDKManager.getInstance().onDetectCheck(data, null, null,
                    height, width, mLiveType, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(LivenessModel livenessModel) {
                            // 输出结果
                            checkCloseResult(livenessModel);
                        }

                        @Override
                        public void onTip(int code, String msg) {
                            displayTip(code, msg);
                        }

                        @Override
                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                            showFrame(livenessModel);
                        }
                    });
        } else if (mLiveType == TYPE_RGBANDNIR_LIVE) {
            rgbData = data;
            checkData();
        }
    }

    private void dealIr(byte[] data) {
        if (mLiveType == TYPE_NO_LIVE ||
                mLiveType == TYPE_RGB_LIVE) {

        } else if (mLiveType == TYPE_RGBANDNIR_LIVE) {
            irData = data;
            checkData();
        }
    }

    private synchronized void checkData() {
        if (rgbData != null && irData != null) {
            //FaceSDKManager.getInstance().onDetectRgbNir(rgbData, irData, null, PERFER_HEIGH, PERFER_HEIGH, TYPE_RGBANDNIR_LIVE, 3,
            FaceSDKManager.getInstance().onDetectCheck(rgbData, irData, null, PERFER_HEIGH, PREFER_WIDTH, TYPE_RGBANDNIR_LIVE,
                    new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(LivenessModel livenessModel) {
                            checkResult(livenessModel);
                        }

                        @Override
                        public void onTip(int code, String msg) {
                            //displayTip(1,msg);
                            Log.i(TAG, "code: " + code + " " + msg);
                        }

                        @Override
                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                            showFrame(livenessModel);
                        }
                    });
            rgbData = null;
            irData = null;
        }
    }

    private void checkResult(final LivenessModel livenessModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel == null) {
                    mTrackText.setVisibility(View.GONE);
                    mDetectText.setText("未检测到人脸");
                    mDetectImage.setImageResource(R.drawable.icon_face_);
                    info_rl.setVisibility(View.GONE);
                    return;
                }

                //当检测到人脸时关闭屏保，显示人脸识别界面
//                if (MainApplication.getInstance().getService() != null) {
//                    MainApplication.getInstance().getService().touch();
//                }

//                BDFaceImageInstance image = livenessModel.getBdFaceImageInstance();
//                if (image != null) {
//                    mFaceDetectImageView.setImageBitmap(BitmapUtils.getInstaceBmp(image));
//                }
                float rgbLivenessScore = livenessModel.getRgbLivenessScore();
                float irLivenessScore = livenessModel.getIrLivenessScore();
                // 判断活体状态UI显示
                if (rgbLivenessScore < mRgbLiveScore) {
                    mTrackText.setVisibility(View.GONE);
                    mDetectText.setText("活体检测未通过");
                    mDetectText.setVisibility(View.VISIBLE);
                    mDetectImage.setImageResource(R.drawable.icon_face_);
                }
                if (irLivenessScore < mNirLiveScore) {
                    mDetectText.setText("活体检测未通过");
                    mDetectText.setVisibility(View.VISIBLE);
                    mDetectImage.setImageResource(R.drawable.icon_face_);
                }

                if (rgbLivenessScore > mRgbLiveScore && irLivenessScore > mNirLiveScore) {
                    User user = livenessModel.getUser();
                    if (user != null) {
                        String absolutePath = FileUtils.getBatchImportSuccessDirectory()
                                + "/" + user.getImageName();
                        Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);
                        mDetectImage.setImageBitmap(bitmap);
                        mTrackText.setVisibility(View.VISIBLE);
                        mTrackText.setText("识别成功");
                        mTrackText.setBackgroundColor(Color.rgb(66, 147, 136));
                        mDetectText.setText("欢迎您， " + user.getUserName());
                        info_rl.setVisibility(View.VISIBLE);

                    } else {
                        mTrackText.setVisibility(View.VISIBLE);
                        mTrackText.setText("识别失败");
                        mTrackText.setBackgroundColor(Color.RED);
                        mDetectText.setText("搜索不到用户");
                        mDetectText.setVisibility(View.VISIBLE);
                        mDetectImage.setImageResource(R.drawable.icon_face_);
                    }
                }

                Log.i(TAG, String.format("检测 ：%s ms", livenessModel.getRgbDetectDuration()));
                Log.i(TAG, String.format("RGB活体 ：%s ms", livenessModel.getRgbLivenessDuration()));
                Log.i(TAG, String.format("RGB得分 ：%s", livenessModel.getRgbLivenessScore()));
                Log.i(TAG, String.format("Ir活体 ：%s ms", livenessModel.getIrLivenessDuration()));
                Log.i(TAG, String.format("Ir得分 ：%s", livenessModel.getIrLivenessScore()));
                Log.i(TAG, String.format("特征抽取 ：%s ms", livenessModel.getFeatureDuration()));
                Log.i(TAG, String.format("检索比对 ：%s ms", livenessModel.getCheckDuration()));
                Log.i(TAG, String.format("总耗时 ：%s ms", livenessModel.getAllDetectDuration()));
            }
        });
    }

    private void checkCloseResult(final LivenessModel livenessModel) {
        // 当未检测到人脸UI显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel == null || livenessModel.getFaceInfo() == null) {
                    mTrackText.setVisibility(View.GONE);
                    mDetectText.setText("未检测到人脸");
                    mDetectImage.setImageResource(R.drawable.icon_face_);
                    info_rl.setVisibility(View.GONE);
                    return;
                } else {
                    //当检测到人脸时关闭屏保，显示人脸识别界面
//                    if (MainApplication.getInstance().getService() != null) {
//                        MainApplication.getInstance().getService().touch();
//                    }

                    if (mLiveType == 1) {
                        User user = livenessModel.getUser();
                        if (user == null) {
                            mTrackText.setVisibility(View.VISIBLE);
                            mTrackText.setText("识别失败");
                            mTrackText.setBackgroundColor(Color.RED);
                            mDetectText.setText("搜索不到用户");
                            mDetectText.setVisibility(View.VISIBLE);
                            mDetectImage.setImageResource(R.drawable.icon_face_);

                        } else {
                            String absolutePath = FileUtils.getBatchImportSuccessDirectory()
                                    + "/" + user.getImageName();
                            Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);
                            mDetectImage.setImageBitmap(bitmap);
                            mTrackText.setVisibility(View.VISIBLE);
                            mTrackText.setText("识别成功");
                            mTrackText.setBackgroundColor(Color.rgb(66, 147, 136));
                            mDetectText.setText("欢迎您， " + user.getUserName());
                            info_rl.setVisibility(View.VISIBLE);
                        }
                    } else {
                        float rgbLivenessScore = livenessModel.getRgbLivenessScore();
                        if (rgbLivenessScore < mRgbLiveScore) {
                            mTrackText.setVisibility(View.VISIBLE);
                            mTrackText.setText("识别失败");
                            mTrackText.setBackgroundColor(Color.RED);
                            mDetectText.setText("活体检测未通过");
                            mDetectImage.setImageResource(R.drawable.icon_face_);
                        } else {
                            User user = livenessModel.getUser();
                            if (user == null) {
                                mTrackText.setVisibility(View.VISIBLE);
                                mTrackText.setText("识别失败");
                                mTrackText.setBackgroundColor(Color.RED);
                                mDetectText.setText("搜索不到用户");
                                mDetectText.setVisibility(View.VISIBLE);
                                mDetectImage.setImageResource(R.drawable.icon_face_);
                            } else {

                                String absolutePath = FileUtils.getBatchImportSuccessDirectory()
                                        + "/" + user.getImageName();
                                Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);
                                mDetectImage.setImageBitmap(bitmap);
                                mTrackText.setVisibility(View.VISIBLE);
                                mTrackText.setText("识别成功");
                                mTrackText.setBackgroundColor(Color.rgb(66, 147, 136));
                                mDetectText.setText("欢迎您， " + user.getUserName());
                            }
                        }
                    }
                }
            }
        });
    }

    private void displayTip(final int code, final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (code == 0) {
                    mTrackText.setVisibility(View.GONE);
                } else {
                    mTrackText.setVisibility(View.VISIBLE);
                    mTrackText.setText("识别失败");
                    mTrackText.setBackgroundColor(Color.RED);
                    mDetectImage.setImageResource(R.drawable.icon_face_);
                }
                mDetectText.setText(tip);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                mDialog.show();
                break;
                default:
        }
    }

    /**
     * 绘制人脸框。
     */
    private void showFrame(final LivenessModel model) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Canvas canvas = mFaceDetectImageView.lockCanvas();
                if (canvas == null) {
                    mFaceDetectImageView.unlockCanvasAndPost(canvas);
                    return;
                }
                if (model == null) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mFaceDetectImageView.unlockCanvasAndPost(canvas);
                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mFaceDetectImageView.unlockCanvasAndPost(canvas);
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));

                // 检测图片的坐标和显示的坐标不一样，需要转换。
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        mAutoCameraPreviewView, model.getBdFaceImageInstance());
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                // 绘制框
                canvas.drawRect(rectF, paint);
                mFaceDetectImageView.unlockCanvasAndPost(canvas);
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy!!!!");
        super.onDestroy();
        CameraPreviewManager.getInstance().stopPreview();
        ImportFileManager.getInstance().release();
        if (mCamera_ir != null) {
            mCamera_ir.setPreviewCallback(null);
            mCamera_ir.stopPreview();
            //pt_ir.release();
            mCamera_ir.release();
            mCamera_ir = null;
        }
        unbindService(mConnection);
    }

    @Override
    public void onConfirmClick(View view) {
        //LogUtils.i(TAG, mDialog.et_pswd.getText().toString());
        //startActivity(new Intent(this, BasicSetActivity.class));
        mDialog.dismiss();
        CameraPreviewManager.getInstance().stopPreview();
        //finish();
    }

    @Override
    public void onModifierClick(View view) {
        mDialog.dismiss();
    }
}
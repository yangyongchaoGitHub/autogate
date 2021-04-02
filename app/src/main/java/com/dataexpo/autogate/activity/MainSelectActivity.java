package com.dataexpo.autogate.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.DBUtils;
import com.dataexpo.autogate.dahuacameracommon.NetSDKLib;
import com.dataexpo.autogate.face.camera.CameraPreviewManager;
import com.dataexpo.autogate.listener.OnServeiceCallback;
import com.dataexpo.autogate.service.MainApplication;
import com.dataexpo.autogate.service.MainService;

/**
 * 主界面 6个选择按钮
 */
public class MainSelectActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = MainSelectActivity.class.getSimpleName();

    //ui资源
    private ImageButton ib_monitor;
    private ImageButton ib_register;
    private ImageButton ib_recorde;
    private ImageButton ib_sync;
    private ImageButton ib_net;
    private ImageButton ib_mode;
    private ImageButton ib_clear;

    private ServiceConnection mConnection;

    //分屏实体
    private SecondaryPhoneCameraPresentationReverse presentation = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_select);
        mContext = this;
        DBUtils.getInstance().create(mContext);
        NetSDKLib.getInstance().init();

        //初始化后台服务
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MainService.MsgBinder msgBinder = (MainService.MsgBinder) service;
                MainApplication.getInstance().setService(msgBinder.getService());
                msgBinder.getService().setCallback(onServeiceCallback);

                if (MainApplication.getInstance().getService() != null) {
                    //初始化双屏
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
        initView();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Log.i(TAG, "width: " + dm.widthPixels + " height: " + dm.heightPixels);
    }

    private void initDisplay() {
        //分屏调试接口  可删除
//        MainApplication app = MainApplication.getInstance();
//        NetSDKLib.getInstance().init();
//        if (app != null && app.getService() != null) {
//            app.getService().setFrameCallback(this);
//        }
//
//        mLoginModule = new IPLoginModule();
//
//        LoginTask loginTask = new LoginTask();
//        loginTask.execute();
//
//        if (1 == 1) {
//            return;
//        }

        DisplayManager mDisplayManager;//屏幕管理类
        Display[] displays;//屏幕数组
        mDisplayManager = (DisplayManager) this.getSystemService(Context.DISPLAY_SERVICE);

        displays = mDisplayManager.getDisplays();
        Log.i(TAG, "display: " + displays.length);


        Window window = null;

        if (displays.length > 1) {
            presentation = new SecondaryPhoneCameraPresentationReverse(getApplicationContext(), displays[1]);//displays[1]是副屏
        }

        if (presentation != null) {
            window = presentation.getWindow();
            if (window != null) {
                window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                presentation.setFaceDataCallback(this);
                presentation.show();
            }
        }
    }

    private void initView() {
        ib_monitor = findViewById(R.id.tl_1);
        ib_register = findViewById(R.id.tl_2);
        ib_recorde = findViewById(R.id.tl_3);
        ib_sync = findViewById(R.id.tl_4);
        ib_net = findViewById(R.id.bl_1);
        ib_mode = findViewById(R.id.bl_2);
        ib_clear = findViewById(R.id.bl_3);

        ib_monitor.setOnClickListener(this);
        ib_register.setOnClickListener(this);
        ib_recorde.setOnClickListener(this);
        ib_sync.setOnClickListener(this);
        ib_net.setOnClickListener(this);
        ib_mode.setOnClickListener(this);
        ib_clear.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tl_1:
                //到监控界面
                startActivity(new Intent(mContext, GateActivityReverse.class));
                break;

            case R.id.tl_2:
                startActivity(new Intent(mContext, ServiceRegisterActivity.class));
                break;

            case R.id.tl_3:
                startActivity(new Intent(mContext, RecordsActivity.class));
                break;

            case R.id.tl_4:
                startActivity(new Intent(mContext, OtherActivity.class));
                break;
            case R.id.bl_1:
                break;
            case R.id.bl_2:
                break;
            case R.id.bl_3:
                break;
            default:
        }
    }
}

package com.dataexpo.autogate.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.core.content.PermissionChecker;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.DBUtils;
import com.dataexpo.autogate.dahuacameracommon.NetSDKLib;
import com.dataexpo.autogate.face.camera.CameraPreviewManager;
import com.dataexpo.autogate.listener.OnServeiceCallback;
import com.dataexpo.autogate.service.MainApplication;
import com.dataexpo.autogate.service.MainService;

/**
 * 主界面 8个选择按钮
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
    private ImageButton ib_8;

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

        hideBottomUIMenu();

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

        //获取允许出现在其他应用上权限
        int hasAlertWindow = PermissionChecker.checkSelfPermission(this, Settings.ACTION_MANAGE_OVERLAY_PERMISSION);

        Log.i("---------------------", hasAlertWindow + " " + PermissionChecker.PERMISSION_GRANTED);

        //if (hasAlertWindow != PermissionChecker.PERMISSION_GRANTED) {
        if (!Settings.canDrawOverlays(this)) {
            //requestPerssionArr.add(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);

            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private void initDisplay() {
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
        ib_8 = findViewById(R.id.bl_4);

        ib_monitor.setOnClickListener(this);
        ib_register.setOnClickListener(this);
        ib_recorde.setOnClickListener(this);
        ib_sync.setOnClickListener(this);
        ib_net.setOnClickListener(this);
        ib_mode.setOnClickListener(this);
        ib_clear.setOnClickListener(this);
        ib_8.setOnClickListener(this);
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
                //到设备注册界面
                startActivity(new Intent(mContext, ServiceRegisterActivity.class));
                break;

            case R.id.tl_3:
                //到日志界面
                startActivity(new Intent(mContext, RecordsActivity.class));
                break;

            case R.id.tl_4:
                //到网络环境检测界面
                startActivity(new Intent(mContext, OtherActivity.class));
                break;

            case R.id.bl_1:
                //到同步界面
                startActivity(new Intent(mContext, SyncActivity.class));
                break;

            case R.id.bl_2:
                //到用户界面
                startActivity(new Intent(mContext, UsersActivity.class));
                break;

            case R.id.bl_3:
                //模式选择
                Toast.makeText(mContext, "当前设备仅支持普通模式 ", Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(mContext, ModelSelectActivity.class));
                break;

            case R.id.bl_4:
                //到清理数据界面
                startActivity(new Intent(mContext, ClearDataActivity.class));
                break;
            default:
        }
    }

    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}

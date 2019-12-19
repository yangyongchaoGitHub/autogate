package com.dataexpo.autogate.activity.faceset;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.activity.BascActivity;
import com.dataexpo.autogate.listener.OnServeiceCallback;

public class ScreensaverActivity extends BascActivity {
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private DevicePolicyManager policyManager;
    private ComponentName adminReceiver;
    private int i = 1;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_screensaver);
//        initView();
//        onServeiceCallback = new OnServeiceCallback() {
//            @Override
//            public void onCallback(final int action) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (action == ACTION_HAVE_FACE) {
//                                Log.i("ScreensaverActivity", "ACTION_HAVE_FACE-----");
//                                if (MainApplication.getInstance().getService() != null) {
//                                    MainApplication.getInstance().getService().touch();
//                                    MainApplication.getInstance().getService().setCallback(null);
//                                }
//                                checkScreenOn();
//                                ScreensaverActivity.this.finish();
//
//                            } else if (action == ACTION_SCREEN_OFF) {
//                                checkScreenOff();
//                            }
//                        }
//                    });
//            }
//        };
//        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
//        adminReceiver = new ComponentName(this, ScreenReceiver.class);
//        policyManager = (DevicePolicyManager) this.getSystemService(Context.DEVICE_POLICY_SERVICE);
//    }
//
//    @Override
//    protected void onResume() {
//        if (MainApplication.getInstance().getService() != null) {
//            if (MainApplication.getInstance().getService().getmScreenStatus() != STATUS_SCREENSAVE) {
//                finish();
//            } else {
//                MainApplication.getInstance().getService().setCallback(onServeiceCallback);
//            }
//        }
//
//        checkScreen();
//        super.onResume();
//    }
//
//    private void initView() {
//        findViewById(R.id.iv_screensave).setOnClickListener(this);
//        findViewById(R.id.btn_screensava_test_btn).setOnClickListener(this);
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btn_screensava_test_btn:
//                if ((i++ & 0x01) == 0) {
//                    checkScreenOn();
//                } else {
//                    checkScreenOff();
//                }
//                break;
//
//            default:
//                if (MainApplication.getInstance().getService() != null) {
//                    MainApplication.getInstance().getService().touch();
//                    checkScreenOn();
//                }
//                this.finish();
//        }
//    }

    /**
     * 使设备屏亮
     */
    @SuppressLint("InvalidWakeLockTag")
    public void checkScreenOn() {
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
        mWakeLock.acquire();
        mWakeLock.release();
    }

    /**
     * 使设备息屏
     */
    public void checkScreenOff() {
        boolean admin = policyManager.isAdminActive(adminReceiver);
        if (admin) {
            policyManager.lockNow();
        } else {
            showToast("没有设备管理权限");
        }
    }

    public void checkScreen() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean screenOn = pm.isScreenOn();
        if (!screenOn) {//如果灭屏
            //相关操作
            showToast("屏幕是息屏");
        } else {
            showToast("屏幕是亮屏");
        }
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}

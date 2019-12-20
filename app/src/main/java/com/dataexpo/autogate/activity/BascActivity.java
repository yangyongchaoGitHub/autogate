package com.dataexpo.autogate.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.content.PermissionChecker;

import com.dataexpo.autogate.listener.GateObserver;
import com.dataexpo.autogate.listener.MQTTObserver;
import com.dataexpo.autogate.listener.OnGateServiceCallback;
import com.dataexpo.autogate.listener.OnServeiceCallback;
import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.service.UserService;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Vector;

public class BascActivity extends Activity implements GateObserver, MQTTObserver {
    public Context mContext;
    public OnServeiceCallback onServeiceCallback;
    public OnGateServiceCallback onGateServiceCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 将activity设置为全屏显示
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestPermissions(99);
    }

    // 请求权限
    public void requestPermissions(int requestCode) {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                ArrayList<String> requestPerssionArr = new ArrayList<>();
                int hasCamrea = checkSelfPermission(Manifest.permission.CAMERA);
                if (hasCamrea != PackageManager.PERMISSION_GRANTED) {
                    requestPerssionArr.add(Manifest.permission.CAMERA);
                }

                int hasSdcardRead = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                if (hasSdcardRead != PackageManager.PERMISSION_GRANTED) {
                    requestPerssionArr.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }

                int hasSdcardWrite = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasSdcardWrite != PackageManager.PERMISSION_GRANTED) {
                    requestPerssionArr.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }

                //获取允许出现在其他应用上权限
//                int hasAlertWindow = PermissionChecker.checkSelfPermission(this, Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//
//                Log.i("---------------------", hasAlertWindow + " " + PermissionChecker.PERMISSION_GRANTED);
//
//                if (hasAlertWindow != PermissionChecker.PERMISSION_GRANTED) {
//                    //requestPerssionArr.add(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//
//                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                            Uri.parse("package:" + getPackageName()));
//                    startActivity(intent);
//                }
                // 是否应该显示权限请求
                if (requestPerssionArr.size() >= 1) {
                    String[] requestArray = new String[requestPerssionArr.size()];
                    for (int i = 0; i < requestArray.length; i++) {
                        requestArray[i] = requestPerssionArr.get(i);
                    }
                    requestPermissions(requestArray, requestCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        boolean flag = false;
        for (int i = 0; i < permissions.length; i++) {
            if (PackageManager.PERMISSION_GRANTED == grantResults[i]) {
                flag = true;
            }
        }
    }

    @Override
    public void responseData(Vector<ReportData> mReports) {
    }

    @Override
    public void responseStatus(int status) {
    }

    @Override
    public void responseMQTTStatus(int status) {
    }
}

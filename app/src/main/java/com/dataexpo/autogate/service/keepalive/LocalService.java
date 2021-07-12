package com.dataexpo.autogate.service.keepalive;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.dataexpo.autogate.IMyAidlInterface;
import com.dataexpo.autogate.activity.MainSelectActivity;
import com.dataexpo.autogate.service.MainApplication;


public class LocalService extends Service {
    private static final String TAG = LocalService.class.getName();
    private MyBinder mBinder;


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IMyAidlInterface iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            try {
                Log.e(TAG, "connected with " + iMyAidlInterface.getName());
                if (MainApplication.getMainActivity() == null) {
                    Intent intent = new Intent(LocalService.this.getBaseContext(), MainSelectActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplication().startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Toast.makeText(LocalService.this, "链接断开，重新启动 RemoteService", Toast.LENGTH_LONG).show();
            Log.e(TAG, "onServiceDisconnected: 链接断开，重新启动 RemoteService");
            startService(new Intent(LocalService.this, RemoteService.class));
            bindService(new Intent(LocalService.this, RemoteService.class), connection, Context.BIND_IMPORTANT);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: LocalService 启动");
        //Toast.makeText(this, "LocalService 启动", Toast.LENGTH_LONG).show();
        startService(new Intent(LocalService.this, RemoteService.class));
        bindService(new Intent(LocalService.this, RemoteService.class), connection, Context.BIND_IMPORTANT);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mBinder = new MyBinder();
        return mBinder;
    }


    private class MyBinder extends IMyAidlInterface.Stub {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public String getName() throws RemoteException {
            return LocalService.class.getName();
        }
    }
}

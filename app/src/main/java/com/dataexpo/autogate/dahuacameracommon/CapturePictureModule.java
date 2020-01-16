package com.dataexpo.autogate.dahuacameracommon;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.company.NetSDK.CB_fSnapRev;
import com.company.NetSDK.INetSDK;
import com.company.NetSDK.SNAP_PARAMS;
import com.company.PlaySDK.Constants;
import com.company.PlaySDK.IPlaySDK;
import com.dataexpo.autogate.R;
import com.dataexpo.autogate.service.MainApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 41299 on 2018/7/6.
 */
public class CapturePictureModule {
    private static final String TAG = CapturePictureModule.class.getSimpleName();
    private Resources res;
    private long m_hLoginHandle;
    public int m_nChanNum;

    public CapturePictureModule(Context context) {
        res = context.getResources();
        m_hLoginHandle = MainApplication.getInstance().getLoginHandle();
        m_nChanNum = MainApplication.getInstance().getDeviceInfo().nChanNum;
    }

    /**
     * 本地抓图
     */
    public static boolean localCapturePicture(int nPort, String picFileName) {

        if (IPlaySDK.PLAYCatchPicEx(nPort, picFileName, Constants.PicFormat_JPEG) == 0) {
            ToolKits.writeLog("localCapturePicture Failed!");
            return false;
        }
        return true;
    }

    /**
     * 远程抓图
     */
    public boolean remoteCapturePicture(int chn) {
        return snapPicture(chn, 0, 0);
    }

    /**
     * 定时抓图
     */
    public boolean timerCapturePicture(int chn) {
        return snapPicture(chn, 1, 2);
    }

    /**
     * 停止定时抓图
     */
    public boolean stopCapturePicture(int chn) {
        return snapPicture(chn, -1, 0);
    }

    /**
     * 抓图
     */
    private boolean snapPicture(int chn, int mode, int interval) {
        Log.i(TAG, "snapPicture!!");
        // 发送抓图命令给前端设备，抓图的信息
        SNAP_PARAMS stuSnapParams = new SNAP_PARAMS();
        stuSnapParams.Channel = chn;  			// 抓图通道
        stuSnapParams.mode = mode;    			// 抓图模式
        stuSnapParams.Quality = 3;				// 画质
        stuSnapParams.InterSnap = interval; 	// 定时抓图时间间隔
        stuSnapParams.CmdSerial = 0;  			// 请求序列号，有效值范围 0~65535，超过范围会被截断为

        if (!INetSDK.SnapPictureEx(m_hLoginHandle, stuSnapParams)) {
            ToolKits.writeLog("SnapPictureEx Failed!");
            return false;
        }
        return true;
    }

    public static void setSnapRevCallBack(CB_fSnapRev snapReceiveCB){
        //设置抓图回调函数， 图片主要在m_SnapReceiveCB中返回
        INetSDK.SetSnapRevCallBack(snapReceiveCB);
    }

    /**
     * 获取要显示的通道号
     */
    public List getChannelList(){
        ArrayList<String> channelList = new ArrayList<String>();
        for (int i=0;i<m_nChanNum;i++){
            channelList.add(res.getString(R.string.channel)+(i));
        }
        return channelList;
    }

    /**
     * 是否显示通道
     */
//    public boolean isNotShowChannel(){
//        return m_nChanNum == 1;
//    }
}

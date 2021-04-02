package com.dataexpo.autogate.hikvision.control;

import android.util.Log;

import com.dataexpo.autogate.hikvision.hcnetsdk.jna.HCNetSDKJNAInstance;
import com.hikvision.netsdk.HCNetSDK;

public class SDKGuider {
    static public SDKGuider g_sdkGuider = new SDKGuider();

    //ISAPI协议透传接口
    public DevTransportGuider m_comTransportGuider = new DevTransportGuider();
    //串口透传接口
    public DevPassThroughGuider m_comSerialTransGuider = new DevPassThroughGuider();
    //设备管理接口
    public DevManageGuider m_comDMGuider = new DevManageGuider();
    //设备远程设置接口
    public DevConfigGuider m_comConfGuider = new DevConfigGuider();
    //设备报警接口
    public DevAlarmGuider m_comDevAlarmGuider = new DevAlarmGuider();
    //关于回放接口
    public DevPlayBackGuider m_comPBGuider = new DevPlayBackGuider();
    //预览相关接口
    public DevPreviewGuider m_comPreviewGuider = new DevPreviewGuider();
    //SDK初始化
    public SDKGuider(){
        initNetSdk_jna();
    }
    //清理
    public void finalize()
    {
        cleanupNetSdk_jna();
    }

    /**
     * @fn GetLastError_jni
     * @param None.
     * @return 返回错误码
     * @brief 获取SDK错误码
     */
    public int GetLastError_jni()
    {
        return HCNetSDK.getInstance().NET_DVR_GetLastError();
    }

    /**
     * @fn initNetSdk_jni
     * @param None.
     * @return 成功初始化NetSDK,返回True,否则False
     * @brief NetSDK初始化.
     */
    private boolean initNetSdk_jni()
    {
        // init net sdk
        if (!HCNetSDK.getInstance().NET_DVR_Init())
        {
            Log.e("[NetSDKSimpleDemo]", "HCNetSDK init is failed!");
            return false;
        }
        HCNetSDK.getInstance().NET_DVR_SetLogToFile(3, "/mnt/sdcard/sdklog/", true);
        return true;
    }

    /**
     * @fn initNetSdk_jna
     * @param None.
     * @return 成功初始化NetSDK,返回True,否则False
     * @brief NetSDK初始化.
     */
    private boolean initNetSdk_jna()
    {
        // init net sdk
        if (!HCNetSDKJNAInstance.getInstance().NET_DVR_Init())
        {
            Log.e("[NetSDKSimpleDemo]", "HCNetSDK init is failed!");
            return false;
        }
        HCNetSDKJNAInstance.getInstance().NET_DVR_SetLogToFile(3, "/mnt/sdcard/sdklog/", true);
        return true;
    }

    /**
     * @fn cleanupNetSdk_jni
     * @param None.
     * @return 成功返回True,否则False
     * @brief NetSDK反初始化.
     */
    private boolean cleanupNetSdk_jni()
    {
        // init net sdk
        if (!HCNetSDK.getInstance().NET_DVR_Cleanup())
        {
            Log.e("[NetSDKSimpleDemo]", "HCNetSDK cleanup is failed!");
            return false;
        }
        return true;
    }

    /**
     * @fn cleanupNetSdk_jna
     * @param None.
     * @return 成功返回True,否则False
     * @brief NetSDK反初始化.
     */
    private boolean cleanupNetSdk_jna()
    {
        // init net sdk
        if (!HCNetSDKJNAInstance.getInstance().NET_DVR_Cleanup())
        {
            Log.e("[NetSDKSimpleDemo]", "HCNetSDK cleanup is failed!");
            return false;
        }
        return true;
    }
}

package com.dataexpo.autogate.dahuacameracommon;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceView;

import com.company.NetSDK.CB_fRealDataCallBackEx;
import com.company.NetSDK.INetSDK;
import com.company.NetSDK.SDK_RealPlayType;
import com.company.PlaySDK.Constants;
import com.company.PlaySDK.IPlaySDK;
import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.service.MainApplication;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 29779 on 2017/4/8.
 */
public class LivePreviewModule {
    private static final String TAG = LivePreviewModule.class.getSimpleName();
    private final int STREAM_BUF_SIZE = 1024 * 1024 * 2;
    private final int RAW_AUDIO_VIDEO_MIX_DATA = 0; ///原始音视频混合数据;  ///Raw audio and video mixing data.
    long mRealHandle = 0;
    static Context mContext;
    Resources res;
    int mPlayPort = 0;
    int mCurVolume = -1;
    boolean isRecording = false;
    boolean isOpenSound = true;
    boolean isDelayPlay = false;
    Map<Integer, Integer> streamTypeMap = new HashMap<Integer, Integer>();
    SurfaceView surface;

    /// for preview date callback
    private CB_fRealDataCallBackEx mRealDataCallBackEx;

    public LivePreviewModule(Context context) {
        this.mContext = context;
        res = mContext.getResources();
        mPlayPort = IPlaySDK.PLAYGetFreePort();
        isOpenSound = true;
        isDelayPlay = false;
        initMap();
    }

    ///码流类型的hash
    private void initMap() {
        streamTypeMap.put(0, SDK_RealPlayType.SDK_RType_Realplay_0);
        streamTypeMap.put(1, SDK_RealPlayType.SDK_RType_Realplay_1);
    }

    ///视频预览前设置
    public boolean prePlay(SurfaceView sv) {
        surface = sv;
        boolean isOpened = IPlaySDK.PLAYOpenStream(mPlayPort, null, 0, STREAM_BUF_SIZE) == 0 ? false : true;
        if (!isOpened) {
            Log.d(TAG, "OpenStream Failed");
            return false;
        }
        boolean isPlayin = IPlaySDK.PLAYPlay(mPlayPort, sv) == 0 ? false : true;
        if (!isPlayin) {
            Log.d(TAG, "PLAYPlay Failed");
            IPlaySDK.PLAYCloseStream(mPlayPort);
            return false;
        }

        if (isOpenSound) {
            boolean isSuccess = IPlaySDK.PLAYPlaySoundShare(mPlayPort) == 0 ? false : true;
            if (!isSuccess) {
                Log.d(TAG, "SoundShare Failed");
                IPlaySDK.PLAYStop(mPlayPort);
                IPlaySDK.PLAYCloseStream(mPlayPort);
                return false;
            }
            if (-1 == mCurVolume) {
                mCurVolume = IPlaySDK.PLAYGetVolume(mPlayPort);
            } else {
                IPlaySDK.PLAYSetVolume(mPlayPort, mCurVolume);
            }
        }

        if (isDelayPlay) {
            if (IPlaySDK.PLAYSetDelayTime(mPlayPort, 500/*ms*/, 1000/*ms*/) == 0) {
                Log.d(TAG, "SetDelayTime Failed");
            }
        }

        return true;
    }

    public long getHandle() {
        return this.mRealHandle;
    }

    public int getPlayPort() {
        return this.mPlayPort;
    }

    public void setOpenSound(boolean isOpenSound) {
        this.isOpenSound = isOpenSound;
    }

    public void setDelayPlay(boolean isDelayPlay) {
        this.isDelayPlay = isDelayPlay;
    }

    ///开始预览视频
    public void startPlay(int channel, int streamType, final SurfaceView view) {
        Log.d(TAG, "StreamTpye: " + streamTypeMap.get(streamType));
        mRealHandle = INetSDK.RealPlayEx(MainApplication.getInstance().getLoginHandle(), channel, streamTypeMap.get(streamType));
        if (mRealHandle == 0) {
            //ToolKits.writeErrorLog("RealPlayEx failed!");
            Log.i(TAG, "RealPlayEx failed!");
            return;
        }

        if (!prePlay(view)) {
            Log.d(TAG, "prePlay returned false..");
            INetSDK.StopRealPlayEx(mRealHandle);
            return;
        }
        if (mRealHandle != 0) {
            mRealDataCallBackEx = new CB_fRealDataCallBackEx() {
                @Override
                public void invoke(long rHandle, int dataType, byte[] buffer, int bufSize, int param) {
                    //Log.v(TAG,"dataType:"+dataType+"; bufSize:"+bufSize+"; param:"+param);
                    if (RAW_AUDIO_VIDEO_MIX_DATA == dataType) {
//                        Log.i(TAG,"dataType == 0");
                        IPlaySDK.PLAYInputData(mPlayPort, buffer, buffer.length);
                        if (bufSize > 10000 && test++ < 200) {

                            //利用surface进行截图
                            //Bitmap bitmap = Bitmap.createBitmap(700, 570, Bitmap.Config.ARGB_8888);
//                            Canvas bitCanvas = new Canvas(bitmap);
//                            surface.draw(bitCanvas);
//                            File facePicDir = FileUtils.getBatchImportSuccessDirectory();
//                            File savePicPath = new File(facePicDir, "test" + test + ".jpg");
//                            FileUtils.saveBitmap(savePicPath, bitmap);

//                            //进行本地截图
//                            File facePicDir = FileUtils.getBatchImportSuccessDirectory();
//                            File savePicPath = new File(facePicDir, "test" + test + ".jpg");
//                            Log.i(TAG, "save to " + savePicPath.getAbsolutePath());
//                            Log.i(TAG, "snap result:" + IPlaySDK.PLAYCatchPicEx(101, savePicPath.getAbsolutePath(), Constants.PicFormat_JPEG));
                        }
                    }
                }
            };
            INetSDK.SetRealDataCallBackEx(mRealHandle, mRealDataCallBackEx, 1);
        }
    }

    static int test = 0;

    ///停止预览视频
    public void stopRealPlay() {
        if (mRealHandle == 0) {
            return;
        }

        try {
            IPlaySDK.PLAYStop(mPlayPort);
            if (isOpenSound) {
                IPlaySDK.PLAYStopSoundShare(mPlayPort);
            }
            IPlaySDK.PLAYCloseStream(mPlayPort);
            INetSDK.StopRealPlayEx(mRealHandle);
            IPlaySDK.PLAYRefreshPlay(mPlayPort);
            if (isRecording) {
                INetSDK.StopSaveRealData(mRealHandle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRealHandle = 0;
        isRecording = false;
    }

    ///初始化视频窗口
    public void initSurfaceView(final SurfaceView sv) {
        if (sv == null)
            return;
        IPlaySDK.InitSurface(mPlayPort, sv);
    }

    public boolean isRealPlaying() {
        return mRealHandle != 0;
    }

    ///获取通道数量
    public int getChannel() {
        if (MainApplication.getInstance() == null)
            return 0;
        return MainApplication.getInstance().getDeviceInfo().nChanNum;
    }

    ///获取要显示的通道号
    public List getChannelList() {
        ArrayList<String> channelList = new ArrayList<String>();
        for (int i = 0; i < getChannel(); i++) {
            channelList.add(res.getString(R.string.channel) + (i));
        }
        return channelList;
    }

    ///获取要显示的码流类型
    public List getStreamTypeList(int channel) {
        ArrayList<String> list = new ArrayList<String>();
        // int stream = getTypeMask(channel);
        String[] streamNames = res.getStringArray(R.array.stream_type_array);
        for (int i = 0; i < 2; i++) {
            //if ((stream & (0x01 << 1))!=0)
            list.add(streamNames[i]);
        }
        return list;
    }

    public boolean record(boolean recordFlag) {
        if (mRealHandle == 0) {
            Log.i(TAG, "record error");
            return false;
        }

        Log.i(TAG, "ExternalFilesDir:" + mContext.getExternalFilesDir(null).getAbsolutePath());
        isRecording = recordFlag;
        if (isRecording) {
            String recordFile = createInnerAppFile("dav");
            if (!INetSDK.SaveRealData(mRealHandle, recordFile)) {
                Log.i(TAG, "record file:" + recordFile);
                return false;
            }
        } else {
            INetSDK.StopSaveRealData(mRealHandle);
        }
        return true;
    }

    public synchronized static String createInnerAppFile(String suffix) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format.format(new Date());
        String file = mContext.getExternalFilesDir(null).getAbsolutePath() + "/" + time.replace(":", "-").replace(" ", "_") +
                "." + suffix;
        return file;
    }
}

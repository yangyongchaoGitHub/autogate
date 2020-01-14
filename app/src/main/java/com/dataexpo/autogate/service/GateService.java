package com.dataexpo.autogate.service;

import android.content.Context;
import android.util.Log;

import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.listener.GateObserver;
import com.dataexpo.autogate.listener.OnGateServiceCallback;
import com.dataexpo.autogate.listener.GateSubject;
import com.dataexpo.autogate.model.gate.ReportData;
import com.rfid.api.ADReaderInterface;
import com.rfid.api.GFunction;
import com.rfid.def.ApiErrDefinition;
import com.rfid.def.RfidDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static com.dataexpo.autogate.comm.Utils.GATE_DIRECTION_SET;
import static com.dataexpo.autogate.comm.Utils.GATE_IP;
import static com.dataexpo.autogate.comm.Utils.GATE_PORT;


public class GateService extends GateSubject {
    private static final String TAG = GateService.class.getSimpleName();

    //设置的端口或者ip为空
    public static final int GATE_STATUS_INIT_NULL_SETTING = 1;
    //接口open成功
    public static final int GATE_STATUS_START = 2;
    //正常运行中
    public static final int GATE_STATUS_RUN = 3;
    //打开接口错误
    public static final int GATE_STATUS_INIT_OPEN_FAIL = 4;
    //上下文为空
    public static final int GATE_STATUS_INIT_NULL_CONTEXT = 5;
    //和设备通信失败
    public static final int GATE_STATUS_READ_ERROR = 6;

    //红灯
    public static final int LED_RED = 1;
    //绿灯
    public static final int LED_GREEN = 2;

    private boolean bGetReportThrd = true;
    private Thread mGetReportThrd = null;
    private int mStatus;
    private Context mContext;
    private OnGateServiceCallback callback;

    static ADReaderInterface m_reader = new ADReaderInterface();

    private static class HolderClass {
        private static final GateService instance = new GateService();
    }

    /**
     * 单例模式
     */
    public static GateService getInstance() {
        return HolderClass.instance;
    }

    void setContext(Context context) {
        if (context != null) {
            mContext =  context;
        }
    }

    /**
     *
     * @param no 控制命令中的端口编号
     * @param model 控制模式（暂时不知道是干嘛用的）
     */
    public static void ledOption(byte no, byte model) {
        Object dnOutputOper = m_reader.RDR_CreateSetOutputOperations();
        m_reader.RDR_AddOneOutputOperation(dnOutputOper, no, model,1,
                100, 100);
        int iret = m_reader.RDR_SetOutput(dnOutputOper);
        Log.i(TAG, "setOption result" + iret);
    }

    public void restart() {
        bGetReportThrd = false;
        try {
            mGetReportThrd.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mGetReportThrd = null;

        if (m_reader.isReaderOpen()) {
            m_reader.RDR_Close();
        }
        start();
    }

    //获取设备信息
    private boolean getDeviceInfo() {
        StringBuffer buffer = new StringBuffer();
        int iret = m_reader.RDR_GetReaderInfor(buffer);
        //获取设备信息成功，说明正常通信
        return iret == ApiErrDefinition.NO_ERROR;
    }

    @Override
    public void notifyGate(ReportData mReports) {
        for(Object obs: gateObservers) {
            ((GateObserver)obs).responseData(mReports);
        }
    }

    @Override
    public void notifyStatus(int status) {
        for(Object obs: gateObservers) {
            ((GateObserver)obs).responseStatus(mStatus);
        }
    }

    void setCallback(OnGateServiceCallback onGateServiceCallback) {
        callback = onGateServiceCallback;
    }

    public int getmStatus() {
        return mStatus;
    }

    int init() {
        if (mContext == null) {
            mStatus = GATE_STATUS_INIT_NULL_CONTEXT;
            return mStatus;
        }

        String ip = Utils.getGATEConfig(mContext, GATE_IP);
        String port = Utils.getGATEConfig(mContext, GATE_PORT);

        if ("".equals(ip) || "".equals(port)) {
            //其中有一个为空则为非法
            mStatus = GATE_STATUS_INIT_NULL_SETTING;
            return mStatus;
        }

        String conStr = String.format(
                "RDType=G302;CommType=NET;RemoteIp=%s;RemotePort=%s",
                ip, port);
        int iret = m_reader.RDR_Open(conStr);

        if (iret != ApiErrDefinition.NO_ERROR) {
            mStatus = GATE_STATUS_INIT_OPEN_FAIL;
        } else {
            mStatus = GATE_STATUS_START;
        }

        return mStatus;
    }

    public void start() {
        mGetReportThrd = new Thread(new GetReportThrd());
        mGetReportThrd.start();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                int i = 0;
//                while (i++ < 15) {
//                    try {
//                        Thread.sleep(3000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    ReportData data;
//                    if (i == 0) {
//                        data = new ReportData("E0040150C71459F3", 123, "123");
//                    } else if (i == 1) {
//                        data = new ReportData("E0040150C715D092", 123, "123");
//                    } else {
//                        data = new ReportData("E0040150C714EA6A", 123, "123");
//                    }
//
//                    notifyGate(data);
//                }
//            }
//        }).start();
    }

    public void ledCtrl(int target) {
        if (target == LED_RED) {
            ledOption((byte)2,(byte)3);
        } else if (target == LED_GREEN) {
            ledOption((byte)3,(byte)3);
        }
    }

    private class GetReportThrd implements Runnable
    {
        public void run() {
            bGetReportThrd = true;
            int iret;
            byte flag = 0;

            while (bGetReportThrd) {
                if (init() == GATE_STATUS_START) {
                    if (getDeviceInfo()) {
                        break;
                    }
                    mStatus = GATE_STATUS_READ_ERROR;
                }

                //notifyObserver(mStatus, null);
                notifyStatus(mStatus);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            mStatus = GATE_STATUS_RUN;

            notifyStatus(mStatus);
            //notifyObserver(mStatus, null);

            while (bGetReportThrd) {
                iret = m_reader.RDR_BuffMode_FetchRecords(flag);
                if (iret == 0) {
                    flag = 1;
                    int nCount = m_reader.RDR_GetTagReportCount();

                    if (nCount > 0) {
                        Object hReport = m_reader.RDR_GetTagDataReport(RfidDef.RFID_SEEK_FIRST);

                        while (hReport != null) {
                            byte[] reportBuf = new byte[64];
                            int[] nSize = new int[1];
                            iret = ADReaderInterface.RDR_ParseTagDataReportRaw(
                                    hReport, reportBuf, nSize);

                            if (iret == 0) {
                                if (nSize[0] < 9) {
                                    hReport = m_reader
                                            .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
                                    continue;
                                }
                                // byte evntType;
                                byte direction;
                                byte[] time = new byte[6];
                                int dataLen;

                                // evntType = reportBuf[0];
                                direction = reportBuf[1];
                                for (int i = 0; i < 6; i++) {
                                    time[i] = reportBuf[2 + i];
                                }
                                dataLen = reportBuf[8];
                                if (dataLen < 0) {
                                    dataLen += 256;
                                }
                                nSize[0] -= 9;
                                if (nSize[0] < dataLen) {
                                    hReport = m_reader
                                            .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
                                    continue;
                                }

                                byte[] byData = new byte[dataLen];
                                System.arraycopy(reportBuf, 9, byData, 0,
                                        dataLen);
                                //获取是否取反通过方向
                                String dire = Utils.getGATEConfig(mContext, GATE_DIRECTION_SET);

                                direction = (byte)("".equals(dire) ? 1 : Integer.parseInt(dire) == 2 ?
                                        (direction == 1 ? 2 : 1) : direction);

                                ReportData report = new ReportData(GFunction.encodeHexStr(byData),
                                        direction, GFunction.encodeHexStr(time));

                                notifyGate(report);
                                hReport = m_reader
                                        .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
                            }
                        }
                    }
                }
//                else {
//                    notifyGate(null);
//                }
            }
            /**
             * 可以发送反馈数据
             */
        }
    }
}

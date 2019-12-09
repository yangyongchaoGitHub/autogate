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

import java.util.Vector;


public class GateService extends GateSubject {
    private static final String TAG = GateService.class.getSimpleName();
    public static final String CONFIG_DEFAULT_IP = "default_ip";
    public static final String CONFIG_DEFAULT_PORT = "default_port";

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

    /**
     *
     * @param no 控制命令中的端口编号
     * @param model 控制模式（暂时不知道是干嘛用的）
     */
    public static void testOption(byte no, byte model) {
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

        if (m_reader.isReaderOpen())
        {
            m_reader.RDR_Close();
        }

        start();
    }

    @Override
    public void notifyObserver(int status, Vector<ReportData> mReports) {
        for(Object obs: gateObservers)
        {
            ((GateObserver)obs).response(status, mReports);
        }
    }

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

    void setCallback(OnGateServiceCallback onGateServiceCallback) {
        callback = onGateServiceCallback;
    }

    public int getmStatus() {
        return mStatus;
    }

    int init() {
        Log.i(TAG, "init status " + mStatus);
        if (mContext == null) {
            mStatus = GATE_STATUS_INIT_NULL_CONTEXT;
            return mStatus;
        }

        String ip = Utils.getConfig(mContext, CONFIG_DEFAULT_IP);
        String port = Utils.getConfig(mContext, CONFIG_DEFAULT_PORT);

        if ("".equals(ip) || "".equals(port)) {
            //其中有一个为空则为非法
            mStatus = GATE_STATUS_INIT_NULL_SETTING;
            return mStatus;
        }

        String conStr = String.format(
                "RDType=G302;CommType=NET;RemoteIp=%s;RemotePort=%s",
                ip, port);
        int iret = m_reader.RDR_Open(conStr);

        if (iret != ApiErrDefinition.NO_ERROR)
        {
            mStatus = GATE_STATUS_INIT_OPEN_FAIL;
        } else {
            mStatus = GATE_STATUS_START;
        }

        return mStatus;
    }

    public void start() {
        mGetReportThrd = new Thread(new GetReportThrd());
        mGetReportThrd.start();
    }

    public void ledCtrl(int target) {
        if (target == LED_RED) {
            testOption((byte)2,(byte)3);
        } else if (target == LED_GREEN) {
            testOption((byte)3,(byte)3);
        }
    }

    private class GetReportThrd implements Runnable
    {
        public void run()
        {
            bGetReportThrd = true;
            int iret;
            byte flag = 0;
            int test = 0;

            while (bGetReportThrd) {
                if (init() == GATE_STATUS_START) {
                    break;
                }

                notifyObserver(mStatus, null);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            mStatus = GATE_STATUS_RUN;

            notifyObserver(mStatus, null);

            while (bGetReportThrd) {
                iret = m_reader.RDR_BuffMode_FetchRecords(flag);
                if (iret == 0) {
                    flag = 1;
                    int nCount = m_reader.RDR_GetTagReportCount();

                    if (nCount > 0)
                    {
                        Vector<ReportData> mReports = new Vector<ReportData>();
                        Object hReport = m_reader
                                .RDR_GetTagDataReport(RfidDef.RFID_SEEK_FIRST);
                        while (hReport != null)
                        {
                            byte[] reportBuf = new byte[64];
                            int[] nSize = new int[1];
                            iret = ADReaderInterface.RDR_ParseTagDataReportRaw(
                                    hReport, reportBuf, nSize);
                            if (iret == 0)
                            {
                                if (nSize[0] < 9)
                                {
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
                                for (int i = 0; i < 6; i++)
                                {
                                    time[i] = reportBuf[2 + i];
                                }
                                dataLen = reportBuf[8];
                                if (dataLen < 0)
                                {
                                    dataLen += 256;
                                }
                                nSize[0] -= 9;
                                if (nSize[0] < dataLen)
                                {
                                    hReport = m_reader
                                            .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
                                    continue;
                                }

                                byte[] byData = new byte[dataLen];
                                System.arraycopy(reportBuf, 9, byData, 0,
                                        dataLen);
                                ReportData report = new ReportData();
                                report.setNumber(GFunction.encodeHexStr(byData));
                                report.setDirection(direction == 1 ? "In" : (direction == 2 ? "Out" : "null"));
                                report.setTime(GFunction.encodeHexStr(time));
                                mReports.add(report);
                                hReport = m_reader
                                        .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
                            }
                        }

                        notifyObserver(mStatus, mReports);
                    }
                } else {
//                    if (test++ == 10) {
//                        Vector<ReportData> mReports = new Vector<ReportData>();
//                        notifyObserver(mStatus, mReports);
//                        Log.i(TAG, "teset notifyObserver!!!!!");
//                        test = 0;
//                    } else {
                        notifyObserver(mStatus, null);
                    //}
                }
            }
            /**
             * 可以发送反馈数据
             */
        }
    }
}

package com.dataexpo.autogate.service;

import android.content.Context;
import android.util.Log;

import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.listener.GateObserver;
import com.dataexpo.autogate.listener.OnGateServiceCallback;
import com.dataexpo.autogate.listener.GateSubject;
import com.dataexpo.autogate.model.Rfid;
import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.service.data.RfidService;
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
    //设备不存在
    public static final int GATE_STATUS_NULL = 7;

    //红灯
    public static final int LED_RED = 1;
    //绿灯
    public static final int LED_GREEN = 2;

    private GetReportThrd mGetReportThrd = null;
    private InitThrd initThrd = null;
    private boolean rStatus = false;
    private boolean rStatus_i = false;

    private Context mContext;
    private OnGateServiceCallback callback;

    //通道门实体
    static List<Rfid> rfids= null;

    //static ADReaderInterface m_reader = new ADReaderInterface();

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
    public static void ledOption(byte no, byte model, Rfid rfid) {
        //TODO: 还要更改多个门
        if (rfid != null) {
            try {
                Object dnOutputOper = rfid.m_reader.RDR_CreateSetOutputOperations();
                rfid.m_reader.RDR_AddOneOutputOperation(dnOutputOper, no, model,1,
                        100, 100);
                int iret = rfid.m_reader.RDR_SetOutput(dnOutputOper);
                Log.i(TAG, "setOption result" + iret);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void restart() {
        try {
            mGetReportThrd.closeThread();
            initThrd.closeThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "restart 0" + rStatus + " " + rStatus_i);
        while (rStatus && rStatus_i) {
            break;
        }
        mGetReportThrd = null;
        initThrd = null;

        //Log.i(TAG, "restart 1" + rStatus + " " + rStatus_i);
        for (Rfid r: rfids) {
            Log.i(TAG, "close --- " + r.getId());
            if (r.m_reader.isReaderOpen()) {
                r.m_reader.RDR_Close();
            }
        }

        //Log.i(TAG, "restart 2");
        start();
    }

    //获取设备信息
    private boolean getDeviceInfo(Rfid rfid) {
        StringBuffer buffer = new StringBuffer();
        int iret = rfid.m_reader.RDR_GetReaderInfor(buffer);
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
    public void notifyStatus(Rfid rfid) {
        for(Object obs: gateObservers) {
            ((GateObserver)obs).responseStatus(rfid);
        }
    }

    void setCallback(OnGateServiceCallback onGateServiceCallback) {
        callback = onGateServiceCallback;
    }

    public int getmStatus(int id) {
        int result = GATE_STATUS_NULL;
        for (int i = 0; i < rfids.size(); i++) {
            if (rfids.get(i).getId() == id) {
                result = rfids.get(i).status;
                break;
            }
        }
        return result;
    }

    int init(Rfid rfid) {
        if (mContext == null) {
            return GATE_STATUS_INIT_NULL_CONTEXT;
        }

//        String ip = Utils.getGATEConfig(mContext, GATE_IP);
//        String port = Utils.getGATEConfig(mContext, GATE_PORT);
        //String ip = "192.168.1.222";

//        String ip = "192.168.1.170";
//        String port = "6012";

        String ip = rfid.getIp();
        String port = rfid.getPort();

        if ("".equals(ip) || "".equals(port)) {
            //其中有一个为空则为非法
            return GATE_STATUS_INIT_NULL_SETTING;
        }

        String conStr = String.format(
                "RDType=G302;CommType=NET;RemoteIp=%s;RemotePort=%s",
                ip, port);
        Log.i(TAG, " connect: " + conStr);
        int iret = rfid.m_reader.RDR_Open(conStr);

        if (iret != ApiErrDefinition.NO_ERROR) {
            return GATE_STATUS_INIT_OPEN_FAIL;
        } else {
            return GATE_STATUS_START;
        }
    }

    public void start() {
        rfids = RfidService.getInstance().listAll();

        initThrd = new InitThrd();
        rStatus_i = true;
        initThrd.start();

        mGetReportThrd = new GetReportThrd();
        rStatus = true;
        mGetReportThrd.start();
    }

    public void ledCtrl(int target, Rfid rfid) {
        if (target == LED_RED) {
            ledOption((byte)2,(byte)3, rfid);
        } else if (target == LED_GREEN) {
            ledOption((byte)3,(byte)3, rfid);
        }
    }

    private class InitThrd extends Thread {
        private boolean bGetReportThrd = true;

        public void closeThread() {
            bGetReportThrd = false;
        }
        @Override
        public void run() {
            int statusChange;

            while (bGetReportThrd) {
                for (Rfid r: rfids) {
                    statusChange = r.status;
                    if (System.currentTimeMillis() - r.lastConnect > 1000 && r.status != GATE_STATUS_RUN) {
                        if (r.status == GATE_STATUS_START) {
                            if (getDeviceInfo(r)) {
                                r.status = GATE_STATUS_RUN;
                                Log.i(TAG, "----  1 GATE_STATUS_RUNGATE_STATUS_RUNGATE_STATUS_RUNGATE_STATUS_RUN");
                            }
                        } else {
                            r.status = init(r);
                        }

                        r.lastConnect = System.currentTimeMillis();

//                        initResult = init(r);
//                        if (initResult == GATE_STATUS_START) {
//                            //获取设备信息，获取到说明通信正常
//                            if (getDeviceInfo(r)) {
//                                r.status = GATE_STATUS_RUN;
//                                Log.i(TAG, "---- GATE_STATUS_RUNGATE_STATUS_RUNGATE_STATUS_RUNGATE_STATUS_RUN");
//                            } else {
//                                r.status = initResult;
//                            }
//                        } else {
//                            r.status = initResult;
//                        }
                    }
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (statusChange != r.status) {
                        Log.i(TAG, "---- GATE_STATUS_RUNGATE_STATUS_RUNGATE_STATUS_RUNGATE_STATUS_RUN notifyStatus");
                        notifyStatus(r);
                    }
                }
            }
            rStatus_i = false;
        }
    }

    private class GetReportThrd extends Thread {
        private boolean bGetReportThrd = true;

        public void closeThread() {
            bGetReportThrd = false;
        }

        public void run() {
            int iret, initResult;
            byte[] reportBuf, time, byData;
            int[] nSize;
            byte direction;
            ReportData report;

            byte flag = 0;

            while (bGetReportThrd) {
                try {
                    sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for (Rfid r: rfids) {
                    if (r.status == GATE_STATUS_RUN) {
                        if (r.m_reader == null) {
                            continue;
                        }

                        try {
                            iret = r.m_reader.RDR_BuffMode_FetchRecords(flag);
                            if (iret == 0) {
                                flag = 1;
                                int nCount = r.m_reader.RDR_GetTagReportCount();

                                if (nCount > 0) {
                                    Object hReport = r.m_reader.RDR_GetTagDataReport(RfidDef.RFID_SEEK_FIRST);

                                    while (hReport != null) {
                                        reportBuf = new byte[64];
                                        nSize = new int[1];
                                        iret = ADReaderInterface.RDR_ParseTagDataReportRaw(
                                                hReport, reportBuf, nSize);

                                        if (iret == 0) {
                                            if (nSize[0] < 9) {
                                                hReport = r.m_reader
                                                        .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
                                                continue;
                                            }
                                            // byte evntType;

                                            time = new byte[6];
                                            int dataLen;

                                            // evntType = reportBuf[0];
                                            direction = reportBuf[1];

                                            System.arraycopy(reportBuf, 2, time, 0, 6);

                                            dataLen = reportBuf[8];
                                            if (dataLen < 0) {
                                                dataLen += 256;
                                            }
                                            nSize[0] -= 9;
                                            if (nSize[0] < dataLen) {
                                                hReport = r.m_reader
                                                        .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
                                                continue;
                                            }

                                            byData = new byte[dataLen];


                                            System.arraycopy(reportBuf, 9, byData, 0,
                                                    dataLen);

//                                            for (byte b:byData) {
//                                                System.out.print(b + " ");
//                                            }
//                                            System.out.println("-");
                                            //获取是否取反通过方向
                                            String dire = Utils.getGATEConfig(mContext, GATE_DIRECTION_SET);

//                                            direction = (byte)("".equals(dire) ? 1 : Integer.parseInt(dire) == 2 ?
//                                                    (direction == 1 ? 2 : 1) : direction);

                                            report = new ReportData(r, GFunction.encodeHexStr(byData),
                                                    direction, GFunction.encodeHexStr(time));

                                            notifyGate(report);
                                            hReport = r.m_reader
                                                    .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
                                        }
                                    }
                                }
                            } else {
                                r.status = GATE_STATUS_READ_ERROR;
                                Log.i(TAG, "读取的数据出错 将重新连接读取？？？？？？？？？？？？？？ ");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            Log.i(TAG, "GetReportThrd end" + this.getName());
            rStatus = false;
        }
    }
}

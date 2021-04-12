package com.dataexpo.autogate.listener;

import com.dataexpo.autogate.model.Rfid;
import com.dataexpo.autogate.model.gate.ReportData;

import java.util.List;
import java.util.Vector;

public interface GateObserver {
    void responseData(ReportData mReports);
    void responseStatus(Rfid rfid);
}

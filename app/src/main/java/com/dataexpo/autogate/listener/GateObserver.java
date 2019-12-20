package com.dataexpo.autogate.listener;

import com.dataexpo.autogate.model.gate.ReportData;

import java.util.Vector;

public interface GateObserver {
    void response(Vector<ReportData> mReports);
}

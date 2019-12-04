package com.dataexpo.autogate.listener;

import com.dataexpo.autogate.model.gate.ReportData;

import java.util.Vector;

public interface OnGateServiceCallback {
    void onCallback(int status, Vector<ReportData> mReports);
}

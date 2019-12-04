package com.dataexpo.autogate.listener;

import com.dataexpo.autogate.model.gate.ReportData;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public abstract class GateSubject {
    protected List<GateObserver> gateObservers =new ArrayList<GateObserver>();

    public void add(GateObserver gateObserver)
    {
        gateObservers.add(gateObserver);
    }
    //删除观察者方法
    public void remove(GateObserver gateObserver)
    {
        gateObservers.remove(gateObserver);
    }
    public abstract void notifyObserver(int status, Vector<ReportData> mReports); //通知观察者方法
}

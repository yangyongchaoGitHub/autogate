package com.dataexpo.autogate.listener;

import com.dataexpo.autogate.model.gate.ReportData;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public abstract class GateSubject {
    //观察者列表
    protected List<GateObserver> gateObservers =new ArrayList<GateObserver>();

    //增加观察者的方法
    public void add(GateObserver gateObserver)
    {
        gateObservers.add(gateObserver);
    }

    //删除观察者的方法
    public void remove(GateObserver gateObserver)
    {
        gateObservers.remove(gateObserver);
    }

    //通知观察者的方法
    public abstract void notifyGate(Vector<ReportData> mReports);
}

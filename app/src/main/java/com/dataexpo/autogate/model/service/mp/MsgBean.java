package com.dataexpo.autogate.model.service.mp;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class MsgBean<T> implements Serializable {

    public  Integer code;

    public String msg;

    public T data;

    public List<Map<String, Object>> dataList;

    public long count;

}

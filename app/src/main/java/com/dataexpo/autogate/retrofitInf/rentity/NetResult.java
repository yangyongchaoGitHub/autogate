package com.dataexpo.autogate.retrofitInf.rentity;

public class NetResult<T> {
    private Integer errcode;

    /**
     * 相关消息
     */
    private String errmsg;

    /**
     * 返回到移动端的数据对象
     */
    private T data;

    public Integer getErrcode() {
        return errcode;
    }

    public void setErrcode(Integer errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

package com.dataexpo.autogate.retrofitInf;

public class URLs {
    //public static final String baseUrl = "http://192.168.1.12:8080/Doors/";
    public static final String baseUrl = "http://saas.dataexpo.com.cn/Doors/";

    private static final String postfix = ".do";

    // 登录
    public static final String tiktakUrl = "pda/tiktak" + postfix;

    // 保存当前设备配置
    public static final String saveDeviceUrl = "pda/device/save" + postfix;

    //查询同步用户数据
    public static final String queryUserUrl = "pda/user/queryByCondition" + postfix;

    //查询同步用户人像
    public static final String queryUserImageUrl = "upload/{eucode}";

    //获取权限列表
    public static final String queryAccessGroupUrl = "api/data/findList" + postfix;

    //获取卡号的权限
    public static final String checkCardUrl = "api/data/findIC" + postfix;

    // 获取当前设备配置
    public static final String queryRegistInfoUrl = "pda/device/info" + postfix;

    // 上传通道门记录
    public static final String uploadRecordUrl = "pda/record/upload" + postfix;

    //查询背景图
    public static final String queryBackgroundUrl = "upload/bg/bg.jpg";
}

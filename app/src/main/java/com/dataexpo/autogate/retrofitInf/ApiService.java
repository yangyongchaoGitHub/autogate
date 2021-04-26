package com.dataexpo.autogate.retrofitInf;

import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.model.service.Device;
import com.dataexpo.autogate.model.service.DeviceQueryResult;
import com.dataexpo.autogate.model.service.MsgBean;
import com.dataexpo.autogate.model.service.PageResult;
import com.dataexpo.autogate.model.service.PermissionBean;
import com.dataexpo.autogate.model.service.UserEntityVo;
import com.dataexpo.autogate.model.service.UserQueryConditionVo;
import com.dataexpo.autogate.retrofitInf.rentity.NetResult;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import static com.dataexpo.autogate.retrofitInf.URLs.*;

public interface ApiService {

    @Deprecated
    @POST(saveDeviceUrl)
    Call<NetResult<String>> saveDeviceConfig(@Body Device device);

    @POST(queryUserUrl)
    Call<NetResult<PageResult<UserEntityVo>>> querySyncUser(@Body UserQueryConditionVo vo);

    @Deprecated
    @GET(queryUserImageUrl)
    Call<ResponseBody> querySyncUserImage(@Path("eucode") String eucode);

    @GET(queryAccessGroupUrl)
    Call<PermissionBean> queryAccessGroup(@Query("expoId") String expoId);

    //获取卡号在服务器的信息
    @GET(checkCardUrl)
    Call<MsgBean> checkCard(@Query("ICD") String icd);

    @Deprecated
    @GET(queryRegistInfoUrl)
    Call<DeviceQueryResult> queryRegistInfo(@Query("serialNumber") String serialNumber);

    @POST(uploadRecordUrl)
    Call<NetResult<String>> uploadRecord(@Body ReportData data);

    @GET(queryBackgroundUrl)
    Call<ResponseBody> queryBackground();

    // 对接到mp后台
    @GET(verifyExpoUrl)
    Call<com.dataexpo.autogate.model.service.mp.MsgBean<String>> verifyExpo(@Query("expoId") String expoId, @Query("address") String address);

    @POST(mpQueryUserUrl)
    Call<com.dataexpo.autogate.model.service.mp.MsgBean<PageResult<UserEntityVo>>> mpQuerySyncUser(@Body UserQueryConditionVo vo);
}

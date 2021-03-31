package com.dataexpo.autogate.retrofitInf;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface BomService {
//    //@param overStatus  是否过期  0过期， 1未过期
//    @GET(bomListUrl)
//    Call<NetResult<List<Bom>>> getBomList(@Query("pageNo") int pageNo, @Query("pageSize") int pageSize,
//                                          @Query("keyWord") String keyWord, @Query("type") Integer type,
//                                          @Query("status") Integer status, @Query("loginId") Integer loginId,
//                                          @Query("overStatus") Integer overStatus);
//
//    @GET(bomSeriesUrl)
//    Call<NetResult<PdaBomSeriesVo>> getBomSeries(@Query("bomId") int bomId);
//
//    @GET(bomDeviceUrl)
//    Call<NetResult<List<Device>>> getBomDevice(@Query("bomId") int bomId);
//
//    @GET(bomFindDeviceInfoUrl)
//    Call<NetResult<Device>> queryDeviceInfo(@Query("code") String code);
//
//    @POST(addDeviceInBomUrl)
//    Call<NetResult<String>> addDeviceInBom(@Body BomDeviceVo bomDeviceVo);
//
//    @Deprecated
//    @GET(bomFindDeviceInfoByRfidUrl)
//    Call<NetResult<Device>> queryDeviceInfoByRfid(@Query("rfid") String rfid);
//
//    @POST(deleteBomDeviceUrl)
//    Call<NetResult<String>> deleteBomDevice(@Body BomDeviceVo bomDeviceVo);
//
//    @GET(deviceInfoUrl)
//    Call<NetResult<List<DeviceUsingInfo>>> getDeviceInfo(@Query("code") String code, @Query("type") Integer type);
//
//    @POST(addBomSeriesUrl)
//    Call<NetResult<String>> addBomSeries(@Body BomSeriesVo bomSeriesVo);
//
//    @GET(deleteBomSeriesUrl)
//    Call<NetResult<String>> deleteBomSeries(@Query("bsId") Integer dsId);
//
//    @POST(addInHomeUrl)
//    Call<NetResult<String>> addInHome(@Body BomDeviceVo bomDeviceVo);
}

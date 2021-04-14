package com.dataexpo.autogate.model.service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 返回IC卡
 * @author Administrator
 */
public class UserAndPermission implements Serializable{
    //卡号是否存在:1:存在；0/null：不存在
    private Integer isFort;
    // 状态（0:待审核,1:已审核,2 驳回.3 已打印；4已发放）
    private Integer euStatus;
    //姓名
    private String uiName;
    //团组/组别
    private String euDefine;
    //euId
    private Integer euId;
    //人像照片
    private String euImage;
    //职务
    private String uiCompanyTitle;
    //英文名
    private String uiDapt;
    private Date euPrintTime;
    // 制证时间
    public Date euRegTime;
    //打印时间
    private String printTime;
    //权限
    private List<RegStatus> regList;

    private String suffix;

    public Integer getIsFort() {
        return isFort;
    }
    public void setIsFort(Integer isFort) {
        this.isFort = isFort;
    }
    public Integer getEuStatus() {
        return euStatus;
    }
    public void setEuStatus(Integer euStatus) {
        this.euStatus = euStatus;
    }

    public String getUiName() {
        return uiName;
    }
    public void setUiName(String uiName) {
        this.uiName = uiName;
    }
    public String getEuDefine() {
        return euDefine;
    }
    public void setEuDefine(String euDefine) {
        this.euDefine = euDefine;
    }
    public Integer getEuId() {
        return euId;
    }
    public void setEuId(Integer euId) {
        this.euId = euId;
    }
    public String getEuImage() {
        return euImage;
    }
    public void setEuImage(String euImage) {
        this.euImage = euImage;
    }

    public String getUiCompanyTitle() {
        return uiCompanyTitle;
    }

    public void setUiCompanyTitle(String uiCompanyTitle) {
        this.uiCompanyTitle = uiCompanyTitle;
    }

    public String getUiDapt() {
        return uiDapt;
    }

    public void setUiDapt(String uiDapt) {
        this.uiDapt = uiDapt;
    }

    public Date getEuPrintTime() {
        return euPrintTime;
    }

    public void setEuPrintTime(Date euPrintTime) {
        this.euPrintTime = euPrintTime;
    }

    public Date getEuRegTime() {
        return euRegTime;
    }

    public void setEuRegTime(Date euRegTime) {
        this.euRegTime = euRegTime;
    }

    public String getPrintTime() {
        return printTime;
    }

    public void setPrintTime(String printTime) {
        this.printTime = printTime;
    }

    public List<RegStatus> getRegList() {
        return regList;
    }

    public void setRegList(List<RegStatus> regList) {
        this.regList = regList;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public boolean initsuffix() {
        if (euImage == null || "".equals(euImage)) {
            return false;
        }
        String[] suffixs = euImage.split("\\.");
        if (suffixs.length == 2) {
            this.suffix = "." + suffixs[1];
            return true;
        }
        return false;
    }
}

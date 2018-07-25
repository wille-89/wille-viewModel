package org.wille.viewmodel.run;

import android.support.annotation.NonNull;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/18
 * 功能简介：
 */
public class RunTimeError implements BasisError {

    private String mErrorInfo = "";

    public RunTimeError(String errorInfo) {
        if (errorInfo == null) {
            mErrorInfo = "";
        } else {
            mErrorInfo = errorInfo;
        }
    }

    public RunTimeError(){

    }

    public RunTimeError(Exception e){
        if(e == null){
            mErrorInfo = "";
        }else{
            mErrorInfo = e.getMessage();
        }

    }

    @Override
    @NonNull
    public String getErrorInfo() {
        return mErrorInfo;
    }
}

package org.wille.lifecycle.run;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/25
 * 功能简介：错误转换器
 */
public class WilleErrorConverter<T extends BasisError> {

    private T mErrorObj = null;


    public WilleErrorConverter(T errorObj){
        mErrorObj = errorObj;
    }

    public T getErrorObj() {
        return mErrorObj;
    }

    public void setErrorObj(T errorObj) {
        mErrorObj = errorObj;
    }
}

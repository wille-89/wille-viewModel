package org.wille.lifecycle.run;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/16
 * 功能简介：基础的错误类
 */
public class BasisError<T extends Exception> {

    private String mErrorInfo = null;
    private Object mExceptionErrorInfo = null;
    private T mException = null;


    public BasisError(T exception) {
        this(exception, exception.getMessage());
    }

    public BasisError(T exception, String errorInfo) {
        this(exception, errorInfo, null);
    }

    public BasisError(T exception, String errorInfo, Object exceptionErrorInfo) {
        mException = exception;
        mErrorInfo = errorInfo;
        mExceptionErrorInfo = exceptionErrorInfo;
    }

    public String getErrorInfo() {
        return mErrorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        mErrorInfo = errorInfo;
    }

    public Object getExceptionErrorInfo() {
        return mExceptionErrorInfo;
    }

    public void setExceptionErrorInfo(Object exceptionErrorInfo) {
        mExceptionErrorInfo = exceptionErrorInfo;
    }

    public T getException() {
        return mException;
    }

    public void setException(T exception) {
        mException = exception;
    }

    public static <T extends Exception> BasisError getBasisError(T exception) {
        return new BasisError(exception);
    }


}

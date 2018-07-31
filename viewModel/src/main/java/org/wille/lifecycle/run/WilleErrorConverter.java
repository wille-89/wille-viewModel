package org.wille.lifecycle.run;


import android.support.annotation.MainThread;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/25
 * 功能简介：错误转换器
 */
public class WilleErrorConverter<T extends BasisError> {

    private T mErrorObj = null;
    private final static Object mConverterLock = new Object();

    public WilleErrorConverter(T errorObj) {
        mErrorObj = errorObj;
    }

    private WilleErrorConverter() {

    }

    public T getErrorObj() {
        return mErrorObj;
    }

    public void setErrorObj(BasisError errorObj) {
        mErrorObj = (T) errorObj;
    }

    /**
     * 主线程异常转换器
     *
     * @param exception
     * @return
     */
    @MainThread
    public static <T extends Exception> WilleErrorConverter exceptionConverter(T exception) {
        InstanceErrorConverter.sWilleErrorConverter.setErrorObj(BasisError.getBasisError(exception));
        return InstanceErrorConverter.sWilleErrorConverter;
    }

    /**
     * 主线程异常转换器
     *
     * @param exception
     * @return
     */
    @MainThread
    public static WilleErrorConverter exceptionConverter(BasisError exception) {
        InstanceErrorConverter.sWilleErrorConverter.setErrorObj(exception);
        return InstanceErrorConverter.sWilleErrorConverter;
    }

    /**
     * 子线程异常转换器
     *
     * @param exception
     * @return
     */
    public static WilleErrorConverter postExceptionConverter(BasisError exception) {
        synchronized (mConverterLock) {
            InstanceErrorConverter.sWilleErrorConverter.setErrorObj(exception);
        }
        return InstanceErrorConverter.sWilleErrorConverter;
    }

    /**
     * 子线程异常转换器
     *
     * @param exception
     * @return
     */
    public static <T extends Exception> WilleErrorConverter postExceptionConverter(T exception) {
        synchronized (mConverterLock) {
            InstanceErrorConverter.sWilleErrorConverter.setErrorObj(BasisError.getBasisError(exception));
        }
        return InstanceErrorConverter.sWilleErrorConverter;
    }

    private final static class InstanceErrorConverter {
        final static WilleErrorConverter sWilleErrorConverter = new WilleErrorConverter();

    }
}

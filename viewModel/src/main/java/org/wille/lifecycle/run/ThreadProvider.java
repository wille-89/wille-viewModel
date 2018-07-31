package org.wille.lifecycle.run;

import android.os.Looper;
import android.support.annotation.NonNull;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/25
 * 功能简介：线程工作提供者
 */
public abstract class ThreadProvider {

    public abstract void postThread(@NonNull Runnable runnable);

    /**
     * 判断是否在主线程
     *
     * @return
     */
    public final static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}

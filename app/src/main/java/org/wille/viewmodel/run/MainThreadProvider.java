package org.wille.viewmodel.run;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/18
 * 功能简介：主线程提供者，用于提供主线程来执行任务
 */
public class MainThreadProvider {

    private Handler mHandler = null;
    // 线程锁
    private final Object mLock = new Object();


    private MainThreadProvider() {
        initHandler();
    }

    public void postToMainThread(@NonNull Runnable runnable) {
        synchronized (mLock) {
            if (isMainThread()) {
                runnable.run();
            } else {
                if (mHandler == null) {
                    initHandler();
                }
                mHandler.post(runnable);
            }
        }
    }

    private void initHandler() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static MainThreadProvider getInstance() {
        return Install.install;
    }

    private static class Install {
        private final static MainThreadProvider install = new MainThreadProvider();
    }


    /**
     * 判断是否在主线程
     *
     * @return
     */
    public boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}

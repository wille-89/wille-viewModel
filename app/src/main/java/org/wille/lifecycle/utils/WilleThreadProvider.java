package org.wille.lifecycle.utils;

import android.support.annotation.NonNull;

import org.wille.lifecycle.run.ThreadProvider;


/**
 * 创建人员：杨浩
 * 创建日期：2018/7/25
 * 功能简介：
 */
public final class WilleThreadProvider extends ThreadProvider {

    // 线程锁
    private final Object mLock = new Object();
    private ThreadProvider mPostToMainProvider = null;
    private ThreadProvider mPostProvider = null;

    /**
     * 判断当前是否处于主线程
     * 并根据结果将 runnable 运行至主线程
     *
     * @param runnable
     */
    @Override
    public void postThread(@NonNull Runnable runnable) {
        synchronized (mLock) {
            if (isMainThread()) {
                runMainThreadProvider(runnable);
            } else {
                runIoThreadProvider(runnable);
            }
        }
    }

    /**
     * 当前处在主线程，运行线程中的内容
     *
     * @param runnable
     */
    private void runMainThreadProvider(Runnable runnable) {
        if (mPostProvider == null) {
            mPostProvider = new MainThreadProvider();
        }
        mPostProvider.postThread(runnable);
    }

    /**
     * 当前处在非主线程，要将线程调度至主线程
     *
     * @param runnable
     */
    private void runIoThreadProvider(Runnable runnable) {
        if (mPostToMainProvider == null) {
            mPostToMainProvider = new IoThreadProvider();
        }
        mPostToMainProvider.postThread(runnable);
    }

    /**
     * 获取 子线程 to main 的 Provider
     *
     * @return
     */
    public ThreadProvider getPostToMainProvider() {
        return mPostToMainProvider;
    }

    /**
     * 设置一个将 runnable 移至主线程执行的 Provider 委托
     *
     * @param postToMainProvider
     */
    public void setPostToMainProvider(ThreadProvider postToMainProvider) {
        mPostToMainProvider = postToMainProvider;
    }

    /**
     * 获取直接运行的 Provider
     *
     * @return
     */
    public ThreadProvider getPostProvider() {
        return mPostProvider;
    }

    /**
     * 设置一个将 runnable 直接执行的 Provider 委托
     *
     * @param postProvider
     */
    public void setPostProvider(ThreadProvider postProvider) {
        mPostProvider = postProvider;
    }

    public static WilleThreadProvider getInstance() {
        return Install.install;
    }

    private static class Install {
        private final static WilleThreadProvider install = new WilleThreadProvider();
    }

    /**
     * 我期望 WilleThreadProvider 将是以单例运行
     * 所以外部无法构造出来
     */
    private WilleThreadProvider() {

    }

}

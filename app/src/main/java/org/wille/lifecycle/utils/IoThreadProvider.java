package org.wille.lifecycle.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import org.wille.lifecycle.run.ThreadProvider;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/25
 * 功能简介：
 */
final class IoThreadProvider extends ThreadProvider {

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void postThread(@NonNull Runnable runnable) {
        mHandler.post(runnable);
    }
}

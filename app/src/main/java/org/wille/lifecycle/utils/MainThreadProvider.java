package org.wille.lifecycle.utils;

import android.support.annotation.NonNull;

import org.wille.lifecycle.run.ThreadProvider;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/18
 * 功能简介：主线程提供者，用于提供主线程来执行任务
 */
final class MainThreadProvider extends ThreadProvider {

    @Override
    public void postThread(@NonNull Runnable runnable) {
        runnable.run();
    }

}

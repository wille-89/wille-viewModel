package org.wille.lifecycle.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.wille.lifecycle.base.ViewModelActivity;
import org.wille.lifecycle.changeModel.CViewModel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 创建人员：杨浩
 * 创建日期：2018/5/14
 * 功能简介：Activity 堆栈管理器
 */
public class ActivityManagerInstance implements Application.ActivityLifecycleCallbacks {


    // activity 堆栈提高多线程效率
    private static final Map<Class<?>, ViewModelActivity> mStack = new ConcurrentHashMap<>();

    // 刷新回调
//    private Handler mHandler = null;
    // 是否处于激活状态
    private boolean mIsAllResume = false;
    // 是否已经绑定过
    private boolean mIsBind = false;

    public void bindApplication(@NonNull Application application) {
        if (mIsBind) {
            throw new RuntimeException("重复绑定，一个APP只推荐绑定一个堆栈管理器");
        }
        mIsBind = true;
        application.registerActivityLifecycleCallbacks(this);
    }

    @Nullable
    private Context getContext() {
        for (ViewModelActivity baseActivity : mStack.values()) {
            Context context = baseActivity.getApplication().getApplicationContext();
            if (context != null) {
                return context;
            }
        }
        return null;
    }

    /**
     * 获取目标 Activity
     * 可能为空
     *
     * @param modelClass
     * @return
     */
    @Nullable
    public <T extends ViewModelActivity> T of(@NonNull Class<T> modelClass) {
        ViewModelActivity baseActivity = mStack.get(modelClass);
        if (baseActivity == null) {
            return null;
        }
        return (T) baseActivity;
    }

    /**
     * 获取目标 ViewModel
     * @param modelClass
     * @param <T>
     * @return
     */
    @Nullable
    public <T extends ViewModelActivity> CViewModel getViewModel(@NonNull Class<T> modelClass) {
        ViewModelActivity activity = of(modelClass);
        if(activity != null){
           return activity.getViewModel();
        }
        return null;
    }

    /**
     * finish 所有 Activity
     */
    public void finishAll() {
        // 循环取出栈堆的方法
        for (ViewModelActivity baseActivity : mStack.values()) {
            baseActivity.finish();
        }
    }

    /**
     * finish 指定 Activity
     *
     * @param className
     */
    @SafeVarargs
    public final void finish(Class<? extends ViewModelActivity>... className) {
        for (Class<? extends ViewModelActivity> baseActivityClass : className) {
            ViewModelActivity baseActivity = mStack.get(baseActivityClass);
            if (baseActivity != null) {
                baseActivity.finish();
            }
        }
    }

    /**
     * finish 全部 Activity 除了指定 Activity
     *
     * @param className
     */
    @SafeVarargs
    public final void finishAll(Class<? extends ViewModelActivity>... className) {
        for (ViewModelActivity baseActivity : mStack.values()) {
            for (Class<? extends ViewModelActivity> classOne : className) {
                if (classOne != baseActivity.getClass()) {
                    baseActivity.finish();
                }
            }
        }
    }

    /**
     * 判断全部 Activity 是否都有焦点，如果有就代表在前台运行
     */
    public boolean isAllResume() {
        return mIsAllResume;
    }

    /**
     * 有 Activity 获取焦点
     */
    private void setActivityResume() {
        mIsAllResume = true;
    }

    /**
     * 有 Activity 失去了焦点
     */
    private void setActivityPaused() {
        mIsAllResume = false;
    }

    /**
     * 添加 Activity 入堆栈
     *
     * @param activity
     */
    private void addActivity(ViewModelActivity activity) {
        mStack.put(activity.getClass(), activity);
    }

    /**
     * 移除 Activity 堆栈
     *
     * @param activity
     */
    private void removeActivity(ViewModelActivity activity) {
        mStack.remove(activity.getClass());
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (activity instanceof ViewModelActivity) {
            addActivity((ViewModelActivity) activity);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        // 有 Activity 获取到焦点，需要记录下来用于判断是否在前台显示
        setActivityResume();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // 有 Activity 失去焦点，需要记录下来用于判断是否在前台显示
        setActivityPaused();
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activity instanceof ViewModelActivity) {
            removeActivity((ViewModelActivity) activity);
        }
    }

    private ActivityManagerInstance() {
        // TODO: 2018/5/14 暂时没有跨线程的需求，不开启
//        mHandler = new Handler(Looper.getMainLooper());
    }

    public static ActivityManagerInstance getInstance() {
        return Install.install;
    }

    public static Context getApplication() {
        return getInstance().getContext();
    }

    private static class Install {
        private final static ActivityManagerInstance install = new ActivityManagerInstance();
    }


}

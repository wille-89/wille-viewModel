package org.wille.viewmodel;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModel;
import android.support.v7.app.AppCompatActivity;


/**
 * 创建人员：杨浩
 * 创建日期：2018/5/21
 * 功能简介：ViewModel 生命周期管理
 */
public abstract class ViewModelLifecycle extends ViewModel implements LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public abstract void onDestroy();


    /**
     * 绑定生命周期
     * @param lifecycle
     */
    public ViewModelLifecycle bindLifecycle(AppCompatActivity lifecycle){
        lifecycle.getLifecycle().addObserver(this);
        return this;
    }


}

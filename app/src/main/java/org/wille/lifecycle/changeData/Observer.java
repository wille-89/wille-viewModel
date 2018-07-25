package org.wille.lifecycle.changeData;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.wille.lifecycle.run.ViewModelConstant;
import org.wille.lifecycle.run.WilleErrorConverter;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/16
 * 功能简介：适用于定制的 LiveData 的简单回调
 */
public interface Observer<T> {



    /**
     * 当调用 {@link LiveData#postValue} 时触发
     * 在多个线程同时触发新的 onChanged 事件在主线程未通知出去时会覆盖旧的事件
     * 详情请参考 {@link LiveData#postValue}
     * 回调一定会在主线程响应
     *
     * @param t    一个新的数据变化
     * @param type 触发这个数据变化的原因
     */
    void onChanged(@Nullable T t, @NonNull @ViewModelConstant.LoadDataType int type);


    /**
     * 调用过程产生了错误
     *
     * @param error
     */
    void onError(WilleErrorConverter error);

}

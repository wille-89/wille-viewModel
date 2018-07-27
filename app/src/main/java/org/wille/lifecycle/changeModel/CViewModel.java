package org.wille.lifecycle.changeModel;

import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import org.wille.lifecycle.changeData.CLiveData;
import org.wille.lifecycle.changeData.Observer;
import org.wille.lifecycle.run.AppLifeCycleAble;
import org.wille.lifecycle.run.BasisError;
import org.wille.lifecycle.run.ViewModelConstant;
import org.wille.lifecycle.run.WilleErrorConverter;

import static org.wille.lifecycle.run.ViewModelConstant.LOAD_DATA_DEF;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/27
 * 功能简介：
 */
public abstract class CViewModel<T> extends ViewModelLifecycle implements AppLifeCycleAble {

    private final static boolean DEF_BOOLEAN = false;
    private final static boolean TRUE_BOOLEAN = true;
    // 是否已经结束了生命周期
    private boolean isOnDestroy = DEF_BOOLEAN;
    // 是否已经绑定了生命周期
    private boolean isBindActivity = DEF_BOOLEAN;
    private CLiveData<T> mData;


    public CViewModel() {
        initLiveData();
    }


    private final void initLiveData() {
        mData = new CLiveData<>();
        onCreate(mData);
    }

    /**
     * 加载数据源入口
     *
     * @param type 获取数据类型，是 init or result or addMore or def
     */
    public abstract void loadData(@ViewModelConstant.LoadDataType int type);

    /**
     * 可以在这里做一些初始化操作
     *
     * @param cLiveData
     */
    public void onCreate(CLiveData<T> cLiveData) {

    }

    /**
     * 注册观测者
     *
     * @param owner
     * @param observer
     */
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
        mData.observe(owner, observer);
    }

    /**
     * 解除所有绑定
     */
    public void removeObservers() {
        // 如果生命周期未结束才需要解绑定
        if (canDoNext()) {
            mData.onDestroy();
        }
    }

    /**
     * 解绑事件
     *
     * @param modelEvent
     */
    public void removeObserver(Observer<T> modelEvent) {
        // 如果生命周期未结束才需要解绑定
        if (canDoNext()) {
            mData.removeObserver(modelEvent);
        }
    }

    /**
     * 异常推送
     *
     * @param e    异常源
     * @param type 刷新数据类型，是 init or result or addMore or def
     */
    public void postError(BasisError e, @ViewModelConstant.LoadDataType int type) {
        // 如果生命周期未结束才可以推送异常
        if (canDoNext()) {
            mData.postValue(WilleErrorConverter.postExceptionConverter(e), type);
        }
    }

    /**
     * 异常推送
     *
     * @param msg  异常信息
     * @param type 刷新数据类型，是 init or result or addMore or def
     */
    public void postError(String msg, @ViewModelConstant.LoadDataType int type) {
        postError(new Exception(msg), type);
    }

    /**
     * 异常推送
     *
     * @param e    异常源
     * @param type 刷新数据类型，是 init or result or addMore or def
     */
    public void postError(Exception e, @ViewModelConstant.LoadDataType int type) {
        // 如果生命周期未结束才可以推送异常
        if (canDoNext()) {
            mData.postValue(WilleErrorConverter.postExceptionConverter(e), type);
        }
    }

    /**
     * 使用上一次的数据去触发刷新
     */
    public void postValue() {
        // 如果生命周期未结束才可以刷新
        if (canDoNext()) {
            mData.postValue();
        }
    }

    /**
     * 刷新数据
     *
     * @param t 数据源
     */
    public void postValue(T t) {
        // 如果生命周期未结束才可以刷新
        if (canDoNext()) {
            mData.postValue(t, LOAD_DATA_DEF);
        }
    }

    /**
     * 刷新数据
     *
     * @param t    数据源
     * @param type 刷新数据类型，是 init or result or addMore or def
     */
    public void postValue(T t, @ViewModelConstant.LoadDataType int type) {
        // 如果生命周期未结束才可以刷新
        if (canDoNext()) {
            mData.postValue(t, type);
        }
    }

    /**
     * 返回当前值
     *
     * @return
     */
    @Nullable
    public T getValue() {
        // 如果生命周期未结束才可以获取
        if (canDoNext() && mData != null) {
            return mData.getValue();
        }
        return null;
    }

    /**
     * 与 Activity 绑定生命周期
     * 多次重复绑定可能出现意想不到的情况
     *
     * @param lifecycle
     * @return
     */
    @NonNull
    public CViewModel bindLifecycle(AppCompatActivity lifecycle) {
        // 如果生命周期未结束才可以获取
        if (canDoNext()) {
            isBindActivity = TRUE_BOOLEAN;
            lifecycle.getLifecycle().addObserver(this);
        }
        return this;
    }

    /**
     * 与 Fragment 绑定生命周期
     * 多次重复绑定可能出现意想不到的情况
     *
     * @param lifecycle
     * @return
     */
    @NonNull
    public CViewModel bindLifecycle(Fragment lifecycle) {
        // 如果生命周期未结束才可以获取
        if (canDoNext()) {
            isBindActivity = TRUE_BOOLEAN;
            lifecycle.getLifecycle().addObserver(this);
        }
        return this;
    }

    /**
     * 用于判断是否绑定了 Activity
     *
     * @return
     */
    public boolean isBindActivity() {
        return isBindActivity;
    }

    @Nullable
    CLiveData<T> getLiveData() {
        return mData;
    }

    @Override
    public boolean canDoNext() {
        return !isOnDestroy;
    }

    @Override
    public void onDestroy() {
        isOnDestroy = TRUE_BOOLEAN;
        if (mData != null) {
            mData.onDestroy();
        }
        mData = null;
    }


}

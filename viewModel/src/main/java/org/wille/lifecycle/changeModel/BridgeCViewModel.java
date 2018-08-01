package org.wille.lifecycle.changeModel;


import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import org.wille.lifecycle.changeData.CLiveData;
import org.wille.lifecycle.changeData.Observer;
import org.wille.lifecycle.run.BasisError;
import org.wille.lifecycle.run.ViewModelConstant;
import org.wille.lifecycle.run.WilleErrorConverter;

import static org.wille.lifecycle.run.ViewModelConstant.LOAD_DATA_DEF;


/**
 * 创建人员：杨浩
 * 创建日期：2018/8/1
 * 功能简介：可以观察俩个 LiveData 的 ViewModel
 * 设计之处就是考虑观测的对象数量是确定的
 * 比如一个界面 A 它有网络请求的数据 Q 需要观测，并且它有一份数据 W 需要分享给界面 B 这是我碰到最常用的情况
 * 所以封装了 BridgeCViewModel
 * 如果有一对多的情况请使用 MediatorCViewModel ，但是由于一对多会关系会导致潜在的管理困难与使用困难，所以不推荐使用
 */
public abstract class BridgeCViewModel<T, X> extends CViewModel<T> {

    private final CLiveData<X> mBridgeData;

    public BridgeCViewModel() {
        super();
        mBridgeData = new CLiveData<>();
        onCreate(getLiveData(), mBridgeData);
    }

    /**
     * 可以在这里做一些初始化操作
     *
     * @param cLiveData
     */
    public void onCreate(CLiveData<T> cLiveData, CLiveData<X> bridgeData) {

    }

    /**
     * 注册观测者
     *
     * @param owner
     * @param observer
     */
    public void bridgeObserve(@NonNull LifecycleOwner owner, @NonNull Observer<X> observer) {
        mBridgeData.observe(owner, observer);
    }

    public void removeBridgeObservers() {
        // 如果生命周期未结束才需要解绑定
        if (canDoNext()) {
            mBridgeData.onDestroy();
        }
    }

    public void removeAllObservers() {
        removeBridgeObservers();
        removeObservers();
    }

    /**
     * 解绑事件
     *
     * @param modelEvent
     */
    @MainThread
    public void removeBridgeObserver(Observer<X> modelEvent) {
        // 如果生命周期未结束才需要解绑定
        if (canDoNext()) {
            mBridgeData.removeObserver(modelEvent);
        }
    }

    public void removeAllObserver(Observer modelEvent) {
        removeObserver(modelEvent);
        removeBridgeObserver(modelEvent);
    }

    public void postAllError(BasisError e, @ViewModelConstant.LoadDataType int type) {
        postBridgeError(e, type);
        postError(e, type);
    }

    public void postAllError(String msg, @ViewModelConstant.LoadDataType int type) {
        postBridgeError(msg, type);
        postError(msg, type);
    }

    /**
     * 异常推送
     *
     * @param e    异常源
     * @param type 刷新数据类型，是 init or result or addMore or def
     */
    public void postBridgeError(BasisError e, @ViewModelConstant.LoadDataType int type) {
        // 如果生命周期未结束才可以推送异常
        if (canDoNext()) {
            mBridgeData.postValue(WilleErrorConverter.postExceptionConverter(e), type);
        }
    }

    /**
     * 异常推送
     *
     * @param msg  异常信息
     * @param type 刷新数据类型，是 init or result or addMore or def
     */
    public void postBridgeError(String msg, @ViewModelConstant.LoadDataType int type) {
        postBridgeError(new Exception(msg), type);
    }

    /**
     * 异常推送
     *
     * @param e    异常源
     * @param type 刷新数据类型，是 init or result or addMore or def
     */
    public void postBridgeError(Exception e, @ViewModelConstant.LoadDataType int type) {
        // 如果生命周期未结束才可以推送异常
        if (canDoNext()) {
            mBridgeData.postValue(WilleErrorConverter.postExceptionConverter(e), type);
        }
    }

    /**
     * 使用上一次的数据去触发刷新
     */
    public void postBridgeValue() {
        // 如果生命周期未结束才可以刷新
        if (canDoNext()) {
            mBridgeData.postValue();
        }
    }

    public void postAllValue() {
        postBridgeValue();
        postValue();
    }

    /**
     * 刷新数据
     *
     * @param t 数据源
     */
    public void postBridgeValue(X t) {
        // 如果生命周期未结束才可以刷新
        if (canDoNext()) {
            mBridgeData.postValue(t, LOAD_DATA_DEF);
        }
    }

    /**
     * 刷新数据
     *
     * @param t    数据源
     * @param type 刷新数据类型，是 init or result or addMore or def
     */
    public void postBridgeValue(X t, @ViewModelConstant.LoadDataType int type) {
        // 如果生命周期未结束才可以刷新
        if (canDoNext()) {
            mBridgeData.postValue(t, type);
        }
    }

    /**
     * 返回当前值
     *
     * @return
     */
    @Nullable
    public X getBridgeValue() {
        // 如果生命周期未结束才可以获取
        if (canDoNext()) {
            return mBridgeData.getValue();
        }
        return null;
    }

    @NonNull
    @Override
    public BridgeCViewModel<T,X> bindLifecycle(Fragment lifecycle) {
        // 如果生命周期未结束才可以获取
        if (canDoNext()) {
            isBindActivity = TRUE_BOOLEAN;
            lifecycle.getLifecycle().addObserver(this);
        }
        return this;
    }

    @NonNull
    @Override
    public BridgeCViewModel<T,X> bindLifecycle(AppCompatActivity lifecycle) {
        // 如果生命周期未结束才可以获取
        if (canDoNext()) {
            isBindActivity = TRUE_BOOLEAN;
            lifecycle.getLifecycle().addObserver(this);
        }
        return this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

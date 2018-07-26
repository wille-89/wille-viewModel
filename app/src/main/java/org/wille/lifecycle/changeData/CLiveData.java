package org.wille.lifecycle.changeData;


import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.wille.lifecycle.run.ViewModelConstant.*;
import org.wille.viewmodel.LiveData.LiveData;
import org.wille.viewmodel.ViewModelConstant;
import org.wille.viewmodel.run.MainThreadProvider;
import org.wille.viewmodel.run.RunTimeError;

import static android.arch.lifecycle.Lifecycle.State.DESTROYED;
import static android.arch.lifecycle.Lifecycle.State.STARTED;
import static org.wille.lifecycle.run.ViewModelConstant.LOAD_DATA_DEF;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/25
 * 功能简介：
 */
public class CLiveData<T> {

    // 初始化数据
    private static final Object NOT_SET = new Object();
    // 发送数据事件默认序号
    private static final int START_VERSION = -1;
    // 内存锁
    private final Object mDataLock = new Object();
    // 当前发送数据的序号
    private int mVersion = START_VERSION;
    // 当前活跃的观测者
    private int mActiveCount = START_VERSION;
    // 数据源
    private volatile Object mData = NOT_SET;
    // 数据加载的方式
    private volatile @LoadDataType
    int mType = LOAD_DATA_DEF;
    // 观测者队列
    private ConcurrentHashMap<Observer<T>, ObserverWrapper> mObservers = new ConcurrentHashMap<>();
    // 是否开始分配数据
    private boolean mDispatchingValue = false;
    // 是否在分发事件时产生了新数据
    // 如果 true 这将导致重新分配数据至所有的观测者
    private boolean mDispatchInvalidated = false;
    // 用于多线程中调度来维护数据一致性
    private volatile Object mPendingData = NOT_SET;
    private volatile @LoadDataType
    int mPendingType = LOAD_DATA_DEF;
    // 将非主线程触发的事件，调度回主线程执行
    private final Runnable mPostValueRunnable = new Runnable() {
        @Override
        public void run() {
            Object newValue;
            @LoadDataType int newType;
            synchronized (mDataLock) {
                // 只取得当前最新的事件
                newValue = mPendingData;
                newType = mPendingType;
                // 重置数据，等待下一次数据
                mPendingData = NOT_SET;
            }
            //noinspection unchecked
            setValue((T) newValue, newType);
        }
    };

    /**
     * 注册观测者
     * @param owner
     * @param observer
     */
    @MainThread
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            // 界面已经销毁，不需要去监听
            return;
        }
        LifecycleBoundObserver wrapper = new LifecycleBoundObserver(owner, observer);
        ObserverWrapper existing = mObservers.putIfAbsent(observer, wrapper);
        if (existing != null && !existing.isAttachedTo(owner)) {
            throw new IllegalArgumentException("不能出现相同的观测者，但是生命周期不同，请检查 owner 是否一致，当前 owner ：" + owner.getClass());
        }
        if (existing != null) {
            return;
        }
        owner.getLifecycle().addObserver(wrapper);
    }


    /**
     * 将数据的变化推给他的观测者们
     * 如果在主线程完成推送前调用了多次此方法
     * 则只执行此方法最后一个发生变化的值
     * 如下：
     * CLiveData.postValue("a");
     * CLiveData.postValue("b");
     * CLiveData.postValue("c");
     * 极有可能 {@link #observe } 只会回调 c 的值
     * 但是这是正确的，这将减少我们回调的次数
     * 并保证 observe 收到的是最新的值
     *
     * @param value 更改的数据
     * @param type  本次更改数据的原因
     */
    public void postValue(@Nullable T value, @NonNull @LoadDataType int type) {
        // 判断当前是否处于主线程，在不同的线程需要有不同的处理方式
        // 来保证我们的 observe 一定处于主线程
        if (MainThreadProvider.getInstance().isMainThread()) {
            // 当前处于主线程，采用线程同步的方式回调
            setValue(value, type);
        } else {
            // 当前处于非主线程，使用 mPostValueRunnable 回调到主线程执行
            boolean postTask;
            synchronized (mDataLock) {
                // 判断是否已经在 mPostValueRunnable 重置数据
                postTask = mPendingData == NOT_SET;
                mPendingData = value;
                mPendingType = type;
            }
            // 如果 mPendingData 未在 mPostValueRunnable 重置，那么不需要重新发送事件
            // 因为 mPendingData 赋值是线程同步的
            // 在 mPostValueRunnable 是一定能取到最新到数据
            // 我们不必在这里重复发送
            if (!postTask) {
                return;
            }
            // 调度至主线程执行
            MainThreadProvider.getInstance().postToMainThread(mPostValueRunnable);
        }
    }

    /**
     * 将数据的变化推送给他的观测者们
     * 但是此方法只能在主线程调用
     * 为了安全起见，只暴露{@link #postValue} 给外部调用
     * 因为是同步线程的关系
     * 如果调用了多次  {@link #observe } 则会发生多次
     * 如下：
     * CLiveData.setValue("a");
     * CLiveData.setValue("b");
     * CLiveData.setValue("c");
     * {@link #observe } 则会收到 a、b、c
     *
     * @param value
     * @param type
     */
    @MainThread
    private void setValue(@Nullable T value, @NonNull @ViewModelConstant.LoadDataType int type) {
        mVersion++;
        mData = value;
        mType = type;
        // 将数据的变化推送给观测者们
        dispatchingValue(null);
    }

    /**
     * 将发生改变的数据推送给观测者们
     * 此方法保证了观测者一定能收到最新的数据
     * 但是因此会导致观测者收到多次 {@Observer.onChanged} 事件
     * 如果有 initiator "生命周期发生变化的观测者"
     * 那么为了让它能够收到最新的数据，此方法会针对它推送一次 {@Observer.onChanged} 事件
     * 如果这时 dispatchingValue 正在运行的话，这将触发 for 循环来重新遍历观测者们以保证他们收到的数据都是最新的
     * @param initiator 生命周期发生变化的观测者
     */
    private void dispatchingValue(@Nullable ObserverWrapper initiator) {
        // 判断当前是否正在推送数据给观测者
        if (mDispatchingValue) {
            mDispatchInvalidated = true;
            return;
        }
        mDispatchingValue = true;
        do {
            mDispatchInvalidated = false;
            if (initiator != null) {
                considerNotify(initiator);
                initiator = null;
            } else {
                for (ObserverWrapper observerWrapper : mObservers.values()) {
                    considerNotify(observerWrapper);
                    // 有新的推送事件产生，中断当前操作，重新推送最新数据
                    if (mDispatchInvalidated) {
                        break;
                    }
                }
            }
        } while (mDispatchInvalidated);
        mDispatchingValue = false;
    }

    /**
     * 推送消息给观测者
     * 保证每个观测者都是处于可接受正常数据的情况
     * @param observer
     */
    private void considerNotify(ObserverWrapper observer) {
        // 检查当前观测者是否活跃
        if (!observer.mActive) {
            return;
        }
        // 检查当前的观测者是否可以接受事件
        if (!observer.shouldBeActive()) {
            // 如果不可以接受事件，就关闭活跃状态
            observer.activeStateChanged(false);
            return;
        }
        // 不发送过时的事件
        if (observer.mLastVersion >= mVersion) {
            return;
        }
        observer.mLastVersion = mVersion;
        // 这里最好检测一下是否能转换为 T
        try {
            //noinspection unchecked
            observer.mObserver.onChanged((T) mData, mType);
        } catch (Exception e) {
            observer.mObserver.onError();
        }
    }


    /**
     * 自动处理生命周期的容器
     */
    private class LifecycleBoundObserver extends ObserverWrapper implements GenericLifecycleObserver {

        @NonNull
        final LifecycleOwner mOwner;

        LifecycleBoundObserver(@NonNull LifecycleOwner owner, Observer<T> observer) {
            super(observer);
            // 需要订阅生命周期的对象
            mOwner = owner;
        }

        @Override
        boolean shouldBeActive() {
            return mOwner.getLifecycle().getCurrentState().isAtLeast(STARTED);
        }

        @Override
        boolean isAttachedTo(LifecycleOwner owner) {
            return mOwner == owner;
        }

        /**
         * removeObserver 处理完成，接触生命周期的绑定
         */
        @Override
        void detachObserver() {
            mOwner.getLifecycle().removeObserver(this);
        }

        @Override
        public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
            // 判断生命周期是否结束
            if (mOwner.getLifecycle().getCurrentState() == DESTROYED) {
                // 如果生命周期结束，那么从观测者队列移除出去
                removeObserver(mObserver);
                return;
            }
            // 生命周期发生变化，重新计算当前活跃的观测者
            activeStateChanged(shouldBeActive());
        }
    }

    /**
     * 观测者的容器，主要是为了确定了观测者可以在合适的时间接受数据变化事件
     */
    private abstract class ObserverWrapper {
        final Observer<T> mObserver;
        // 当前是否活跃
        boolean mActive = false;
        // 当前执行的订阅事件序列
        int mLastVersion = START_VERSION;

        ObserverWrapper(Observer<T> observer) {
            mObserver = observer;
        }

        /**
         * 是否已经绑定到 Activity or Fragment 的生命周期当中
         *
         * @param owner
         * @return
         */
        boolean isAttachedTo(LifecycleOwner owner) {
            return false;
        }

        /**
         * 判断是否处于活跃状态
         * true 处于活跃可以处理事件
         *
         * @return
         */
        abstract boolean shouldBeActive();

        /**
         * 清理 Observer
         */
        void detachObserver() {
        }

        /**
         * 生命周期发生变化
         *
         * @param newActive 是否正处在活跃状态
         */
        final void activeStateChanged(boolean newActive) {
            // 判断 Observer 的活动状态是否产生变化
            if (newActive == mActive) {
                return;
            }
            mActive = newActive;
            boolean wasInactive = mActiveCount == 0;
            // 重新计算当前处于活跃状态的 Observer
            mActiveCount += mActive ? 1 : -1;
            if (wasInactive && mActive) {
                onActive();
            }
            if (mActiveCount == 0 && !mActive) {
                onInactive();
            }
            // 如果观测者 处于活跃状态，那么就给观测者提供最新的数据
            if (mActive) {
                dispatchingValue(this);
            }
        }
    }

    @MainThread
    public void removeObserver(@NonNull final Observer<T> observer) {
        if (MainThreadProvider.getInstance().isMainThread()) {
            ObserverWrapper removed = mObservers.get(observer);
            if (removed == null) {
                return;
            }
            removed.detachObserver();
            removed.activeStateChanged(false);
        }
    }

    @MainThread
    public void removeObservers(@NonNull final LifecycleOwner owner) {
        if (MainThreadProvider.getInstance().isMainThread()) {
            for (Map.Entry<Observer<T>, ObserverWrapper> entry : mObservers.entrySet()) {
                if (entry.getValue().isAttachedTo(owner)) {
                    removeObserver(entry.getKey());
                }
            }
        }
    }

    /**
     * 当前 LiveData 处于活跃状态
     * 可以理解成有活跃的 ObserverWrapper 对象
     */
    protected void onActive() {

    }

    /**
     * 当前 LiveData 处于非活跃状态
     * 可以理解成没有活跃的 ObserverWrapper 对象
     */
    protected void onInactive() {

    }


}

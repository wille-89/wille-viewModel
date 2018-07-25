package org.wille.viewmodel.LiveData;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.wille.viewmodel.ViewModelConstant;
import org.wille.viewmodel.run.MainThreadProvider;
import org.wille.viewmodel.run.RunTimeError;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static android.arch.lifecycle.Lifecycle.State.DESTROYED;
import static android.arch.lifecycle.Lifecycle.State.STARTED;
import static org.wille.viewmodel.ViewModelConstant.LOAD_DATA_DEF;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/16
 * 功能简介：
 */
public class LiveData<T> {

    // 内存锁，防止多线程出现问题
    private final Object mDataLock = new Object();
    // 发送数据事件默认序号
    private static final int START_VERSION = -1;
    // 当前发送数据的序号
    private int mVersion = START_VERSION;
    // 初始化数据
    private static final Object NOT_SET = new Object();
    // 订阅队列
    private ConcurrentHashMap<Observer<T>, ObserverWrapper> mObservers = new ConcurrentHashMap<>();
    // 当前活跃的观察者数量
    private int mActiveCount = 0;
    // 数据源
    private volatile Object mData = NOT_SET;
    private volatile @ViewModelConstant.LoadDataType
    int mType = LOAD_DATA_DEF;
    private boolean mDispatchingValue = false;
    private boolean mDispatchInvalidated = false;
    // 用于多线程中调度来维护数据一致性
    private volatile Object mPendingData = NOT_SET;
    private volatile @ViewModelConstant.LoadDataType
    int mPendingType = LOAD_DATA_DEF;
    // 将 onChanged 带回主线程执行
    private final Runnable mPostValueRunnable = new Runnable() {
        @Override
        public void run() {
            Object newValue;
            @ViewModelConstant.LoadDataType int newType;
            synchronized (mDataLock) {
                // 只取得当前最新的事件
                newValue = mPendingData;
                newType = mPendingType;
                // 重制数据，等待下一次数据
                mPendingData = NOT_SET;
            }
            // 未经过检查的类型
            setValue((T) newValue, newType);
        }
    };

    /**
     * 自动订阅生命周期
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
        public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
            if (mOwner.getLifecycle().getCurrentState() == DESTROYED) {
                removeObserver(mObserver);
                return;
            }
            activeStateChanged(shouldBeActive());
        }

        @Override
        boolean shouldBeActive() {
            return mOwner.getLifecycle().getCurrentState().isAtLeast(STARTED);
        }

        @Override
        boolean isAttachedTo(LifecycleOwner owner) {
            return mOwner == owner;
        }

        @Override
        void detachObserver() {
            mOwner.getLifecycle().removeObserver(this);
        }
    }


    /**
     * 包裹了 Observer 对象，定义了完整的生命周期能力
     * 由于 isAttachedTo 返回的是 false 所以不具备完整的使用能力
     */
    private abstract class ObserverWrapper {
        final Observer<T> mObserver;
        // 当前是否活跃
        boolean mActive = false;
        // 当前执行的订阅事件序列
        int mLastVersion = START_VERSION;

        abstract boolean shouldBeActive();

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
         * 清理 Observer
         */
        void detachObserver() {
        }

        void activeStateChanged(boolean newActive) {
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
            if (mActive) {
                dispatchingValue(this);
            }
        }
    }

    /**
     * 将数据的变化推给他们的观测者们
     * 如果在主线程完成推送前调用了多次此方法
     * 则只执行此方法最后一个保存下来的值
     * 如下：
     * LiveData.postValue("a");
     * LiveData.postValue("b");
     * LiveData.postValue("c");
     * 极有可能 {@link #observe } 只会回调 c 的值
     * 但是这是正确的，这将减少我们回调的次数
     *
     * @param value 更改的数据
     * @param type  本次更改数据的原因
     */
    public void postValue(@Nullable T value, @NonNull @ViewModelConstant.LoadDataType int type) {
        if (MainThreadProvider.getInstance().isMainThread()) {
            // 线程同步
            setValue(value, type);
        } else {
            // 线程异步
            boolean postTask;
            synchronized (mDataLock) {
                postTask = mPendingData == NOT_SET;
                mPendingData = value;
                mPendingType = type;
            }
            // 如果 mPendingData 未重置，那么不需要重新发送事件，因为已经加了 synchronized 能保证是线程同步的
            if (!postTask) {
                return;
            }
            MainThreadProvider.getInstance().postToMainThread(mPostValueRunnable);
        }
    }

    private void setValue(@Nullable T value, @NonNull @ViewModelConstant.LoadDataType int type) {
        mVersion++;
        mData = value;
        mType = type;
        dispatchingValue(null);
    }

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

    @MainThread
    public void removeObserver(@NonNull final Observer<T> observer) {
        if (MainThreadProvider.getInstance().isMainThread()) {
            ObserverWrapper removed = mObservers.remove(observer);
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

    private void dispatchingValue(@Nullable ObserverWrapper initiator) {
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
                    if (mDispatchInvalidated) {
                        break;
                    }
                }
            }
        } while (mDispatchInvalidated);
        mDispatchingValue = false;
    }


    private void considerNotify(LiveData.ObserverWrapper observer) {
        // 检查当前 Observer 是否有活跃的对象
        if (!observer.mActive) {
            return;
        }
        // 检查当前的活跃对象是否可以接受事件
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
            observer.mObserver.onChanged(mData, mType);
        } catch (Exception e) {
            observer.mObserver.onError(new RunTimeError(e));
        }
    }

    private class AlwaysActiveObserver extends ObserverWrapper {

        AlwaysActiveObserver(Observer<T> observer) {
            super(observer);
        }

        @Override
        boolean shouldBeActive() {
            return true;
        }
    }

    /**
     * 当前 LiveData 处于活跃状态
     */
    protected void onActive() {

    }

    /**
     * 当前 LiveData 处于非活跃状态
     */
    protected void onInactive() {

    }


}

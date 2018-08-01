package org.wille.lifecycle.changeModel;

import org.wille.lifecycle.run.AppLifeCycleAble;

/**
 * 创建人员：杨浩
 * 创建日期：2018/8/1
 * 功能简介：
 */
public class MediatorCViewModel extends ViewModelLifecycle implements AppLifeCycleAble {

    // 是否已经结束了生命周期
    private boolean isOnDestroy = DEF_BOOLEAN;


    @Override
    public boolean canDoNext() {
        return !isOnDestroy;
    }

    @Override
    public void onDestroy() {
        isOnDestroy = TRUE_BOOLEAN;
    }
}

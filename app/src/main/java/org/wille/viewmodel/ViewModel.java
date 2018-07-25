package org.wille.viewmodel;

import org.wille.viewmodel.LiveData.LiveData;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/18
 * 功能简介：
 */
public class ViewModel<T> extends ViewModelLifecycle {

    private LiveData<T> mLiveData = null;






    @Override
    public void onDestroy() {

    }
}

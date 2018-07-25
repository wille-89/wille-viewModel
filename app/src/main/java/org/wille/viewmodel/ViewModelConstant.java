package org.wille.viewmodel;

import android.support.annotation.IntDef;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/18
 * 功能简介：
 */
public class ViewModelConstant {
    // 数据变更的模式
    public static final int LOAD_DATA_INIT = 0;     // 初始化加载
    public static final int LOAD_DATA_REFRESH = 1;  // 下拉刷新
    public static final int LOAD_DATA_MORE = 2;     // 加载更多
    public static final int LOAD_DATA_DEF = -1;     // 默认加载

    @IntDef({LOAD_DATA_INIT, LOAD_DATA_REFRESH, LOAD_DATA_MORE, LOAD_DATA_DEF})
    public @interface LoadDataType {
    }
}

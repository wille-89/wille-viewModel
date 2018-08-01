package org.wille.lifecycle.changeModel;

import org.wille.lifecycle.run.ViewModelConstant;

import static org.wille.lifecycle.run.ViewModelConstant.LOAD_DATA_DEF;
import static org.wille.lifecycle.run.ViewModelConstant.LOAD_DATA_MORE;
import static org.wille.lifecycle.run.ViewModelConstant.LOAD_DATA_REFRESH;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/27
 * 功能简介：
 */
public abstract class ListCViewModel<T> extends CViewModel<T> {

    // 分页加载下标
    private int mPageLocation = DEF_PAGE;
    // 默认页码
    protected static final int DEF_PAGE = 1;


    @Override
    public final void loadData() {
        loadData(LOAD_DATA_DEF);
    }

    /**
     * 加载数据源入口
     *
     * @param type 获取数据类型，是 init or result or addMore or def
     */
    public abstract void loadData(@ViewModelConstant.LoadDataType int type);

    /**
     * 处理当前页码
     *
     * @param type
     */
    protected void dealWithPage(int type) {
        // 判断是否需要Page++
        switch (type) {
            case LOAD_DATA_DEF:{
                mPageLocation = DEF_PAGE;
                break;
            }
            case LOAD_DATA_REFRESH: {
                mPageLocation = DEF_PAGE;
                break;
            }
            case LOAD_DATA_MORE: {
                mPageLocation = mPageLocation + 1;
                break;
            }
        }
    }

    /**
     * 获取当前页码
     *
     * @return
     */
    protected int getPageLocation() {
        return mPageLocation;
    }

    /**
     * 网络请求错误情况下处理当前页码
     *
     * @param type
     */
    protected void networkError(int type) {
        // 区分是加载更多还是初始化
        if (type == LOAD_DATA_MORE) {
            // 如果加载更多失败
            mPageLocation = mPageLocation - 1;
        }
    }

}

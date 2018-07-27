package org.wille.lifecycle.run;

/**
 * 创建人员：杨浩
 * 创建日期：2018/5/3.
 * 功能简介：
 */

public interface AppLifeCycleAble {

    /**
     * 是否产生向下调用事件
     * @return
     */
    public boolean canDoNext();

}

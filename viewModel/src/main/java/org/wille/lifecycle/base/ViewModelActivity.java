package org.wille.lifecycle.base;

import android.arch.lifecycle.ViewModel;
import android.support.v7.app.AppCompatActivity;

import org.wille.lifecycle.changeModel.CViewModel;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/27
 * 功能简介：
 */
public abstract class ViewModelActivity<T extends CViewModel<X>,X> extends AppCompatActivity {

    public  T getViewModel(){
        return null;
    }
}

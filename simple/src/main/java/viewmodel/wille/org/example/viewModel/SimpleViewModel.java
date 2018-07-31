package viewmodel.wille.org.example.viewModel;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import org.wille.lifecycle.changeModel.CViewModel;

import viewmodel.wille.org.example.bean.SimpleBean;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/31
 * 功能简介：
 */
public class SimpleViewModel extends CViewModel<SimpleBean> {


    @Override
    public void loadData() {
        // 模拟网络请求
    }

    public void loadData(String info) {
        postValue(SimpleBean.getSimple(info));
    }


}

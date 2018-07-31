package viewmodel.wille.org.example;

import android.app.Application;

import org.wille.lifecycle.utils.ActivityManagerInstance;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/31
 * 功能简介：
 */
public class IndexApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        ActivityManagerInstance.getInstance().bindApplication(this);
    }
}

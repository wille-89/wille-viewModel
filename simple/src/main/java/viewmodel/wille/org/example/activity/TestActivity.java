package viewmodel.wille.org.example.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.wille.lifecycle.changeData.Observer;
import org.wille.lifecycle.run.WilleErrorConverter;
import org.wille.lifecycle.utils.ActivityManagerInstance;

import java.util.Random;

import viewmodel.wille.org.example.R;
import viewmodel.wille.org.example.bean.SimpleBean;
import viewmodel.wille.org.example.viewModel.SimpleViewModel;

public class TestActivity extends AppCompatActivity {

    Button btnSend = null;
    TextView mTvObserve = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        btnSend = findViewById(R.id.btn_sendMessage);
        mTvObserve = findViewById(R.id.tv_observe);

        // 监听第一个 Activity 的 ViewModel
        // TODO: 2018/7/31 可以尝试根据ViewModel 的class 来查询
        final MainActivity activity = ActivityManagerInstance
                .getInstance()
                .of(MainActivity.class);

        if (activity != null) {
            activity.getViewModel().observe(this, new Observer<SimpleBean>() {
                @Override
                public void onChanged(@NonNull SimpleBean simpleBean, int type) {
                    mTvObserve.setText(simpleBean.getSimple());
                }

                @Override
                public void onError(WilleErrorConverter error) {
                    String errorInfo = "出现错误：" + error.getErrorObj().getErrorInfo();
                    mTvObserve.setText(errorInfo);
                }
            });
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity != null) {
                    activity.getViewModel().loadData("当然是测试：" + new Random().nextInt(100));
                }
            }
        });
    }


    public static void launch(Context context) {
        Intent starter = new Intent(context, TestActivity.class);
        context.startActivity(starter);
    }
}

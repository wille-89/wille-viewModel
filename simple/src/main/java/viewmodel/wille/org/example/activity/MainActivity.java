package viewmodel.wille.org.example.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.wille.lifecycle.base.ViewModelActivity;
import org.wille.lifecycle.changeData.Observer;
import org.wille.lifecycle.run.WilleErrorConverter;

import viewmodel.wille.org.example.R;
import viewmodel.wille.org.example.bean.SimpleBean;
import viewmodel.wille.org.example.viewModel.SimpleViewModel;

public class MainActivity extends ViewModelActivity {

    TextView mTvObserve = null;
    Button btnNext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvObserve = findViewById(R.id.tv_observe);
        getViewModel()
                .bindLifecycle(this)
                .observe(this, new Observer<SimpleBean>() {
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
        btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestActivity.launch(MainActivity.this);
            }
        });
    }


    public SimpleViewModel getViewModel() {
        return ViewModelProviders.of(this)
                .get(SimpleViewModel.class);
    }
}

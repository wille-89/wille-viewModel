package org.wille.test;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.wille.viewmodel.LiveData.LiveData;
import org.wille.viewmodel.LiveData.Observer;
import org.wille.viewmodel.R;
import org.wille.viewmodel.run.BasisError;

public class TestActivity extends AppCompatActivity {

    private static LiveData<String> test1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        test1 = new LiveData<>();
        test1.observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s, @NonNull int type) {
                Log.d("观察者1", "收到更新:" + s);
            }

            @Override
            public void onError(BasisError error) {

            }
        });
        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Test2Activity.launch(TestActivity.this);
            }
        });

        new Runnable() {

            @Override
            public void run() {
                Log.e("当前线程1", "  " + Looper.myLooper().getThread().getName());
            }
        }.run();

        new Thread(new Runnable() {

            @Override
            public void run() {
                Log.e("当前线程2", "  " + Looper.myLooper().getThread().getName());
            }
        }).start();
    }

    public static LiveData<String> getTest1() {
        return test1;
    }
}

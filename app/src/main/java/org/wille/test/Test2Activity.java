package org.wille.test;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.wille.viewmodel.LiveData.Observer;
import org.wille.viewmodel.R;
import org.wille.viewmodel.run.BasisError;

import static android.arch.lifecycle.Lifecycle.State.STARTED;
import static org.wille.viewmodel.ViewModelConstant.LOAD_DATA_INIT;

public class Test2Activity extends AppCompatActivity {

    private int size = 0;
    private final int maxSize = 10;
    private View mView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);

        TestActivity.getTest1().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s, @NonNull int type) {
                Log.d("观察者2", "收到更新:" + s);
            }

            @Override
            public void onError(BasisError error) {

            }
        });
        TestActivity.getTest1().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s, @NonNull int type) {
                Log.d("观察者3", "收到更新:" + s);
            }

            @Override
            public void onError(BasisError error) {

            }
        });
        TestActivity.getTest1().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s, @NonNull int type) {
                Log.d("观察者4", "收到更新:" + s);
            }

            @Override
            public void onError(BasisError error) {

            }
        });

        mView = new View(this);
//        mView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (size >= maxSize) {
//                    return;
//                }
//                mView.postDelayed(this, 100);
//                TestActivity.getTest1().postValue(size + "", LOAD_DATA_INIT);
//                size = size + 1;
//            }
//        }, 100);

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 10; i > 0; i--) {
                    TestActivity.getTest1().postValue(i + "线程", LOAD_DATA_INIT);
                }
            }
        }).start();

        findViewById(R.id.btn_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestActivity.getTest1().postValue("主动发送测试", LOAD_DATA_INIT);
            }
        });
    }


    public static void launch(Context context) {
        Intent starter = new Intent(context, Test2Activity.class);
        context.startActivity(starter);
    }
}

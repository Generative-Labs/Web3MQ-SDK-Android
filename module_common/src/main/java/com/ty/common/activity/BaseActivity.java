package com.ty.common.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ty.common.R;

public class BaseActivity extends AppCompatActivity {
    private ViewGroup root_view;
    private ConstraintLayout cl_loading;
    private ImageView iv_loading;
    private Animation loadAnimation;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ARouter.getInstance().inject(this);
        setContentView(R.layout.base_activity);
        root_view = findViewById(R.id.root);
        cl_loading = findViewById(R.id.cl_loading);
        iv_loading = findViewById(R.id.iv_loading);
        loadAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
    }

    protected void setContent(int view_id){
        View view = LayoutInflater.from(this).inflate(view_id,null,false);
        root_view.addView(view,0);
    }

    protected void showLoading(){
        cl_loading.setVisibility(View.VISIBLE);
        doLoadingAnimate();
    }

    protected void hideLoading(){
        cl_loading.setVisibility(View.GONE);
        stopLoadingAnimate();
    }


    private void doLoadingAnimate(){
        iv_loading.startAnimation(loadAnimation);
    }

    private void stopLoadingAnimate() {
        iv_loading.clearAnimation();
    }
}

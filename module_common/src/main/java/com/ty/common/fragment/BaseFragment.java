package com.ty.common.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.ty.common.R;
import com.ty.common.utils.CommonUtils;

public class BaseFragment extends Fragment {
    protected ConstraintLayout rootView;
    private int layoutId;
    private ConstraintLayout cl_loading;
    private ImageView iv_loading;
    private Animation loadAnimation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setContent(int layoutId){
        this.layoutId = layoutId;
    }

    protected void onBaseCreateView() {

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ConstraintLayout) inflater.inflate(R.layout.base_fragment,null);
        View view = LayoutInflater.from(getActivity()).inflate(layoutId,rootView,false);
        cl_loading = rootView.findViewById(R.id.cl_loading);
        iv_loading = rootView.findViewById(R.id.iv_loading);
        rootView.addView(view);
        loadAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.rotate);
        onBaseCreateView();
        return rootView;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void showLoadingDialog(){
        cl_loading.setVisibility(View.VISIBLE);
        doLoadingAnimate();
    }

    public void hideLoadingDialog(){
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

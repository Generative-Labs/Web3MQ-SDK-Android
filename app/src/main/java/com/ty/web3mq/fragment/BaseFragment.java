package com.ty.web3mq.fragment;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ty.web3_mq.utils.CommonUtils;
import com.ty.web3mq.R;

public class BaseFragment extends Fragment {
    protected View rootView;
    private int layoutId;
    private boolean isFreshAble;
//    private AlertDialog alertDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout cl_loading;
    private ImageView iv_loading;
    private Animation loadAnimation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setContent(int layoutId, boolean isFreshAble){
        this.layoutId = layoutId;
        this.isFreshAble = isFreshAble;
    }

    protected void onBaseCreateView() {

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.base_fragment,null);
        swipeRefreshLayout = rootView.findViewById(R.id.refresh_layout);
        cl_loading = rootView.findViewById(R.id.cl_loading);
        iv_loading = rootView.findViewById(R.id.iv_loading);
        View view = LayoutInflater.from(getActivity()).inflate(layoutId,null,false);
        swipeRefreshLayout.addView(view,0);
        swipeRefreshLayout.setEnabled(isFreshAble);
        int start = CommonUtils.dp2px(getActivity(),70);
        int end = CommonUtils.dp2px(getActivity(),130);
        swipeRefreshLayout.setProgressViewOffset(false,start,end);
        loadAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.rotate);
        onBaseCreateView();
        return rootView;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected void stopRefresh(){
        swipeRefreshLayout.setRefreshing(false);
    }

    protected void setRefreshListener(SwipeRefreshLayout.OnRefreshListener onRefreshListener){
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
    }

    protected void showLoading(){
//        if (getActivity() == null || getActivity().isDestroyed() || getActivity().isFinishing()) {
//            return;
//        }
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_loading,null);
//        builder.setView(v);
//        builder.setCancelable(false);
//        alertDialog = builder.create();
//        alertDialog.show();
//        alertDialog.getWindow().setLayout(CommonUtils.dp2px(getActivity(),335),CommonUtils.dp2px(getActivity(),205));
        cl_loading.setVisibility(View.VISIBLE);
        doLoadingAnimate();
    }

    protected void hideLoading(){
//        if(alertDialog!=null) {
//            alertDialog.dismiss();
//        }
        cl_loading.setVisibility(View.GONE);
        stopLoadingAnimate();
    }

//    protected void showLoading(){
//        cl_loading.setVisibility(View.VISIBLE);
//        doLoadingAnimate();
//    }
//
//    protected void hideLoading(){
//        cl_loading.setVisibility(View.GONE);
//        stopLoadingAnimate();
//    }

    private void doLoadingAnimate(){
        iv_loading.startAnimation(loadAnimation);
    }

    private void stopLoadingAnimate() {
        iv_loading.clearAnimation();
    }


}

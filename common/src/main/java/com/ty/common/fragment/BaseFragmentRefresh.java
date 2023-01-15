package com.ty.common.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ty.common.R;
import com.ty.common.utils.CommonUtils;


public class BaseFragmentRefresh extends Fragment {
    protected View rootView;
    private int layoutId;
    private boolean isFreshAble;
    private AlertDialog alertDialog;
    private SwipeRefreshLayout swipeRefreshLayout;

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
        rootView = inflater.inflate(R.layout.base_fragment_refresh,null);
        swipeRefreshLayout = rootView.findViewById(R.id.refresh_layout);
        View view = LayoutInflater.from(getActivity()).inflate(layoutId,null,false);
        swipeRefreshLayout.addView(view);
        swipeRefreshLayout.setEnabled(isFreshAble);
        int start = CommonUtils.dp2px(getActivity(),70);
        int end = CommonUtils.dp2px(getActivity(),130);
        swipeRefreshLayout.setProgressViewOffset(false,start,end);
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

    protected void showLoadingDialog(){
        if (getActivity() == null || getActivity().isDestroyed() || getActivity().isFinishing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_loading,null);
        builder.setView(v);
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout(CommonUtils.dp2px(getActivity(),335),CommonUtils.dp2px(getActivity(),205));
    }

    protected void hideLoadingDialog(){
        if(alertDialog!=null) {
            alertDialog.dismiss();
        }
    }
}

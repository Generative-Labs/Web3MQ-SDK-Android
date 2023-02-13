package com.ty.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ty.common.R;
import com.ty.common.utils.CommonUtils;


public class Web3MQListView extends SwipeRefreshLayout {
    private ConstraintLayout cl_empty_view;
    private RecyclerView recyclerview;
    private ImageView iv_empty_icon;
    private TextView tv_empty_message;
    public Web3MQListView(@NonNull Context context) {
        this(context,null);
    }

    public Web3MQListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context){
        View rootView = LayoutInflater.from(context).inflate(R.layout.view_web3mq_list,this);
        cl_empty_view = rootView.findViewById(R.id.cl_empty_view);
        recyclerview = rootView.findViewById(R.id.recyclerview);
        iv_empty_icon = rootView.findViewById(R.id.iv_empty_icon);
        tv_empty_message = rootView.findViewById(R.id.tv_empty_message);
        recyclerview.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false));
        int start = CommonUtils.dp2px(context,70);
        int end = CommonUtils.dp2px(context,130);
        setProgressViewOffset(false,start,end);
    }

    public void setAdapter(RecyclerView.Adapter adapter){
        recyclerview.setAdapter(adapter);
    }

    public void setEmptyIcon(int drawable){
        iv_empty_icon.setImageResource(drawable);
    }

    public void setEmptyMessage(String message){
        tv_empty_message.setText(message);
    }

    public void showEmptyView(){
        cl_empty_view.setVisibility(VISIBLE);
    }

    public void hideEmptyView(){
        cl_empty_view.setVisibility(GONE);
    }

    public void scrollTo(int position){
        recyclerview.post(new Runnable() {
            @Override
            public void run() {
                recyclerview.scrollToPosition(position);
            }
        });
    }

}

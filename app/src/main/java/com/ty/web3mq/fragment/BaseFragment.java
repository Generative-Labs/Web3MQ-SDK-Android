package com.ty.web3mq.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {
    protected View rootView;
    private int layoutId;

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
        rootView = inflater.inflate(layoutId,null);
        onBaseCreateView();
        return rootView;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

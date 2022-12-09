package com.ty.web3mq.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ty.web3_mq.utils.CommonUtils;
import com.ty.web3mq.R;

public class BaseFragment extends Fragment {
    protected View rootView;
    private int layoutId;
    private AlertDialog alertDialog;

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

    protected void showLoadingDialog(){
        if(alertDialog==null){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_loading,null);
            builder.setView(v);
            alertDialog = builder.create();
            alertDialog.show();
            alertDialog.getWindow().setLayout(CommonUtils.dp2px(getActivity(),335),CommonUtils.dp2px(getActivity(),205));
        }else{
            alertDialog.show();
        }
    }

    protected void hideLoadingDialog(){
        if(alertDialog!=null) {
            alertDialog.dismiss();
        }
    }
}

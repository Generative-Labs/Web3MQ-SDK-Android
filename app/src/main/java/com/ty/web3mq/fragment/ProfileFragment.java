package com.ty.web3mq.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ty.web3_mq.Web3MQClient;
import com.ty.web3_mq.Web3MQUser;
import com.ty.web3_mq.http.beans.ProfileBean;
import com.ty.web3_mq.interfaces.GetMyProfileCallback;
import com.ty.web3_mq.interfaces.PostMyProfileCallback;
import com.ty.web3_mq.utils.CommonUtils;
import com.ty.web3mq.R;

public class ProfileFragment extends BaseFragment {
    private static final String TAG = "ProfileFragment";
    private static ProfileFragment instance;
    private ImageView iv_avatar;
    private TextView tv_nickname,tv_userid,tv_did_value;
    private ConstraintLayout cl_change_avatar,cl_edit_nickname;
    private AlertDialog alertDialog;
    private Button btn_logout;

    public static synchronized ProfileFragment getInstance() {
        if (instance == null) {
            instance = new ProfileFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_profile,false);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        requestData();
        initView();
        setListener();
    }

    private void requestData(){
        showLoading();
        Web3MQUser.getInstance().getMyProfile(new GetMyProfileCallback() {
            @Override
            public void onSuccess(ProfileBean profileBean) {
                hideLoading();
                updateView(profileBean);
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"error:"+error, Toast.LENGTH_SHORT).show();
                hideLoading();
            }
        });
    }

    private void updateView(ProfileBean profileBean){
        tv_nickname.setText(profileBean.nickname);
        tv_userid.setText(profileBean.userid);
        if(profileBean.wallet_type!=null && profileBean.wallet_address!=null){
            tv_did_value.setText(profileBean.wallet_type+":"+profileBean.wallet_address);
        }
    }

    private void initView() {
        iv_avatar = rootView.findViewById(R.id.iv_avatar);
        tv_nickname = rootView.findViewById(R.id.tv_nickname);
        tv_userid = rootView.findViewById(R.id.tv_userid);
        tv_did_value = rootView.findViewById(R.id.tv_did_value);
        cl_change_avatar = rootView.findViewById(R.id.cl_change_avatar);
        cl_edit_nickname = rootView.findViewById(R.id.cl_edit_nickname);
        btn_logout = rootView.findViewById(R.id.btn_logout);
    }

    private void setListener() {
        cl_edit_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditNickNameDialog();
            }
        });
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Web3MQClient.getInstance().close();
                getActivity().finish();
            }
        });
    }

    private void showEditNickNameDialog(){
        if(alertDialog==null){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_nickname,null);
            Button btn_cancel = v.findViewById(R.id.btn_cancel);
            Button btn_confirm = v.findViewById(R.id.btn_confirm);
            EditText et_nickname = v.findViewById(R.id.et_nickname);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

            btn_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nickname = et_nickname.getText().toString();
                    editNicknameRequest(nickname);
                }
            });
            builder.setView(v);
            alertDialog = builder.create();
            alertDialog.show();
            alertDialog.getWindow().setLayout(CommonUtils.dp2px(getActivity(),335),CommonUtils.dp2px(getActivity(),205));
        }else{
            alertDialog.show();
        }
    }

    private void editNicknameRequest(String nickname) {
        Web3MQUser.getInstance().postMyProfile(nickname, "", new PostMyProfileCallback() {
            @Override
            public void onSuccess(ProfileBean profileBean) {
                updateView(profileBean);
                if(alertDialog!=null){
                    alertDialog.dismiss();
                }
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"error:"+error, Toast.LENGTH_SHORT).show();
                if(alertDialog!=null){
                    alertDialog.dismiss();
                }
            }
        });
    }
}

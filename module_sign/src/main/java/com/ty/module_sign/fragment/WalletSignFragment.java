package com.ty.module_sign.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ty.common.fragment.BaseFragment;
import com.ty.module_sign.R;
import com.ty.module_sign.interfaces.OnConnectCallback;
import com.ty.module_sign.interfaces.OnSignCallback;
import com.ty.module_sign.interfaces.WalletInitCallback;
import com.ty.web3_mq.Web3MQClient;
import com.ty.web3_mq.Web3MQSign;
import com.ty.web3_mq.interfaces.BridgeConnectCallback;
import com.ty.web3_mq.interfaces.ConnectCallback;
import com.ty.web3_mq.utils.AppUtils;
import com.ty.web3_mq.websocket.bean.BridgeMessageWalletInfo;

import org.jetbrains.annotations.NotNull;

public class WalletSignFragment extends BaseFragment {
    private static WalletSignFragment instance;
    private static final String TAG = "WalletSignFragment";
    private BottomSheetDialog bottomSheetDialog;
    private OnConnectCallback connectCallback;
    private OnSignCallback onSignCallback;

    public static synchronized WalletSignFragment getInstance() {
        if (instance == null) {
            instance = new WalletSignFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_wallet_sign);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
    }

    public void init(String dAppID, String topicId, WalletInitCallback callback){
        Web3MQClient.getInstance().startConnect(new ConnectCallback() {
            @Override
            public void onSuccess() {
                Web3MQSign.getInstance().init(dAppID, new BridgeConnectCallback() {
                    @Override
                    public void onConnectCallback() {
                        callback.onSuccess();
                    }
                });
                Web3MQSign.getInstance().setTargetTopicID(topicId);
            }

            @Override
            public void onFail(String error) {
                callback.onFail("connect websocket error:"+error);
            }
        });
    }



    public void setOnSignCallback(OnSignCallback onSignCallback){
        this.onSignCallback= onSignCallback;
    }

    public void setOnConnectCallback(OnConnectCallback onConnectCallback){
        this.connectCallback = onConnectCallback;
    }

    public void showConnectBottomDialog(String website, String iconUrl,@NotNull BridgeMessageWalletInfo walletInfo) {
        if(bottomSheetDialog!=null && bottomSheetDialog.isShowing()){
            bottomSheetDialog.dismiss();
        }
        bottomSheetDialog = new BottomSheetDialog(getActivity());
        View view = View.inflate(getActivity(),R.layout.bottom_dialog_connect,null);
        Button btn_connect = view.findViewById(R.id.btn_connect);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        TextView tv_website_url = view.findViewById(R.id.tv_website_url);
        ImageView iv_website_icon = view.findViewById(R.id.iv_website_icon);
        TextView tv_address = view.findViewById(R.id.tv_address);
        if(website!=null){
            tv_website_url.setText(website);
        }
        if(iconUrl!=null){
            Glide.with(getActivity()).load(iconUrl).into(iv_website_icon);
        }
        tv_address.setText(walletInfo.address);

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Web3MQSign.getInstance().sendConnectResponse(true,walletInfo,false);
                bottomSheetDialog.dismiss();
                if(connectCallback !=null){
                    connectCallback.connectApprove();
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Web3MQSign.getInstance().sendConnectResponse(false,null,false);
                bottomSheetDialog.dismiss();
                if(connectCallback !=null){
                    connectCallback.connectReject();
                }
            }
        });
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet)
                .setBackgroundResource(android.R.color.transparent);
        bottomSheetDialog.show();
    }

    public void showSignBottomDialog(String website, String iconUrl, @NotNull String address,@NotNull String sign_content){
        if(bottomSheetDialog!=null && bottomSheetDialog.isShowing()){
            bottomSheetDialog.dismiss();
        }
        bottomSheetDialog = new BottomSheetDialog(getActivity());
        View view = View.inflate(getActivity(),R.layout.bottom_dialog_sign,null);
        Button btn_sign = view.findViewById(R.id.btn_sign);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        TextView tv_website_url = view.findViewById(R.id.tv_website_url);
        ImageView iv_website_icon = view.findViewById(R.id.iv_website_icon);
        TextView tv_address = view.findViewById(R.id.tv_address);
        TextView tv_sign_content  =view.findViewById(R.id.tv_sign_content);
        if(website!=null){
            tv_website_url.setText(website);
        }
        if(iconUrl!=null){
            Glide.with(getActivity()).load(address).into(iv_website_icon);
        }
        tv_address.setText(address);
        tv_sign_content.setText(sign_content);

        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onSignCallback!=null ){
                    String signature = onSignCallback.sign(sign_content);
                    Web3MQSign.getInstance().sendSignResponse(true,signature,false);
                    bottomSheetDialog.dismiss();
                    onSignCallback.signApprove();
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onSignCallback!=null ){
                    Web3MQSign.getInstance().sendSignResponse(false,null,false);
                    bottomSheetDialog.dismiss();
                    onSignCallback.signReject();
                }
            }
        });
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet)
                .setBackgroundResource(android.R.color.transparent);
        bottomSheetDialog.show();
    }

}

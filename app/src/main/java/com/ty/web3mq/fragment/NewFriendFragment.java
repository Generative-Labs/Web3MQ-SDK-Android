package com.ty.web3mq.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ty.web3_mq.Web3MQContacts;
import com.ty.web3_mq.http.beans.ContactBean;
import com.ty.web3_mq.http.beans.ContactsBean;
import com.ty.web3_mq.http.beans.FriendRequestBean;
import com.ty.web3_mq.http.beans.FriendRequestsBean;
import com.ty.web3_mq.interfaces.FriendRequestCallback;
import com.ty.web3_mq.interfaces.GetReceiveFriendRequestListCallback;
import com.ty.web3_mq.interfaces.HandleFriendRequestCallback;
import com.ty.web3_mq.utils.CommonUtils;
import com.ty.web3mq.R;
import com.ty.web3mq.adapter.NewFriendAdapter;

import java.util.ArrayList;

public class NewFriendFragment extends BaseFragment {
    private static final String TAG = "NewFriendFragment";
    private static NewFriendFragment instance;
    private AlertDialog alertDialog;
    private ImageView iv_add;
    private NewFriendAdapter adapter;
    private RecyclerView recycler_view;

    public static synchronized NewFriendFragment getInstance() {
        if (instance == null) {
            instance = new NewFriendFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_new_friend,true);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        requestData();
        initView();
    }

    private void requestData() {
        showLoadingDialog();
        Web3MQContacts.getInstance().getReceiveFriendRequestList(1, 20, new GetReceiveFriendRequestListCallback() {

            @Override
            public void onSuccess(FriendRequestsBean friendRequestBeans) {
                updateNewFriendView(friendRequestBeans.result);
                hideLoadingDialog();
                stopRefresh();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"get friend request fail. error:"+error,Toast.LENGTH_SHORT).show();
                hideLoadingDialog();
                stopRefresh();
            }
        });

//        Web3MQContacts.getInstance().getSentFriendRequestList(1, 20, new GetSentFriendRequestListCallback() {
//            @Override
//            public void onSuccess(ContactsBean contactsBean) {
//                Log.i(TAG,"total:"+contactsBean.total);
//                updateNewFriendView(contactsBean.result);
//            }
//
//            @Override
//            public void onFail(String error) {
//                Toast.makeText(NewFriendActivity.this,"get friend request fail. error:"+error,Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void updateNewFriendView(ArrayList<FriendRequestBean> friendRequestBeans){
        adapter = new NewFriendAdapter(getActivity(),friendRequestBeans);
        recycler_view.setAdapter(adapter);
        adapter.setOnActionClickListener(new NewFriendAdapter.OnActionClickListener() {
            @Override
            public void onItemClick(int position) {
                FriendRequestBean contact = friendRequestBeans.get(position);
                handleFriendRequest(contact);
            }
        });
    }

    private void handleFriendRequest(FriendRequestBean contact) {
        Web3MQContacts.getInstance().handleFriendRequest(contact.userid, "agree", new HandleFriendRequestCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getActivity(),"agree success",Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"agree fail. error:"+error,Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initView() {
        iv_add = rootView.findViewById(R.id.iv_add);
        recycler_view = rootView.findViewById(R.id.recycler_view_new_friends);
        recycler_view.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewFriendDialog();
            }
        });

        setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData();
            }
        });
    }

    private void showNewFriendDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_friend,null);
        Button btn_cancel = v.findViewById(R.id.btn_cancel);
        Button btn_confirm = v.findViewById(R.id.btn_confirm);
        EditText et_userid = v.findViewById(R.id.et_userid);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String target_userid = et_userid.getText().toString();
                sendFriendRequest(target_userid);
            }
        });
        builder.setView(v);
        alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout(CommonUtils.dp2px(getActivity(),335),CommonUtils.dp2px(getActivity(),205));

    }

    private void sendFriendRequest(String target_userid){
        Web3MQContacts.getInstance().sendFriendRequest(target_userid, new FriendRequestCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getActivity(),"send friend request success",Toast.LENGTH_SHORT).show();
                if(alertDialog!=null){
                    alertDialog.dismiss();
                }
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"send friend request fail error:"+ error,Toast.LENGTH_SHORT).show();
                if(alertDialog!=null){
                    alertDialog.dismiss();
                }
            }
        });
    }
}

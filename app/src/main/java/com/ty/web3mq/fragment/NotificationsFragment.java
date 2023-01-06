package com.ty.web3mq.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ty.web3_mq.Web3MQNotification;
import com.ty.web3_mq.http.beans.NotificationBean;
import com.ty.web3_mq.http.beans.NotificationsBean;
import com.ty.web3_mq.interfaces.GetNotificationHistoryCallback;
import com.ty.web3_mq.interfaces.NotificationMessageCallback;
import com.ty.web3mq.R;
import com.ty.web3mq.adapter.NotificationsAdapter;

import java.util.ArrayList;

import web3mq.Message;

public class NotificationsFragment extends BaseFragment {
    private static NotificationsFragment instance;
    private RecyclerView recycler_view;
    private NotificationsAdapter adapter;
    private static final String TAG = "NotificationsFragment";
    private ArrayList<NotificationBean> notifications = new ArrayList<>();
    private ConstraintLayout cl_none_bg;
    public static synchronized NotificationsFragment getInstance() {
        if (instance == null) {
            instance = new NotificationsFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_notifications,true);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        requestData();
        initView();
        Web3MQNotification.getInstance().setOnNotificationMessageEvent(new NotificationMessageCallback() {

            @Override
            public void onNotificationMessage(ArrayList<NotificationBean> response) {
                notifications.addAll(response);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void requestData() {
        Web3MQNotification.getInstance().getNotificationHistory(1, 20, new GetNotificationHistoryCallback() {
            @Override
            public void onSuccess(NotificationsBean notificationsBean) {
                notifications.clear();
                notifications = notificationsBean.result;
                if(notifications.size()>0){
                    cl_none_bg.setVisibility(View.GONE);
                    recycler_view.setVisibility(View.VISIBLE);
                    adapter = new NotificationsAdapter(getActivity(),notifications);
                    recycler_view.setAdapter(adapter);
                }
                stopRefresh();
            }

            @Override
            public void onFail(String error) {
                Log.e(TAG,"onFail:"+error);
                stopRefresh();
            }
        });
    }

    private void initView() {
        recycler_view = rootView.findViewById(R.id.recycler_view);
        cl_none_bg = rootView.findViewById(R.id.cl_none_bg);
        recycler_view.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Web3MQNotification.getInstance().removeNotificationMessageEvent();
    }
}

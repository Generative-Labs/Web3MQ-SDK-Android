package com.ty.web3mq.fragment;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.web3_mq.Web3MQNotification;
import com.ty.web3_mq.http.beans.NotificationsBean;
import com.ty.web3_mq.interfaces.GetNotificationHistoryCallback;
import com.ty.web3mq.R;
import com.ty.web3mq.adapter.NotificationsAdapter;

public class NotificationsFragment extends BaseFragment {
    private static NotificationsFragment instance;
    private RecyclerView recycler_view;
    private NotificationsAdapter adapter;
    private static final String TAG = "NotificationsFragment";
    public static synchronized NotificationsFragment getInstance() {
        if (instance == null) {
            instance = new NotificationsFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_notifications);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        requestData();
        initView();
    }

    private void requestData() {
        showLoadingDialog();
        Web3MQNotification.getInstance().getNotificationHistory(1, 10, new GetNotificationHistoryCallback() {
            @Override
            public void onSuccess(NotificationsBean notificationsBean) {
                adapter = new NotificationsAdapter(getActivity(),notificationsBean.result);
                recycler_view.setAdapter(adapter);
                hideLoadingDialog();
            }

            @Override
            public void onFail(String error) {
                Log.e(TAG,"onFail:"+error);
                hideLoadingDialog();
            }
        });
    }

    private void initView() {
        recycler_view = rootView.findViewById(R.id.recycler_view);
        recycler_view.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
    }
}

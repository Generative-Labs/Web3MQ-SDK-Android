package com.ty.web3mq.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ty.web3mq.R;

public class NotificationsFragment extends BaseFragment {
    private View rootView;
    private static NotificationsFragment instance;
    public static synchronized NotificationsFragment getInstance() {
        if (instance == null) {
            instance = new NotificationsFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_notifications,null);
        return rootView;
    }
}

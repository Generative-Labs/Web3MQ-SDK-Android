package com.ty.web3mq.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ty.web3mq.R;

public class ChatsFragment extends BaseFragment {
    private View rootView;
    private static ChatsFragment instance;
    public static synchronized ChatsFragment getInstance() {
        if (instance == null) {
            instance = new ChatsFragment();
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
        rootView = inflater.inflate(R.layout.fragment_chats,null);
        return rootView;
    }
}

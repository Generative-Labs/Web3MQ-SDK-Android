package com.ty.web3mq.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.web3mq.R;

public class NewFriendItemViewHolder extends RecyclerView.ViewHolder{
    TextView tv_userid;
    TextView tv_action;
    public NewFriendItemViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_userid = itemView.findViewById(R.id.tv_userid);
        tv_action = itemView.findViewById(R.id.tv_action);
    }

    void setTv_userid(String userid){
        tv_userid.setText(userid);
    }
}

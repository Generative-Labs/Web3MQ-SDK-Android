package com.ty.web3mq.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.web3mq.R;

public class NewFriendItemViewHolder extends RecyclerView.ViewHolder{
    public TextView tv_userid;
    public TextView tv_action;
    public NewFriendItemViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_userid = itemView.findViewById(R.id.tv_userid);
        tv_action = itemView.findViewById(R.id.tv_action);
    }

    public void setTv_userid(String userid){
        tv_userid.setText(userid);
    }
}

package com.ty.web3mq.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.web3mq.R;

public class GroupMemberViewHolder extends RecyclerView.ViewHolder{
    public TextView tv_user_id;
    public GroupMemberViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_user_id = itemView.findViewById(R.id.tv_user_id);
    }

    public void setTv_user_id(String user_id){
        tv_user_id.setText(user_id);
    }
}

package com.ty.web3mq.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.web3mq.R;

public class ContactViewHolder extends RecyclerView.ViewHolder{
    public TextView tv_userid;
    public ContactViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_userid = itemView.findViewById(R.id.tv_userid);
    }

    public void setTv_userid(String userid){
        tv_userid.setText(userid);
    }
}

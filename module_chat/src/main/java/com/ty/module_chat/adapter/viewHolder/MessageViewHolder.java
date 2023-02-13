package com.ty.module_chat.adapter.viewHolder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.module_chat.R;
import com.ty.web3_mq.utils.DateUtils;

public class MessageViewHolder extends RecyclerView.ViewHolder{
    public TextView tv_userid, tv_content, tv_timestamp;
    public ImageView iv_icon;
    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_userid = itemView.findViewById(R.id.tv_userid);
        tv_content = itemView.findViewById(R.id.tv_content);
        tv_timestamp = itemView.findViewById(R.id.tv_timestamp);
    }

    public void setTv_userid(String userid) {
        this.tv_userid.setText(userid);
    }

    public void setTv_content(String content) {
        this.tv_content.setText(content);
    }

    public void setTv_timestamp(long timestamp) {
        this.tv_timestamp.setText(DateUtils.getTimeStringH(timestamp));
    }

    public void setAvatarUrl(String avatarUrl, Context context){

    }
}

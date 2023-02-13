package com.ty.module_chat.adapter.viewHolder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ty.module_chat.R;
import com.ty.web3_mq.http.beans.FollowerBean;

public class InviteGroupViewHolder extends RecyclerView.ViewHolder{
    public TextView tv_user_name;
    private ImageView iv_avatar;
    public CheckBox checkbox;
    private Context context;

    public InviteGroupViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        this.context = context;
        tv_user_name = itemView.findViewById(R.id.tv_user_name);
        iv_avatar = itemView.findViewById(R.id.iv_avatar);
        checkbox = itemView.findViewById(R.id.checkbox);
    }


    public void setFollower(FollowerBean follower){
        tv_user_name.setText(follower.userid);
        if(!TextUtils.isEmpty(follower.avatar_url)){
            Glide.with(context).load(follower.avatar_url).into(iv_avatar);
        }
    }
}

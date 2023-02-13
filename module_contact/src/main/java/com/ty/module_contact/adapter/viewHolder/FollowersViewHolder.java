package com.ty.module_contact.adapter.viewHolder;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ty.common.config.Constants;
import com.ty.module_contact.R;
import com.ty.module_contact.bean.FollowItem;
import com.ty.web3_mq.utils.AppUtils;

public class FollowersViewHolder extends RecyclerView.ViewHolder{
    public TextView tv_user_name;
    public Button btn_follow;
    private ImageView iv_avatar;
    private Context context;
    public FollowersViewHolder(@NonNull View itemView,Context context) {
        super(itemView);
        this.context = context;
        tv_user_name = itemView.findViewById(R.id.tv_user_name);
        iv_avatar = itemView.findViewById(R.id.iv_avatar);
        btn_follow = itemView.findViewById(R.id.btn_follow);
    }

    public void setFollower(FollowItem follower){
        tv_user_name.setText(follower.userName);
        if(!TextUtils.isEmpty(follower.avatar_url)){
            Glide.with(context).load(follower.avatar_url).into(iv_avatar);
        }
        if(follower.follow_status.equals(Constants.FOLLOW_STATUS_FOLLOWER)){
            btn_follow.setBackgroundResource(R.drawable.shape_bg_btn_follow);
            btn_follow.setTextColor(Color.parseColor("#FFFFFF"));
            btn_follow.setText("Follow");
        }else{
            btn_follow.setBackgroundResource(R.drawable.shape_bg_btn_following);
            btn_follow.setTextColor(Color.parseColor("#18181B"));
            btn_follow.setText("Following");
        }
    }
}

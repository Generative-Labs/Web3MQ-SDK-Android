package com.ty.moudle_new_message.adapter.viewHolder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ty.common.config.Constants;
import com.ty.moudle_new_message.R;
import com.ty.moudle_new_message.adapter.FriendListAdapter;
import com.ty.web3_mq.http.beans.FollowerBean;

public class FriendListViewHolder extends RecyclerView.ViewHolder{
    public TextView tv_user_name;
    public Button btn_action;
    private ImageView iv_avatar;
    public CheckBox checkbox;
    private Context context;
    private String style = FriendListAdapter.STYLE_DEFAULT;

    public FriendListViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        this.context = context;
        tv_user_name = itemView.findViewById(R.id.tv_user_name);
        iv_avatar = itemView.findViewById(R.id.iv_avatar);
        btn_action = itemView.findViewById(R.id.btn_action);
        checkbox = itemView.findViewById(R.id.checkbox);
    }

    public void setStyle(String style){
        this.style = style;
    }

    public void setFollower(FollowerBean follower){
        switch (style){
            case FriendListAdapter.STYLE_DEFAULT:
                btn_action.setVisibility(View.GONE);
                checkbox.setVisibility(View.GONE);
                break;
            case FriendListAdapter.STYLE_SEARCH:
                btn_action.setVisibility(View.VISIBLE);
                checkbox.setVisibility(View.GONE);
                break;
            case FriendListAdapter.STYLE_CREATE_ROOM:
                btn_action.setVisibility(View.GONE);
                checkbox.setVisibility(View.VISIBLE);
                break;
        }

        tv_user_name.setText(follower.userid);
        if(!TextUtils.isEmpty(follower.avatar_url)){
            Glide.with(context).load(follower.avatar_url).into(iv_avatar);
        }
        switch (follower.follow_status){
            case Constants.FOLLOW_STATUS_FOLLOWER:
                btn_action.setText("Follow");
                break;
            case Constants.FOLLOW_STATUS_EACH:
                btn_action.setVisibility(View.GONE);
                break;
            case Constants.FOLLOW_STATUS_FOLLOWING:
                btn_action.setText("Request");
                break;
        }
    }
}

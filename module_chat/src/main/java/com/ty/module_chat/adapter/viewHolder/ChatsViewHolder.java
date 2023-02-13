package com.ty.module_chat.adapter.viewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.module_chat.R;
import com.ty.web3_mq.utils.AppUtils;
import com.ty.web3_mq.utils.DateUtils;

public class ChatsViewHolder extends RecyclerView.ViewHolder{
    public TextView tv_title, tv_content, tv_timestamp,tv_unread_count;
    public ImageView iv_icon;
    public ChatsViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_title = itemView.findViewById(R.id.tv_title);
        tv_content = itemView.findViewById(R.id.tv_content);
        tv_timestamp = itemView.findViewById(R.id.tv_timestamp);
        iv_icon = itemView.findViewById(R.id.iv_icon);
        tv_unread_count = itemView.findViewById(R.id.tv_unread_count);
    }

    public void setTv_title(String title){
        tv_title.setText(title);
    }
    public void setTv_content(String content){
        tv_content.setText(content);
    }
    public void setTv_timestamp(long timestamp){
        if(timestamp!=0){
            String date = DateUtils.getTimeString(timestamp);
            tv_timestamp.setText(date);
        }else{
            tv_timestamp.setText("");
        }

    }
    public void setIv_icon(int drawable_id){
        iv_icon.setImageDrawable(AppUtils.getApplicationContext().getDrawable(drawable_id));
    }
    public void setTv_unread_count(int count){
        if(count>0){
            tv_unread_count.setVisibility(View.VISIBLE);
            tv_unread_count.setText(count+"");
        }else{
            tv_unread_count.setVisibility(View.GONE);
        }
    }
}

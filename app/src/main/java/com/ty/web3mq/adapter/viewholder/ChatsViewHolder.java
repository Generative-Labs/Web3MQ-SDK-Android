package com.ty.web3mq.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.web3_mq.utils.AppUtils;
import com.ty.web3_mq.utils.CommonUtils;
import com.ty.web3_mq.utils.DateUtils;
import com.ty.web3mq.R;

public class ChatsViewHolder extends RecyclerView.ViewHolder{
    public TextView tv_title, tv_content, tv_timestamp;
    public ImageView iv_icon;
    public ChatsViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_title = itemView.findViewById(R.id.tv_title);
        tv_content = itemView.findViewById(R.id.tv_content);
        tv_timestamp = itemView.findViewById(R.id.tv_timestamp);
        iv_icon = itemView.findViewById(R.id.iv_icon);
    }

    public void setTv_title(String title){
        tv_title.setText(title);
    }
    public void setTv_content(String content){
        tv_content.setText(content);
    }
    public void setTv_timestamp(long timestamp){
        String date = DateUtils.getTimeString(timestamp);
        tv_timestamp.setText(date);
    }
    public void setIv_icon(int drawable_id){
        iv_icon.setImageDrawable(AppUtils.getApplicationContext().getDrawable(drawable_id));
    }
}

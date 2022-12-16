package com.ty.web3mq.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.web3mq.R;

public class NotificationHistoryViewHolder extends RecyclerView.ViewHolder{
    public TextView tv_notification_title,tv_notification_content,tv_notification_time;
    public NotificationHistoryViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_notification_title = itemView.findViewById(R.id.tv_notification_title);
        tv_notification_content = itemView.findViewById(R.id.tv_notification_content);
        tv_notification_time = itemView.findViewById(R.id.tv_notification_time);
    }

    public void setNotificationTitle(String title){
        tv_notification_title.setText(title);
    }

    public void setNotificationContent(String content){
        tv_notification_content.setText(content);
    }

    public void setNotificationTime(String time){
        tv_notification_time.setText(time);
    }
}

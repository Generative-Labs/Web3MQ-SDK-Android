package com.ty.module_notification.adapter.viewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.module_notification.R;
import com.ty.web3_mq.http.beans.NotificationBean;
import com.ty.web3_mq.utils.DateUtils;

public class NotificationHistoryViewHolder extends RecyclerView.ViewHolder{
    public TextView tv_notification_title,tv_notification_content,tv_notification_time;
    public Button btn_follow;
    public NotificationHistoryViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_notification_title = itemView.findViewById(R.id.tv_notification_title);
        tv_notification_content = itemView.findViewById(R.id.tv_notification_content);
        tv_notification_time = itemView.findViewById(R.id.tv_notification_time);
        btn_follow = itemView.findViewById(R.id.btn_follow);
    }

    public void setNotificationBean(NotificationBean notificationBean){
        tv_notification_title.setText(notificationBean.payload.title);
        tv_notification_content.setText(notificationBean.payload.content);
        tv_notification_time.setText(DateUtils.getTimeStringNotification(notificationBean.timestamp));
        switch (notificationBean.payload.type){
            case NotificationBean.TYPE_FRIEND_REQUEST:
                btn_follow.setVisibility(View.VISIBLE);
            case NotificationBean.TYPE_AGREE_FRIEND_REQUEST:
                break;
            default:
                btn_follow.setVisibility(View.GONE);
                break;
        }
    }
}

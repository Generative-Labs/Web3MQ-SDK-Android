package com.ty.web3mq.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.web3_mq.http.beans.NotificationBean;
import com.ty.web3_mq.utils.DateUtils;
import com.ty.web3mq.R;
import com.ty.web3mq.adapter.viewholder.NotificationHistoryViewHolder;

import java.util.ArrayList;

public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private ArrayList<NotificationBean> notificationBeans;
    private OnItemClickListener onItemClickListener;
    public NotificationsAdapter(Context context, ArrayList<NotificationBean> notificationBeans){
        this.context = context;
        this.notificationBeans = notificationBeans;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_history,parent,false);
        NotificationHistoryViewHolder viewHolder = new NotificationHistoryViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationBean notification = notificationBeans.get(position);
        NotificationHistoryViewHolder viewHolder = (NotificationHistoryViewHolder) holder;
        viewHolder.setNotificationTitle(notification.payload.title);
        viewHolder.setNotificationContent(notification.payload.content);
        viewHolder.setNotificationTime(DateUtils.getTimeStringNotification(notification.timestamp));
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    @Override
    public int getItemCount() {
        return notificationBeans.size();
    }
}

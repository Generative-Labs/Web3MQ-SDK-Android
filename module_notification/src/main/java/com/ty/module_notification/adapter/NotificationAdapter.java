package com.ty.module_notification.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.module_notification.R;
import com.ty.module_notification.adapter.viewHolder.NotificationHistoryViewHolder;
import com.ty.web3_mq.http.beans.NotificationBean;
import com.ty.web3_mq.utils.DateUtils;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private ArrayList<NotificationBean> notificationBeans;
    private OnFollowClickListener onItemClickListener;
    public NotificationAdapter(Context context, ArrayList<NotificationBean> notificationBeans){
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
        viewHolder.setNotificationBean(notification);
        viewHolder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener!=null){
                    onItemClickListener.onItemClick(position);
                }
            }
        });
    }

    public void setOnFollowClickListener(OnFollowClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnFollowClickListener{
        void onItemClick(int position);
    }

    @Override
    public int getItemCount() {
        return notificationBeans.size();
    }
}

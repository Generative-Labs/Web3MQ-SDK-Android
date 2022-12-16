package com.ty.web3mq.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.web3mq.R;
import com.ty.web3mq.adapter.viewholder.ChatsViewHolder;
import com.ty.web3mq.adapter.viewholder.MessageViewHolder;
import com.ty.web3mq.bean.ChatItem;
import com.ty.web3mq.bean.MessageItem;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<MessageItem> messageList;
    private OnItemClickListener onItemClickListener;

    public MessagesAdapter(ArrayList<MessageItem> messageList){
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message,parent,false);
        MessageViewHolder viewHolder = new MessageViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageViewHolder viewHolder = (MessageViewHolder) holder;
        MessageItem messageItem = messageList.get(position);
        viewHolder.setTv_userid(messageItem.from);
        viewHolder.setTv_content(messageItem.content);
        viewHolder.setTv_timestamp(messageItem.timestamp);
    }



    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
}

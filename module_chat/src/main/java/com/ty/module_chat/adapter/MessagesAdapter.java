package com.ty.module_chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.module_chat.R;
import com.ty.module_chat.adapter.viewHolder.MessageViewHolder;
import com.ty.module_chat.bean.MessageItem;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<MessageItem> messageList;
    private OnItemClickListener onItemClickListener;
    private static final int VIEW_TYPE_MINE = 0;
    private static final int VIEW_TYPE_OTHER = 1;
    private Context context;

    public MessagesAdapter(ArrayList<MessageItem> messageList, Context context){
        this.messageList = messageList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_OTHER){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_other,parent,false);
            MessageViewHolder viewHolder = new MessageViewHolder(v);
            return viewHolder;
        }else{
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_mine,parent,false);
            MessageViewHolder viewHolder = new MessageViewHolder(v);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageViewHolder viewHolder = (MessageViewHolder) holder;
        MessageItem messageItem = messageList.get(position);
        viewHolder.setTv_userid(messageItem.from);
        viewHolder.setTv_content(messageItem.content);
        viewHolder.setTv_timestamp(messageItem.timestamp);
        viewHolder.setAvatarUrl(messageItem.avatar_url,context);
    }

    @Override
    public int getItemViewType(int position) {
        MessageItem messageItem = messageList.get(position);
        if(messageItem.isMine){
            return VIEW_TYPE_MINE;
        }else {
            return VIEW_TYPE_OTHER;
        }
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

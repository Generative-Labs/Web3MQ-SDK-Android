package com.ty.module_chat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.module_chat.R;
import com.ty.module_chat.adapter.viewHolder.ChatsViewHolder;
import com.ty.module_chat.bean.ChatItem;

import java.util.ArrayList;


public class ChatsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<ChatItem> chatList;
    private OnItemClickListener onItemClickListener;

    public ChatsAdapter(ArrayList<ChatItem> chatList){
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat,parent,false);
        ChatsViewHolder viewHolder = new ChatsViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatsViewHolder viewHolder = (ChatsViewHolder) holder;
        ChatItem chat = chatList.get(position);
        switch (chat.chat_type){
            case ChatItem.CHAT_TYPE_USER:
                viewHolder.setIv_icon(R.mipmap.ic_dm);
                break;
            case ChatItem.CHAT_TYPE_GROUP:
                viewHolder.setIv_icon(R.mipmap.ic_group);
                break;
        }
        viewHolder.setTv_title(chat.title);
        viewHolder.setTv_content(chat.content);
        viewHolder.setTv_timestamp(chat.timestamp);
        viewHolder.setTv_unread_count(chat.unreadCount);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener!=null){
                    onItemClickListener.onItemClick(position);
                }
            }
        });
    }



    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

//    public void updateItem() {
//        notifyItemChanged();
//    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
}

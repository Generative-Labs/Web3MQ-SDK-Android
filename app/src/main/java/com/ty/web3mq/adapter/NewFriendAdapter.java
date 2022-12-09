package com.ty.web3mq.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.web3_mq.http.beans.ContactBean;
import com.ty.web3mq.R;

import java.util.ArrayList;

public class NewFriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private ArrayList<ContactBean> contactList;
    private OnActionClickListener onActionClickListener;
    public NewFriendAdapter(Context context, ArrayList<ContactBean> contactList){
        this.context = context;
        this.contactList = contactList;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_friend,parent,false);
        NewFriendItemViewHolder viewHolder = new NewFriendItemViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindContactItem(holder, position);
    }


    private void bindContactItem(RecyclerView.ViewHolder holder, int position){
        ContactBean contact = contactList.get(position);
        NewFriendItemViewHolder viewHolder = (NewFriendItemViewHolder)holder;
        viewHolder.setTv_userid(contact.userid);
        ((NewFriendItemViewHolder) holder).tv_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onActionClickListener !=null){
                    onActionClickListener.onItemClick(position);
                }
            }
        });
    }

    public void setOnActionClickListener(OnActionClickListener onActionClickListener) {
        this.onActionClickListener = onActionClickListener;
    }

    public interface OnActionClickListener{
        void onItemClick(int position);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }
}

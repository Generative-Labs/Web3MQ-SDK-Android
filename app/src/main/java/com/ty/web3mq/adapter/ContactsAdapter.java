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

public class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private ArrayList<ContactBean> contactList;
    private static final int VIEW_TYPE_NEW_FRIEND = 0;
    private static final int VIEW_TYPE_CONTRACT = 1;
    private OnItemClickListener onItemClickListener;
    public ContactsAdapter(Context context, ArrayList<ContactBean> contactList){
        this.context = context;
        this.contactList = contactList;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_NEW_FRIEND){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_new_friend,parent,false);
            ContactNewFriendViewHolder viewHolder = new ContactNewFriendViewHolder(v);
            return viewHolder;
        }else if(viewType == VIEW_TYPE_CONTRACT){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact,parent,false);
            ContactViewHolder viewHolder = new ContactViewHolder(v);
            return viewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ContactNewFriendViewHolder){
            bindNewFriendItem(holder);
        }else if(holder instanceof ContactViewHolder){
            bindContactItem(holder, position);
        }
    }

    private void bindNewFriendItem(RecyclerView.ViewHolder holder) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener!=null){
                    onItemClickListener.onItemClick(0);
                }
            }
        });
    }

    private void bindContactItem(RecyclerView.ViewHolder holder, int position){
        ContactBean contact = contactList.get(position-1);
        ContactViewHolder viewHolder = (ContactViewHolder)holder;
        viewHolder.setTv_userid(contact.userid);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener!=null){
                    onItemClickListener.onItemClick(position);
                }
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    @Override
    public int getItemCount() {
        if(contactList!=null){
            return contactList.size()+1;
        }else{
            return 1;
        }

    }

    @Override
    public int getItemViewType(int position) {
        if(position==0){
            return VIEW_TYPE_NEW_FRIEND;
        }else {
            return VIEW_TYPE_CONTRACT;
        }
    }
}

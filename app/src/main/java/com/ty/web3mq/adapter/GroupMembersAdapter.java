package com.ty.web3mq.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.web3mq.R;
import com.ty.web3mq.adapter.viewholder.ContactNewFriendViewHolder;
import com.ty.web3mq.adapter.viewholder.GroupMemberViewHolder;

import java.util.ArrayList;

public class GroupMembersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<String> user_ids;
    public GroupMembersAdapter(ArrayList<String> user_ids){
        this.user_ids = user_ids;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_members,parent,false);
        GroupMemberViewHolder viewHolder = new GroupMemberViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String user_id = user_ids.get(position);
        GroupMemberViewHolder viewHolder = (GroupMemberViewHolder) holder;
        viewHolder.setTv_user_id(user_id);
    }

    @Override
    public int getItemCount() {
        return user_ids.size();
    }
}

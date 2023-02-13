package com.ty.module_contact.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.module_contact.R;
import com.ty.module_contact.adapter.viewHolder.FollowersViewHolder;
import com.ty.module_contact.bean.FollowItem;

import java.util.ArrayList;

public class FollowersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<FollowItem> followItems;
    private OnFollowClickListener onFollowClickListener;
    private Context context;

    public FollowersAdapter(ArrayList<FollowItem> followItems,Context context){
        this.followItems = followItems;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_followers,parent,false);
        FollowersViewHolder viewHolder = new FollowersViewHolder(v,context);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FollowersViewHolder viewHolder = (FollowersViewHolder) holder;
        FollowItem item = followItems.get(position);
        viewHolder.setFollower(item);
        viewHolder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onFollowClickListener!=null){
                    onFollowClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return followItems.size();
    }

    public void setOnFollowClickListener(OnFollowClickListener onFollowClickListener) {
        this.onFollowClickListener = onFollowClickListener;
    }

    public interface OnFollowClickListener{
        void onItemClick(int position);
    }
}

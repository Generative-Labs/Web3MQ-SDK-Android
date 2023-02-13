package com.ty.module_chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.module_chat.R;
import com.ty.module_chat.adapter.viewHolder.InviteGroupViewHolder;
import com.ty.web3_mq.http.beans.FollowerBean;

import java.util.ArrayList;

public class InviteGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<FollowerBean> followItems;
    private Context context;
    private ArrayList<FollowerBean> checkedFollower = new ArrayList<>();
    public InviteGroupAdapter(ArrayList<FollowerBean> followItems, Context context){
        this.followItems = followItems;
        this.context = context;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invite_group,parent,false);
        InviteGroupViewHolder viewHolder = new InviteGroupViewHolder(v,context);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        InviteGroupViewHolder viewHolder = (InviteGroupViewHolder) holder;
        FollowerBean bean = followItems.get(position);
        viewHolder.setFollower(bean);
        viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    checkedFollower.add(bean);
                }else{
                    checkedFollower.remove(bean);
                }
            }
        });
    }

    public ArrayList<FollowerBean> getCheckedFollower(){
        return checkedFollower;
    }

    @Override
    public int getItemCount() {
        return followItems.size();
    }

}

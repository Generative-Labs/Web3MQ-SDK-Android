package com.ty.moudle_new_message.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.moudle_new_message.R;
import com.ty.moudle_new_message.adapter.viewHolder.FriendListViewHolder;
import com.ty.web3_mq.http.beans.FollowerBean;

import java.util.ArrayList;

public class FriendListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<FollowerBean> followItems;
    private Context context;
    public static final String STYLE_DEFAULT = "default";
    public static final String STYLE_SEARCH = "search";
    public static final String STYLE_CREATE_ROOM = "create_room";
    private String style = STYLE_DEFAULT;
    private ArrayList<FollowerBean> checkedFollower = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    public FriendListAdapter (ArrayList<FollowerBean> followItems, Context context){
        this.followItems = followItems;
        this.context = context;
    }

    public void setStyle(String style){
        this.style = style;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_list,parent,false);
        FriendListViewHolder viewHolder = new FriendListViewHolder(v,context);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FriendListViewHolder viewHolder = (FriendListViewHolder) holder;
        FollowerBean bean = followItems.get(position);
        viewHolder.setStyle(style);
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
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener!=null){
                    onItemClickListener.onItemClick(position);
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

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
}

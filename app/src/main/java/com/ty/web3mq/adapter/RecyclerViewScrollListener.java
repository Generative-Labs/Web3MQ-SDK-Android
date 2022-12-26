package com.ty.web3mq.adapter;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewScrollListener extends RecyclerView.OnScrollListener implements BottomListener {
    // 最后一个完全可见项的位置
    private int lastCompletelyVisibleItemPosition;

    private int maxLoadCount;

    public RecyclerViewScrollListener(int maxLoadCount){
        this.maxLoadCount = maxLoadCount;
    }

    public void setMaxLoadCount(int maxLoadCount){
        this.maxLoadCount = maxLoadCount;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        lastCompletelyVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        // 通过比对 最后完全可见项位置 和 总条目数，来判断是否滑动到底部
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        if (totalItemCount>=maxLoadCount && newState == RecyclerView.SCROLL_STATE_IDLE) {
            if (visibleItemCount > 0 && lastCompletelyVisibleItemPosition >= totalItemCount - 1) {
                onScrollToBottom();
            }
        }
    }

    @Override
    public void onScrollToBottom() {

    }

}

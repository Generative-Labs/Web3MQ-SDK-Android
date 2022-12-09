package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.TopicBean;

import java.util.ArrayList;

public interface GetMySubscribeTopicCallback {
    void onSuccess(ArrayList<TopicBean> topicList);
    void onFail(String error);
}

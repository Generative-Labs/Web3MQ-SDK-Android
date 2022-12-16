package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.TopicBean;

public interface CreateTopicCallback {
    void onSuccess(TopicBean topicBean);
    void onFail(String error);
}

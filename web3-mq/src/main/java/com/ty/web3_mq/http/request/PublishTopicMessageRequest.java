package com.ty.web3_mq.http.request;

public class PublishTopicMessageRequest extends BaseRequest{
    public String userid;
    public String topicid;
    public String title;
    public String content;
    public long timestamp;
    public String web3mq_signature;
}

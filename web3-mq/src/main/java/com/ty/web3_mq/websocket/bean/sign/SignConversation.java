package com.ty.web3_mq.websocket.bean.sign;

import com.ty.web3_mq.websocket.bean.ErrorResponse;
import com.ty.web3_mq.websocket.bean.SignRequest;
import com.ty.web3_mq.websocket.bean.SignSuccessResponse;

public class SignConversation {
    public String id;
    public SignRequest request;
    public SignSuccessResponse successResponse;
    public ErrorResponse errorResponse;
}

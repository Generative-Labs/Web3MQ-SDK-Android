package com.ty.web3_mq.http;

public interface ApiConfig {
//    https://testnet-us-west-1-1.web3mq.com
//    https://testnet-us-west-1-2.web3mq.com
//    https://testnet-ap-jp-1.web3mq.com
//    https://testnet-ap-jp-2.web3mq.com
//    https://testnet-ap-singapore-1.web3mq.com
//    https://testnet-ap-singapore-2.web3mq.com


    String BASE_URL = "https://dev-ap-jp-1.web3mq.com";
    String USER_LOGIN = BASE_URL + "/api/user_login/";
    String PING = BASE_URL + "/api/ping/";
    String CHANGE_NOTIFICATION_STATUS = BASE_URL + "/api/notification/status/";
    String GET_CHAT_LIST = BASE_URL + "/api/chats/";
    String GET_CONTACT_LIST = BASE_URL+ "/api/contacts/";
    String POST_FRIEND_REQUEST = BASE_URL+"/api/contacts/add_friends/";
    String GET_SENT_FRIEND_REQUEST_LIST = BASE_URL+"/api/contacts/add_friends_list/";
    String HANDLE_FRIEND_REQUEST = BASE_URL + "/api/contacts/friend_requests/";
    String GET_RECEIVE_FRIEND_REQUEST_LIST = BASE_URL+"/api/contacts/friend_requests_list/";
    interface Headers {
        String DATE_TIME = "DateTime";
        String REQUEST_ID = "RequestId";
        String ACCEPT_LANGUAGE = "Accept-Language";
        String SIGN = "Sign";
        String AUTHORIZATION = "Authorization";
        String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
    }
}

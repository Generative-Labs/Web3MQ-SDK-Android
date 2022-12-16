package com.ty.web3_mq.http;

public interface ApiConfig {
//    https://testnet-us-west-1-1.web3mq.com
//    https://testnet-us-west-1-2.web3mq.com
//    https://testnet-ap-jp-1.web3mq.com
//    https://testnet-ap-jp-2.web3mq.com
//    https://testnet-ap-singapore-1.web3mq.com
//    https://testnet-ap-singapore-2.web3mq.com

//    String BASE_URL ="https://testnet-ap-jp-1.web3mq.com";
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
    String GET_MY_PROFILE = BASE_URL+"/api/my_profile/";
    String POST_MY_PROFILE = BASE_URL+"/api/my_profile/";
    String GET_USER_INFO = BASE_URL+"/api/get_user_info/";
    String SEARCH_USERS = BASE_URL+"/api/users/search/";
    String GROUP_CREATE = BASE_URL+"/api/groups/";
    String GROUP_INVITATION = BASE_URL+"/api/group_invitation/";
    String GET_GROUP_LIST = BASE_URL+"/api/groups/";
    String GET_GROUP_MEMBERS = BASE_URL+"/api/group_members/";
    String CHANGE_MESSAGE_STATUS = BASE_URL+"/api/messages/status/";
    String GET_MESSAGE_HISTORY = BASE_URL+"/api/messages/history/";
    String GET_NOTIFICATION_HISTORY = BASE_URL+"/api/notification/history/";
    String CREATE_TOPIC = BASE_URL+"/api/create_topic/";
    String GET_MY_CREATE_TOPIC_LIST = BASE_URL+"/api/my_create_topic_list/";
    String GET_MY_SUBSCRIBE_TOPIC_LIST = BASE_URL+"/api/my_subscribe_topic_list/";
    String PUBLISH_TOPIC_MESSAGE = BASE_URL+"/api/publish_topic_message/";
    String SUBSCRIBE_TOPIC_MESSAGE = BASE_URL+"/api/subscribe_topic/";
    interface Headers {
        String DATE_TIME = "DateTime";
        String REQUEST_ID = "RequestId";
        String ACCEPT_LANGUAGE = "Accept-Language";
        String SIGN = "Sign";
        String AUTHORIZATION = "Authorization";
        String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
    }
}

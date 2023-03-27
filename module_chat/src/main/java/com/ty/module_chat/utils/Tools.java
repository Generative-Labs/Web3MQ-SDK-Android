package com.ty.module_chat.utils;

import com.google.gson.reflect.TypeToken;
import com.ty.module_chat.bean.ChatItem;
import com.ty.module_chat.bean.MessageItem;
import com.ty.web3_mq.http.beans.MessageBean;
import com.ty.web3_mq.utils.DefaultSPHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Tools {
//    public static void saveMessageItemList(String chatId, ArrayList<MessageItem> messageItems){
//        DefaultSPHelper.getInstance().put(chatId,messageItems);
//    }
//
//    public static ArrayList<MessageItem> getMessageItemList(String chatId){
//        Type listType = new TypeToken<ArrayList<MessageItem>>(){}.getType();
//        ArrayList<MessageItem> itemList = (ArrayList<MessageItem>) DefaultSPHelper.getInstance().getObject(chatId,listType);
//        return itemList;
//    }

    public static void saveChatItemList(ArrayList<ChatItem> chatItems){
        DefaultSPHelper.getInstance().put("ChatItem",chatItems);
    }

    public static ArrayList<ChatItem> getChatItemList(){
        Type listType = new TypeToken<ArrayList<ChatItem>>(){}.getType();
        ArrayList<ChatItem> itemList = (ArrayList<ChatItem>) DefaultSPHelper.getInstance().getObject("ChatItem",listType);
        return itemList;
    }

    // call in message activity
    public static void updateChatItem(String chatId,String content, long timestamp, int unReadCount){
        ArrayList<ChatItem> chatItems = getChatItemList();
        if(chatItems==null || chatItems.size()==0){
            return;
        }
        for(int i=0; i<chatItems.size(); i++){
            ChatItem item = chatItems.get(i);
            if(item.chatid.equals(chatId)){
                item.content = content;
                item.unreadCount = unReadCount;
                item.timestamp = timestamp;
            }
        }
        saveChatItemList(chatItems);
    }
}

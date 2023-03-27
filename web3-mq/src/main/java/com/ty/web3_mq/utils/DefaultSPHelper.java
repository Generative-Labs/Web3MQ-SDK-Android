package com.ty.web3_mq.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ty.web3_mq.http.beans.MessageBean;
import com.ty.web3_mq.http.beans.MessagesBean;
import com.ty.web3_mq.websocket.bean.ErrorResponse;
import com.ty.web3_mq.websocket.bean.SignRequest;
import com.ty.web3_mq.websocket.bean.SignResponseSuccessData;
import com.ty.web3_mq.websocket.bean.SignSuccessResponse;
import com.ty.web3_mq.websocket.bean.sign.SignConversation;
import com.ty.web3_mq.websocket.bean.sign.Web3MQSession;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import web3mq.Message;


public class DefaultSPHelper {
    private static final String TAG = "DefaultSPHelper";
    private static final String PREFERENCE_NAME = "web3_mq";
    private static volatile DefaultSPHelper instance;
    private SharedPreferences mPreferences;
    private static Gson gson;

    private DefaultSPHelper() {
        mPreferences = AppUtils.getApplicationContext().getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
    }

    public static DefaultSPHelper getInstance() {
        if (instance == null) {
            synchronized (DefaultSPHelper.class) {
                if (instance == null) {
                    instance = new DefaultSPHelper();
                    gson = new GsonBuilder()
                            .setPrettyPrinting()
                            .disableHtmlEscaping()
                            .create();
                }
            }
        }
        return instance;
    }

    public void saveNodeID(String node_id) {
        put(Constant.SP_NODE_ID,node_id);
    }

    public String getNodeID(){
        return getString(Constant.SP_NODE_ID,null);
    }

    public void saveMainPrivate(String prv){
        put(Constant.SP_ED25519_MAIN_PRV,prv);
    }

    public void saveMainPublic(String pub){
        put(Constant.SP_ED25519_MAIN_PUB,pub);
    }

    public void saveTempPrivate(String prv){
        put(Constant.SP_ED25519_TEMP_PRV,prv);
    }

    public void saveTempPublic(String pub){
        put(Constant.SP_ED25519_TEMP_PUB,pub);
    }

    public void saveUserID(String userID){
        put(Constant.SP_USER_ID, userID);
    }

    public void saveDidKey(String didKey){
        put(Constant.SP_DID_KEY, didKey);
    }

    public String getMainPrivate(){
        return getString(Constant.SP_ED25519_MAIN_PRV);
    }

    public String getMainPublic(){
        return getString(Constant.SP_ED25519_MAIN_PUB);
    }

    public String getTempPrivate(){
        return getString(Constant.SP_ED25519_TEMP_PRV);
    }

    public String getTempPublic(){
        return getString(Constant.SP_ED25519_TEMP_PUB);
    }

    public String getUserID(){
        return getString(Constant.SP_USER_ID);
    }

    public String getDidKey(){
        return getString(Constant.SP_DID_KEY);
    }


//    public MessagesBean getMessages(String chatId){
//        return (MessagesBean) getObject(chatId,MessagesBean.class);
//    }

    public Object getObject(String key,Type type){
        String json_str = getString(key);
        if(json_str!=null){
            return gson.fromJson(json_str,type);
        }else{
            return null;
        }
    }

    /**
     * 将String信息存入Preferences
     */
    public boolean put(String key, String value) {
        // 存入数据
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key, value);
        return editor.commit();
    }



    /**
     * 获取SharePreference中的String类型值
     */
    public String getString(String key) {
        // 获取数据
        return mPreferences.getString(key, "");
    }

    /**
     * 获取SharePreference中的String类型值
     */
    public String getString(String key, String defValue) {
        // 获取数据
        return mPreferences.getString(key, defValue);
    }

    /**
     * 将string的信息加密存入mPreferences
     */
    public boolean putByAES(String key, String value) {
        // 存入数据
        SharedPreferences.Editor editor = mPreferences.edit();
        String decrypt = AESUtils.encrypt(value, key);
        editor.putString(key, decrypt);
        return editor.commit();
    }


    /**
     * 取出string通过解密的方式
     */
    public String getStringByAES(String key) {
        // 获取数据
        String mPreferencesString = mPreferences.getString(key, "");
        return AESUtils.decrypt(mPreferencesString, key);
    }

    /**
     * 将boolean信息存入Preferences
     */
    public boolean put(String key, boolean value) {
        // 存入数据
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    /**
     * 获取SharePreference中的值
     */
    public boolean getBoolean(String key, boolean defValue) {
        // 获取数据
        return mPreferences.getBoolean(key, defValue);
    }


    /**
     * 将int信息存入Preferences
     */
    public boolean put(String key, int value) {
        // 存入数据
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public boolean put(String key, Object value) {
        // 存入数据
        String json = gson.toJson(value);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key,json);
        return editor.commit();
    }

    /**
     * 获取SharePreference中的int类型值
     */
    public int getInt(String key, int defValue) {
        // 获取数据
        return mPreferences.getInt(key, defValue);
    }

    public boolean put(String key, long value) {
        // 存入数据
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(key, value);
        return editor.commit();
    }

    /**
     * 获取SharePreference中的int类型值
     */
    public Long getLong(String key, Long defValue) {
        // 获取数据
        return mPreferences.getLong(key, defValue);
    }

    /**
     * 将float信息存入Preferences
     */
    public boolean put(String key, float value) {
        // 存入数据
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putFloat(key, value);
        return editor.commit();
    }

    /**
     * 获取SharePreference中的值
     */
    public float getFloat(String key, float defValue) {
        // 获取数据
        return mPreferences.getFloat(key, defValue);
    }

    public boolean put(String key, ArrayList<String> value){
        SharedPreferences.Editor editor = mPreferences.edit();
        String json = gson.toJson(value);
        editor.putString(key, json);
        return editor.commit();
    }




    /**
     * delete in preferences value
     */
    public boolean remove(String key) {
        // 存入数据
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(key);
        return editor.commit();
    }

//    private void saveSession(String sessionID, Web3MQSession session) {
//        String json = gson.toJson(session);
//        put("session:"+sessionID,json);
//        Log.i(TAG,"saveSession session:"+sessionID+"   "+json);
//    }

    public void appendSession(Web3MQSession session){
        ArrayList<Web3MQSession> sessionList = getSessionList();
        if(sessionList == null){
            sessionList = new ArrayList<>();
        }
        sessionList.add(session);
        saveSessionList(sessionList);
    }

    public void removeSession(String sessionID){
        ArrayList<Web3MQSession> sessionList = getSessionList();
        if(sessionList == null){
            return;
        }
        for(int i=0;i<sessionList.size();i++){
            Web3MQSession session = sessionList.get(i);
            if(session.peerTopic.equals(sessionID)){
                sessionList.remove(i);
                if(sessionList.size()>0){
                    saveSessionList(sessionList);
                }
                break;
            }
        }
    }

    public Web3MQSession getSession(String sessionID) {
        ArrayList<Web3MQSession> sessionList = getSessionList();
        if(sessionList==null){
            return null;
        }
        for(Web3MQSession session:sessionList){
            if(session.peerTopic.equals(sessionID)){
                return session;
            }
        }
        return null;
    }

    public void updateSession(String sessionID,Web3MQSession newSession) {
//        Log.i(TAG,"updateSession sessionID:"+sessionID+" newSession:"+gson.toJson(newSession));
        ArrayList<Web3MQSession> sessionList = getSessionList();
        if(sessionList==null){
            return;
        }
        for(int i=0;i<sessionList.size();i++){
            Web3MQSession session = sessionList.get(i);
            if(session.peerTopic.equals(sessionID)){
                sessionList.set(i,newSession);
                break;
            }
        }
        saveSessionList(sessionList);
    }

    private void saveSessionList(ArrayList<Web3MQSession> sessionList){
        put("sessionList",gson.toJson(sessionList));
    }

    public ArrayList<Web3MQSession> getSessionList(){
        String json = mPreferences.getString("sessionList", null);
        if(json!=null){
            Type type = new TypeToken<ArrayList<Web3MQSession>>() {}.getType();
            return gson.fromJson(json, type);
        }else{
            return null;
        }
    }

    public void appendSignRequest(String sessionID, String id, SignRequest signRequest){
//        Log.i(TAG,"appendSignRequest sessionID:"+sessionID+" id:"+id);
        Web3MQSession web3MQSession = getSession(sessionID);
        if(web3MQSession.signConversationMap == null) {
            web3MQSession.signConversationMap = new HashMap<>();
        }
        SignConversation conversation = new SignConversation();
        conversation.id = id;
        conversation.request = signRequest;
        web3MQSession.signConversationMap.put(id,conversation);
        updateSession(sessionID,web3MQSession);
    }


//    public ArrayList<String> getRequestList(String sessionID) {
//        String json = mPreferences.getString("request:"+sessionID, null);
//        if(json!=null){
//            Type type = new TypeToken<ArrayList<String>>() {}.getType();
//            return gson.fromJson(json, type);
//        }else{
//            return null;
//        }
//    }

    public void appendSignSuccessResponse(String sessionID, String id, SignSuccessResponse successResponse){
        Web3MQSession web3MQSession = getSession(sessionID);
        if(web3MQSession.signConversationMap == null) {
            return;
        }
        SignConversation conversation = web3MQSession.signConversationMap.get(id);
        if(conversation==null){
            return;
        }
        conversation.successResponse = successResponse;
        web3MQSession.signConversationMap.put(id,conversation);
        updateSession(sessionID,web3MQSession);
    }

    public void appendSignErrorResponse(String sessionID, String id, ErrorResponse errorResponse){
        Web3MQSession web3MQSession = getSession(sessionID);
        if(web3MQSession.signConversationMap == null) {
            return;
        }
        SignConversation conversation = web3MQSession.signConversationMap.get(id);
        if(conversation==null){
            return;
        }
        conversation.errorResponse = errorResponse;
        web3MQSession.signConversationMap.put(id,conversation);
        updateSession(sessionID,web3MQSession);
    }

//    public ArrayList<String> getResponseList(String sessionID){
//        String json = mPreferences.getString("response:"+sessionID, null);
//        if(json!=null){
//            Type type = new TypeToken<ArrayList<String>>() {}.getType();
//            return gson.fromJson(json, type);
//        }else{
//            return null;
//        }
//    }

    public void showSessionInfo(){
        Log.i(TAG,"----------showSessionInfo--------");
        ArrayList<Web3MQSession> sessionList= getSessionList();
        if(sessionList==null){
            Log.i(TAG,"session list is null");
        }else{
            for(Web3MQSession session:sessionList){
                Log.i(TAG,"session : "+gson.toJson(session)+"");
            }
        }
    }

    /**
     * Mark in the editor to remove all data from the preferences.
     */
    public boolean clear() {
        // 存入数据
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.clear();
        return editor.commit();
    }



}

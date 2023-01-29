package com.ty.web3_mq.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ty.web3_mq.http.beans.MessageBean;
import com.ty.web3_mq.http.beans.MessagesBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;


public class DefaultSPHelper {

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
                            .disableHtmlEscaping()
                            .create();
                }
            }
        }
        return instance;
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

    public void saveMessage(String chatId, MessagesBean message){
        put(chatId,message);
    }


    public MessagesBean getMessages(String chatId){
        return (MessagesBean) getObject(chatId,MessagesBean.class);
    }

    public Object getObject(String key,Class cls){
        return gson.fromJson(getString(key),cls);
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

    /**
     * delete in preferences value
     */
    public boolean remove(String key) {
        // 存入数据
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(key);
        return editor.commit();
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

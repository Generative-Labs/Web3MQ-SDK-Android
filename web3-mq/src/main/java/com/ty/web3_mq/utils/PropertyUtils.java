package com.ty.web3_mq.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.ty.web3_mq.websocket.bean.BridgeMessageContent;
import com.ty.web3_mq.websocket.bean.ConnectRequest;
import com.ty.web3_mq.websocket.bean.ConnectSuccessResponse;
import com.ty.web3_mq.websocket.bean.ErrorResponse;
import com.ty.web3_mq.websocket.bean.SignRequest;
import com.ty.web3_mq.websocket.bean.SignSuccessResponse;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PropertyUtils {
    public static BridgeMessageContent convertJsonToBridgeMessageContent(String json_content, Gson gson) {
        BridgeMessageContent bridgeMessageContent = new BridgeMessageContent();
        JsonParser jsonParser = new JsonParser();
        JsonElement element = jsonParser.parse(json_content);
        if(element.getAsJsonObject().has("params")){
            if(element.getAsJsonObject().get("method").getAsString().equals("provider_authorization")){
                // connect request
                bridgeMessageContent.type = BridgeMessageContent.TYPE_CONNECT_REQUEST;
                bridgeMessageContent.content = gson.fromJson(json_content, ConnectRequest.class);
            }else if(element.getAsJsonObject().get("method").getAsString().equals("personal_sign")){
                // sign request
                bridgeMessageContent.type = BridgeMessageContent.TYPE_SIGN_REQUEST;
                bridgeMessageContent.content = gson.fromJson(json_content, SignRequest.class);
            }
        }else{
            //response
            if(element.getAsJsonObject().has("result")){
                //success response
                if(element.getAsJsonObject().get("method").getAsString().equals("provider_authorization")){
                    //connect response
                    bridgeMessageContent.type = BridgeMessageContent.TYPE_CONNECT_SUCCESS_RESPONSE;
                    bridgeMessageContent.content = gson.fromJson(json_content, ConnectSuccessResponse.class);
                }else if(element.getAsJsonObject().get("method").getAsString().equals("personal_sign")){
                    //sign response
                    bridgeMessageContent.type = BridgeMessageContent.TYPE_SIGN_SUCCESS_RESPONSE;
                    bridgeMessageContent.content = gson.fromJson(json_content, SignSuccessResponse.class);
                }
            }else if(element.getAsJsonObject().has("error")){
                //error response
                if(element.getAsJsonObject().get("method").getAsString().equals("provider_authorization")){
                    bridgeMessageContent.type = BridgeMessageContent.TYPE_CONNECT_ERROR_RESPONSE;
                }else if(element.getAsJsonObject().get("method").getAsString().equals("personal_sign")){
                    bridgeMessageContent.type = BridgeMessageContent.TYPE_SIGN_ERROR_RESPONSE;
                }
                bridgeMessageContent.content = gson.fromJson(json_content, ErrorResponse.class);
            }
        }
        return bridgeMessageContent;
    }


    public static String toBracketedFormURLEncoded(Object obj) throws UnsupportedEncodingException {
        Map<String, String> formData = new LinkedHashMap<>();
        toBracketedFormURLEncodedHelper(obj, "", formData);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(URLEncoder.encode(entry.getKey(),"UTF-8"));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getKey(),"UTF-8"));
        }
        return sb.toString();
    }

    private static void toBracketedFormURLEncodedHelper(Object obj, String prefix, Map<String, String> formData) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            String name = field.getName();
            Object value;
            try {
                value = field.get(obj);
            } catch (IllegalAccessException e) {
                value = null;
            }
            if (value != null) {
                if (isPrimitive(value.getClass())) {
                    formData.put(prefix + name, value.toString());
                } else if (value instanceof Object) {
                    toBracketedFormURLEncodedHelper(value, prefix + name + "[", formData);
                }
            }
        }
    }

    private static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == Boolean.class ||
                clazz == Character.class ||
                clazz == Byte.class ||
                clazz == Short.class ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Float.class ||
                clazz == Double.class ||
                clazz == String.class ||
                clazz == Object.class;
    }

    public static <T> T fromFormURLEncoded(String encoded, Class<T> clazz) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        for (String pair : encoded.split("&")) {
            String[] parts = pair.split("=");
            String name = URLDecoder.decode(parts[0], "UTF-8");
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], "UTF-8") : "";
            addProperty(map, name, value);
        }
        return fromMap(map, clazz);
    }

    private static void addProperty(Map<String, Object> map, String name, String value) {
        int openBracket = name.indexOf('[');
        int closeBracket = name.indexOf(']');
        if (openBracket != -1 && closeBracket != -1 && openBracket < closeBracket) {
            String subkey = name.substring(0, openBracket);
            String subprop = name.substring(openBracket + 1, closeBracket);
            Map<String, Object> submap = (Map<String, Object>) map.get(subkey);
            if (submap == null) {
                submap = new HashMap<String, Object>();
                map.put(subkey, submap);
            }
            addProperty(submap, subprop, value);
        } else {
            map.put(name, value);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T fromMap(Map<String, Object> map, Class<T> clazz) throws Exception {
        T obj = clazz.getDeclaredConstructor().newInstance();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object fieldValue = fromMap((Map<String, Object>) value, fieldType);
                field.set(obj, fieldValue);
            } else {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object convertedValue = convertValue(value, fieldType);
                field.set(obj, convertedValue);
            }
        }
        return obj;
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (targetType == String.class) {
            return value.toString();
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value.toString());
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value.toString());
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value.toString());
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value.toString());
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value.toString());
        } else {
            throw new IllegalArgumentException("Unsupported target type: " + targetType);
        }
    }

    public static Object getProperty(Object obj, String propertyName) {
        try {
            // 如果属性名称中包含 "."，则说明是嵌套属性，需要递归获取
            if (propertyName.contains(".")) {
                String[] propertyNames = propertyName.split("\\.", 2);
                Object propertyValue = getProperty(obj, propertyNames[0]);
                if (propertyValue == null) {
                    return null;
                }
                return getProperty(propertyValue, propertyNames[1]);
            }

            // 如果属性名称中包含 "[]"，则说明是集合属性，需要按索引获取
            if (propertyName.endsWith("]")) {
                int index = propertyName.indexOf("[");
                String collectionName = propertyName.substring(0, index);
                String indexString = propertyName.substring(index + 1, propertyName.length() - 1);
                Object collection = getProperty(obj, collectionName);
                if (collection instanceof Collection) {
                    int i = Integer.parseInt(indexString);
                    Collection<?> c = (Collection<?>) collection;
                    if (i >= 0 && i < c.size()) {
                        Object[] array = c.toArray();
                        return array[i];
                    }
                }
                return null;
            }

            Field field = getField(obj.getClass(), propertyName);
            if (field == null) {
                return null;
            }

            Method getter = getGetter(obj.getClass(), field);
            if (getter != null) {
                return getter.invoke(obj);
            }

            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setProperty(Object obj, String propertyName, Object value) {
        try {
            // 如果属性名称中包含 "."，则说明是嵌套属性，需要递归设置
            if (propertyName.contains(".")) {
                String[] propertyNames = propertyName.split("\\.", 2);
                Object propertyValue = getProperty(obj, propertyNames[0]);
                if (propertyValue == null) {
                    propertyValue = createObject(getField(obj.getClass(), propertyNames[0]).getType());
                    setProperty(obj, propertyNames[0], propertyValue);
                }
                setProperty(propertyValue, propertyNames[1], value);
                return;
            }

            // 如果属性名称中包含 "[]"，则说明是集合属性，需要按索引设置
            // TODO
//            if (propertyName.endsWith("]")) {
//                int index = propertyName.indexOf("[");
//                String collectionName = propertyName.substring(0, index);
//                String indexString = propertyName.substring(index + 1, propertyName.length() - 1);
//                Object collection = getProperty(obj, collectionName);
//                if (collection instanceof Collection) {
//                    int i = Integer.parseInt(indexString);
//                    Collection<Object> c = (Collection<Object>) collection;
//                    while (c.size() <= i) {
//                        c.add(null);
//                    }
//                    c.remove(i);
//                    c.add(i, value);
//                    return;
//                }
//                return;
//            }

            Field field = getField(obj.getClass(), propertyName);
            if (field == null) {
                return;
            }

            Method setter = getSetter(obj.getClass(), field);
            if (setter != null) {
                setter.invoke(obj, value);
            } else {
                field.setAccessible(true);
                field.set(obj, value);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static Field getField(Class<?> clazz, String propertyName) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(propertyName);
        } catch (NoSuchFieldException e) {
            // do nothing
        }
        if (field == null && clazz.getSuperclass() != null) {
            return getField(clazz.getSuperclass(), propertyName);
        }
        return field;
    }

    private static Method getGetter(Class<?> clazz, Field field) {
        try {
            String fieldName = field.getName();
            String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            return clazz.getMethod(getterName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Method getSetter(Class<?> clazz, Field field) {
        try {
            String fieldName = field.getName();
            String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            return clazz.getMethod(setterName, field.getType());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Object createObject(Class<?> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

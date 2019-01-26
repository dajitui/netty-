package com.example.demo.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.util.Strings;

public class Mage {

    private static ObjectMapper gson = new ObjectMapper();


    public static ObjectMapper getGson() {
        return gson;
    }

    public static void setGson(ObjectMapper gson) {
        Mage.gson = gson;
    }

    /**
     * 消息发送者
     */
    private String name;

    /**
     * 直播房间号
     */
    private String homeid;

    /**
     * 发送的消息
     */
    private String msg;

    /**
     * 是否是第一次发送，是则为第一次连入直播间
     */
    private boolean count;

    public boolean isCount() {
        return count;
    }

    public void setCount(boolean count) {
        this.count = count;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHomeid() {
        return homeid;
    }

    public void setHomeid(String homeid) {
        this.homeid = homeid;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 将json字符串转成Mage
     *
     * @param message
     * @return
     * @throws Exception
     */
    public Mage strChangeToMage(String message) throws Exception {
        // 转换为格式化的json
        gson.enable(SerializationFeature.INDENT_OUTPUT);

        // 如果json中有新增的字段并且是实体类类中不存在的，不报错
        gson.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return Strings.isEmpty(message) ? null : gson.readValue(message, Mage.class);
    }

    @Override
    public String toString() {
        return "Mage{" +
                "name='" + name + '\'' +
                ", homeid='" + homeid + '\'' +
                ", msg='" + msg + '\'' +
                ", count=" + count +
                '}';
    }
}

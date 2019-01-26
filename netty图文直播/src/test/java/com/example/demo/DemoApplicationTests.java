package com.example.demo;

import com.example.demo.util.Mage;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @Test
    public void contextLoads() {
        String str = "{\"name\":\"123\",\"homeid\":\"1\",\"count\":\"true\"}";

        Mage stu = new Mage();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // 转换为格式化的json
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            // 如果json中有新增的字段并且是实体类类中不存在的，不报错
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            stu = objectMapper.readValue(str, Mage.class);
            System.out.println(stu.toString());
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void a(){
        ConcurrentHashMap<String,ConcurrentHashMap> concurrentHashMap=new ConcurrentHashMap();
        ConcurrentHashMap concurrentHashMap1=new ConcurrentHashMap();
        concurrentHashMap1.put("wqe","qwe");
        concurrentHashMap.put("1",concurrentHashMap1);
        concurrentHashMap1=new ConcurrentHashMap();
        concurrentHashMap1.put("gth","asdf");
        concurrentHashMap.put("1",concurrentHashMap1);
        for(String s:concurrentHashMap.keySet()){
            System.out.println(concurrentHashMap.get(s));
        }
    }
}


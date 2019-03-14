package com.alibaba.csp.sentinel.adapter.dubbo.extend;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

public class ExtendTest {

    /**
     * 方法返回值信息
     */
    @Test
    public void testGetMethod(){
        try{
            String interfaceName = "com.alibaba.csp.sentinel.adapter.dubbo.DemoService";
            String method = "sayHello";
            Class cls = Class.forName(interfaceName);

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }


    @Test
    public void helloJsonSer(){
        String aaa = "hello";
        System.out.println(JSON.toJSONString(aaa));

        int a = 10;
        System.out.println(JSON.toJSONString(a));
    }

    @Test
    public void helloJsonDeser(){
        String aaa = "\"hello\"";
        String bbb = JSON.parseObject(aaa,String.class);
        System.out.println(bbb);

        String ccc = "10";
        Integer ddd = JSON.parseObject(ccc,Integer.class);
        System.out.println(ddd);
    }


}

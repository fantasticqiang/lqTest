package com.lqq.spring4.chapter7.aop;

import java.lang.reflect.Proxy;

/**
 * Created by sunbaiqiang on 2017/12/13.
 */
public class PerformanceHandlerTest {



    public static void proxy(){
        ForumService target = new ForumServiceImpl();
        PerformanceHandler handler = new PerformanceHandler(target);
        ForumService proxy = (ForumService)Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                handler);
        proxy.removeForum(10);
        proxy.removeTopic(100);
    }

    public static void main(String[] args){
        proxy();
    }


}

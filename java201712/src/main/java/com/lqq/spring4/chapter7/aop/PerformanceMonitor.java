package com.lqq.spring4.chapter7.aop;

import com.sun.org.apache.xpath.internal.SourceTree;

/**
 * Created by sunbaiqiang on 2017/12/13.
 */
public class PerformanceMonitor {

    public static long start;
    public static void begin(String className,String methodName){
        start = System.currentTimeMillis();
        System.out.println("监控开始：className:"+className+" methodName:"+methodName+"时间："+start);
    }

    public static void end(){
        System.out.println("监控结束,耗时："+(System.currentTimeMillis()-start));
    }
}

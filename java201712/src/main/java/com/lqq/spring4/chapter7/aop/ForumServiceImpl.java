package com.lqq.spring4.chapter7.aop;

/**
 * Created by sunbaiqiang on 2017/12/13.
 */
public class ForumServiceImpl implements ForumService {
    @Override
    public void removeTopic(int tipicId) {
        System.out.println("模拟删除topic："+tipicId);
        try {
            Thread.currentThread().sleep(80);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void removeForum(int forumId) {
        System.out.println("模拟删除forumId："+forumId);
        try {
            Thread.currentThread().sleep(100);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

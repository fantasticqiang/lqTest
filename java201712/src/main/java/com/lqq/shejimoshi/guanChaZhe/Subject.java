package com.lqq.shejimoshi.guanChaZhe;

import java.util.Observable;

/**
 * Created by sunbaiqiang on 2017/12/1.
 */
public class Subject extends Observable {
    private int data = 0;
    public int getData(){
        return data;
    }
    public void setData(int i){
        if(this.data != i){
            this.data = i;
            setChanged();
        }
        notifyObservers();
    }
}

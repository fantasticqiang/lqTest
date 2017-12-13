package com.lqq.shejimoshi.guanChaZhe;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by sunbaiqiang on 2017/12/1.
 */
public class BObserver implements Observer{

    public BObserver(Subject s) {
        s.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("B Observer receive"+ ((Subject)arg).getData());
    }
}

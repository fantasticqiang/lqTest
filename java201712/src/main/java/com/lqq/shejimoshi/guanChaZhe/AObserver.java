package com.lqq.shejimoshi.guanChaZhe;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by sunbaiqiang on 2017/12/1.
 */
public class AObserver implements Observer {
    public AObserver(Subject s) {
        s.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("A Observer receive " + ((Subject)arg).getData());
    }
}

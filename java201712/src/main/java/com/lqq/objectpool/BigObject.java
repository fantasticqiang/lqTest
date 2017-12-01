package com.lqq.objectpool;

import org.apache.commons.pool2.PooledObject;

/**
 * Created by sunbaiqiang on 2017/12/1.
 */
public class BigObject {
    private boolean active;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public BigObject() {
        active = true;
        System.out.println("我是大对象");
    }

    @Override
    public String toString() {
        return "BigObject{" +
                "active=" + active +
                '}'+this.hashCode();
    }

}

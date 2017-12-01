package com.lqq.objectpool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by sunbaiqiang on 2017/12/1.
 */
public class TestFactory extends BasePooledObjectFactory<BigObject> {


    public BigObject create() throws Exception {
        return new BigObject();
    }

    public PooledObject<BigObject> wrap(BigObject bigObject) {
        return new DefaultPooledObject<BigObject>(bigObject);
    }

    static GenericObjectPool<BigObject> pool = null;

    public synchronized static GenericObjectPool<BigObject> getInstance(){
        if(pool == null){
            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMaxIdle(-1);
            poolConfig.setMaxTotal(-1);
            poolConfig.setMinIdle(100);
            poolConfig.setLifo(false);
            pool = new GenericObjectPool<BigObject>(new TestFactory(), poolConfig);
        }
        return pool;
    }

    public static BigObject borrowObject() throws Exception{
        return (BigObject)TestFactory.getInstance().borrowObject();
    }
    public static void returnObject(BigObject bigObject) throws Exception{
        TestFactory.getInstance().returnObject(bigObject);
    }
    public static void close() throws Exception{
        TestFactory.getInstance().close();
    }
    public static void clear() throws Exception{
        TestFactory.getInstance().clear();
    }

    public static void main(String[] args) throws Exception{
        BigObject bigObject = TestFactory.borrowObject();
        System.out.println(bigObject);
        TestFactory.returnObject(bigObject);
    }
}

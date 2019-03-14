package com.example.mygoappapis.dao;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;

public class ConnectionManagerDriver {

    private static String url = "jdbc:mysql://185.229.113.4:3306/u484401486_godb";
    private static String driverName = "com.mysql.jdbc.Driver";
    private static String username = "u484401486_user";
    private static String password = "xFVtX1yIUhQp";

    private GenericObjectPool connectionPool = null;
    public DataSource setUp() throws Exception
    {
        Class.forName( ConnectionManagerDriver.driverName).newInstance();
        connectionPool = new GenericObjectPool();
        connectionPool.setMaxActive(-1);
        connectionPool.setMaxIdle(-1);
        System.out.println("driver database connected");
        ConnectionFactory cf = new DriverManagerConnectionFactory( ConnectionManagerDriver.url, ConnectionManagerDriver.username, ConnectionManagerDriver.password);
        PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, connectionPool, null, null, false, true);
        return new PoolingDataSource(connectionPool);
    }

    public GenericObjectPool getConnectionPool()
    {
        return connectionPool;
    }
}

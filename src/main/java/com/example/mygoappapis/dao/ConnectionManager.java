package com.example.mygoappapis.dao;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;

public class ConnectionManager {

    private static String url = "jdbc:mysql://localhost/MyGoDriver?useSSL=false&allowMultiQueries=true&rewriteBatchedStatements=true&autoReconnect=true&useUnicode=yes&characterEncoding=UTF-8";
    private static String driverName = "com.mysql.jdbc.Driver";
    private static String username = "mygo";
    private static String password = "MyGo@2019";

    private GenericObjectPool connectionPool = null;
    public DataSource setUp() throws Exception
    {
        Class.forName( ConnectionManager.driverName).newInstance();
        connectionPool = new GenericObjectPool();
        connectionPool.setMaxActive(-1);
        connectionPool.setMaxIdle(-1);
        ConnectionFactory cf = new DriverManagerConnectionFactory( ConnectionManager.url, ConnectionManager.username, ConnectionManager.password);
        PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, connectionPool, null, null, false, true);
        return new PoolingDataSource(connectionPool);
    }

    public GenericObjectPool getConnectionPool()
    {
        return connectionPool;
    }
}

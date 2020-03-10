package com.hs.personnel.dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class BaseDao {
    public static Connection conn;

    public BaseDao() {
        if(conn == null) {
            try {
                String url = "jdbc:derby://localhost:1527/HSDB";
                //String url = "jdbc:derby:/Users/vincenttuan/Documents/Derby/databases/HSDB";
                String user = "app";
                String password = "app";
                conn = DriverManager.getConnection(url, user, password);
                System.out.println(!conn.isClosed());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

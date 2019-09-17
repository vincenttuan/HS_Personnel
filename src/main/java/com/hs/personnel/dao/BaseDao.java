package com.hs.personnel.dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class BaseDao {
    public static Connection conn;

    public BaseDao() {
        if(conn == null) {
            try {
                String url = "jdbc:derby://localhost:1527/HS";
                String user = "app";
                String password = "1234";
                conn = DriverManager.getConnection(url, user, password);
                System.out.println(!conn.isClosed());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

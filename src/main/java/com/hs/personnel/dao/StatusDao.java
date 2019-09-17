package com.hs.personnel.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatusDao extends BaseDao {
    
    public List<Map<String, String>> query() {
        List<Map<String, String>> list = new ArrayList<>();
        try {
            String sql = "Select status_id, status_no, status_name, status_begin, status_end From Status";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, String> map = new LinkedHashMap<>();
                map.put("status_id", rs.getString("status_id"));
                map.put("status_no", rs.getString("status_no"));
                map.put("status_name", rs.getString("status_name"));
                map.put("status_begin", rs.getString("status_begin"));
                map.put("status_end", rs.getString("status_end"));
                list.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
}

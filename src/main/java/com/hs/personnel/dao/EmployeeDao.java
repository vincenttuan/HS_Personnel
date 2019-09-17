package com.hs.personnel.dao;

import static com.hs.personnel.dao.BaseDao.conn;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class EmployeeDao extends BaseDao {
    
    // 取得 emp_no
    public String getEmpNo(String emp_rfid) {
        try {
            String sql = "Select emp_no From employee Where emp_rfid = ? and emp_active = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, emp_rfid);
            pstmt.setBoolean(2, true);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("emp_no");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    // 取得所有 emp
    public List<Map<String, Object>> queryAll() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            String sql = "Select emp_id, emp_no, emp_name, emp_active, emp_rfid, emp_ct From employee";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            while(rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("emp_id", rs.getInt("emp_id"));
                map.put("emp_no", rs.getString("emp_no"));
                map.put("emp_name", rs.getString("emp_name"));
                map.put("emp_active", rs.getBoolean("emp_active"));
                map.put("emp_rfid", rs.getString("emp_rfid"));
                map.put("emp_ct", rs.getDate("emp_ct"));
                list.add(map);
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // 新增 emp
    public void add(String emp_no, String emp_name, boolean emp_active, String emp_rfid) {
        try {
            String sql = "Insert into Employee(emp_no, emp_name, emp_active, emp_rfid) Values(?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, emp_no);
            pstmt.setString(2, emp_name);
            pstmt.setBoolean(3, emp_active);
            pstmt.setString(4, emp_rfid);
            pstmt.executeUpdate(); // 執行更新
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

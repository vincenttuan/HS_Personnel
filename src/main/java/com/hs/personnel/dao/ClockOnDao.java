package com.hs.personnel.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClockOnDao extends BaseDao {
    
    public int add(String emp_no, String status_name, String image) {
        // 檢查參數
        int emp_id = getEmpId(emp_no);    // 從 emp_no 反查 emp_id , 例如 : 0011 -> 1
        int status_id = getStatusId(status_name); // 從 status_name 反查 status_id , 例如 : 下午下班 -> 4
        // 驗證狀態
        if(emp_id == 0)
            return -1;
        if(status_id == 0)
            return -2;
        // 存入資料表
        try {
            String sql = "Insert Into CLOCKON(emp_id, status_id, image) values(?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, emp_id);
            pstmt.setInt(2, status_id);
            pstmt.setString(3, image);
            int rows = pstmt.executeUpdate();
            System.out.println("新增 " + rows + " 筆");
            // 存入成功
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            // 存入失敗
            return 0;
        }
        
    }
    
    // 取得 Image
    public String getImage(int clock_id) {
        try {
            String sql = "Select image From clockon Where clock_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, clock_id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("image");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // 取得 emp_id
    public int getEmpId(String emp_no) {
        try {
            String sql = "Select emp_id From employee Where emp_no = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, emp_no);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("emp_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // 取得 status_id
    public int getStatusId(String status_name) {
        try {
            String sql = "Select status_id From status Where status_name = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status_name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("status_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Query
    public List<Map<String, String>> queryToday(String emp_no) {
        String today = LocalDate.now().toString();
        return query(emp_no, today, today);
    }
    
    public List<Map<String, String>> query(String emp_no, String begin, String end) {
        String today_begin = begin.toString() + " 00:00:00"; // ex: 2019-08-22 00:00:00
        String today_end   = end.toString() + " 23:59:59"; // ex: 2019-08-22 23:59:59
        List<Map<String, String>> list = new ArrayList<>();
        // 查詢資料表
        try {
            String sql = "SELECT c.CLOCK_ID, e.EMP_NO, e.EMP_NAME, s.STATUS_NAME, c.CLOCK_ON, c.IMAGE\n" +
                         "FROM employee e, clockon c, status s\n" +
                         "WHERE e.EMP_NO = ? \n" +
                         "and c.EMP_ID = e.EMP_ID \n" +
                         "and s.STATUS_ID = c.STATUS_ID\n" +
                         "and c.CLOCK_ON between ? and ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, emp_no);
            pstmt.setString(2, today_begin);
            pstmt.setString(3, today_end);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> map = new LinkedHashMap<>();
                map.put("clock_id", rs.getString("clock_id"));
                map.put("emp_no", rs.getString("emp_no"));
                map.put("emp_name", rs.getString("emp_name"));
                map.put("status_name", rs.getString("status_name"));
                map.put("clock_on", rs.getString("clock_on"));
                map.put("image", rs.getString("image"));
                list.add(map); // 加入到 list 集合中
            }
            
        } catch (Exception e) {
            e.printStackTrace();
           
        }
        return list;
    }
}

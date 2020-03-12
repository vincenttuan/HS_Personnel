package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

// Java Format - Java printf Date/Time Format
// http://www.java2s.com/Tutorials/Java/Java_Format/0120__Java_Format_Dates_Times.htm

public class MySQL {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/hs?useUnicode=true";
        String user = "root";
        String password = "12345678";
        Connection conn = DriverManager.getConnection(url, user, password);
        System.out.println(!conn.isClosed());
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Employee");
        while (rs.next()) {
            System.out.printf("%d, %s, %s, %b, %tF %<tT, %s\n",
                    rs.getInt("emp_id"),
                    rs.getString("emp_no"),
                    rs.getNString("emp_name"),
                    rs.getBoolean("emp_active"),
                    rs.getTimestamp("emp_ct"),
                    rs.getString("emp_rfid"));
        }
        conn.close();
    }
}

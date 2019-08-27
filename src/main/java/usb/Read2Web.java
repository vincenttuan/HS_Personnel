package usb;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Read2Web {

    public static void main(String[] args) throws Exception {

        // 封裝 Serial 傳來的訊息
        Message message = new Message() {
            // 顯示所傳來的資料
            @Override
            public void setData(String data) {
                try {
                    System.out.println(data);
                    upload(data);
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
        };

        // 連接指定的 Serial Port
        (new USBReader()).connect("/dev/ttyUSB0", message);

    }

    private static void upload(String data) throws Exception {
        String ip = "192.168.0.21";
        String port = "8080";
        String url = String.format("http://%s:%s/iot/linechart_ultra/upload.jsp?cm=%s", ip, port, data);

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());

    }
}

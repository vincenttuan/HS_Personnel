package usb;
// 在 Netbeans -> 專案 -> 右鍵 -> Properties -> 執行 -> VM選項(V) 輸入以下設定
// -Djava.library.path="/Users/vincenttuan/NetBeansProjects/HS_Sersonnel/libs/rxtx_mac:%PATH%"
// 注意：是「：」不是「;」
public class Read {
    
    public static void main(String[] args) throws Exception {
        
        // 封裝 Serial 傳來的訊息
        Message message = new Message() {
            // 顯示所傳來的資料
            @Override
            public void setData(String data) {
                data = data.replaceAll("\n", "");
//                if(data.contains("Card UID: ")) {
//                    data = data.replaceAll("Card UID: ", "");
//                    data = data.replaceAll(" ", "");
//                }
                System.out.println(data);
            }
        };
        
        // 連接指定的 Serial Port
        (new USBReader()).connect("/dev/cu.wchusbserial14640", message);
        
    }
}


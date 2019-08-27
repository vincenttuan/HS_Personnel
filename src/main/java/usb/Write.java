package usb;

public class Write {
    
    public static void main(String[] args) throws Exception {
        (new USBWriter()).connect("/dev/cu.wchusbserial14620");
    }
    
}

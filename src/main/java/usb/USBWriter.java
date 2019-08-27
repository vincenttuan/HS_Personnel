package usb;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Random;

public class USBWriter {
    static CommPort commPort;
    void connect(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier
                .getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            int timeout = 3000;
            commPort = portIdentifier.open(this.getClass().getName(), timeout);
            
            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();
                
                while(true) {
                    Thread.sleep(timeout);
                    (new Thread(new SerialWriter(out))).start();
                }
                
            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }
    
    public static class SerialWriter implements Runnable {

        OutputStream out;

        public SerialWriter(OutputStream out) {
            this.out = out;
        }

        public void run() {
            
            try {
                int n = new Random().nextInt(3);
                byte[] data = (n + "").getBytes(Charset.forName("UTF-8"));
                out.write(data); // 0~9
                out.flush();
                System.out.println("n:" + n);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (Exception e) {
                }
                
            }
        }
    }
}

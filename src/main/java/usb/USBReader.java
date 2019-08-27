/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package usb;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class USBReader {
    private static Message message;
    
    public void connect(String portName, Message message) throws Exception {
        USBReader.message = message;
        CommPortIdentifier portIdentifier = CommPortIdentifier
                .getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            int timeout = 2000;
            CommPort commPort = portIdentifier.open(this.getClass().getName(), timeout);

            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();
                (new Thread(new SerialReader(new InputStreamReader(in, "UTF-8")))).start();
            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }

    public static class SerialReader implements Runnable {

        InputStreamReader in;

        public SerialReader(InputStreamReader in) {
            this.in = in;
        }

        public void run() {
            char[] buffer = new char[1024];
            int len = -1;
            try {
                while ((len = this.in.read(buffer)) >= -1) {
                    String data = new String(buffer, 0, len);
                    //System.out.print(data);
                    USBReader.message.setData(data);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

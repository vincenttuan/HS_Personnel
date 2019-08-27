package usb;

public abstract class Message {
    private String data;

    public String getData() {
        return data;
    }

    public abstract void setData(String data);
    
}

package web;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BMIWebAPI {

    static OkHttpClient client = new OkHttpClient();

    static String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static void main(String[] args) throws Exception {
        String url = "http://192.168.0.122:8080/JavaWeb0924/servlet/bmi?h=%f&w=%f";
        url = String.format(url, 170.1, 60.5);
        String result = run(url);
        System.out.println(result);
    }
}

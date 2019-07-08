package razerdp.github.com.demo.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import razerdp.github.com.demo.AppContext;

public class FileUtil {
    public static String getFromAssets(String fileName) {
        String result = "";
        try {
            InputStream in = AppContext.getResources().getAssets().open(fileName);
            byte[] buffered = new byte[in.available()];
            in.read(buffered);
            result = new String(buffered, StandardCharsets.UTF_8);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}

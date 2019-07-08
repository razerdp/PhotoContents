package razerdp.github.com.demo;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import razerdp.github.com.demo.utils.FileUtil;
import razerdp.github.com.demo.utils.ToolUtil;

/**
 * Created by 大灯泡 on 2019/4/10
 * <p>
 * Description：内测版测试数据
 */
public class TestServerData {

    private static List<String> pics;
    private static List<String> avatars;


    public static String getPicUrl() {
        if (ToolUtil.isListEmpty(pics)) {
            Pattern pattern = Pattern.compile("(https?|ftp|file)://(?!(\\.jpg|\\.png|\\.gif)).+?(\\.jpg|\\.png|\\.gif)");
            pics = new ArrayList<>();
            String result = FileUtil.getFromAssets("pics");
            Matcher matcher = pattern.matcher(result);
            while (matcher.find()) {
                pics.add(matcher.group(0));
            }
        }
        Random random = new Random();
        return pics.get(random.nextInt(pics.size()));
    }

    public static String getAvatar() {
        if (ToolUtil.isListEmpty(avatars)) {
            Pattern pattern = Pattern.compile("(https?|ftp|file)://(?!(\\.jpg|\\.png|\\.gif)).+?(\\.jpg|\\.png|\\.gif)");
            avatars = new ArrayList<>();
            String result = FileUtil.getFromAssets("avatars");
            Matcher matcher = pattern.matcher(result);
            while (matcher.find()) {
                avatars.add(matcher.group(0));
            }
        }
        Random random = new Random();
        return avatars.get(random.nextInt(avatars.size()));
    }
}

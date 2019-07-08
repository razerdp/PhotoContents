package razerdp.github.com.demo.utils;

import java.util.Random;

/**
 * Created by 大灯泡 on 2019/6/28
 * <p>
 * Description：
 */
public class RandomUtil {
    private static final String CHAR_LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String CHAR_NUMBER = "0123456789";
    private static final String CHAR_ALL = CHAR_NUMBER + CHAR_LETTERS;

    /**
     * 产生长度为length的随机字符串（包括字母和数字）
     *
     * @param length
     * @return
     */
    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(CHAR_ALL.charAt(random.nextInt(CHAR_ALL.length())));
        }
        return sb.toString();
    }

    public static String randomNonNumberString(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(CHAR_LETTERS.charAt(random.nextInt(CHAR_LETTERS.length())));
        }
        return sb.toString();
    }

    public static String randomLowerNonNumberString(int length) {
        return randomNonNumberString(length).toLowerCase();
    }

    public static String randomUpperNonNumberString(int length) {
        return randomNonNumberString(length).toUpperCase();
    }

    public static int randomInt(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }
}

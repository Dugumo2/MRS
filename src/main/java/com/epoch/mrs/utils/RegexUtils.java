package com.epoch.mrs.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

    // 定义邮箱正则表达式
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    // 编译正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    // 校验邮箱方法
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;  // 如果 email 是空的，直接返回 false
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();  // 如果匹配，返回 true，否则返回 false
    }
}

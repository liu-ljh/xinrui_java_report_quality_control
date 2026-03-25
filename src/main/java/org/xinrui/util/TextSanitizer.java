package org.xinrui.util;

/**
 * 文本安全处理（医疗数据脱敏+长度校验）
 */
public class TextSanitizer {
    public static String sanitize(String text) {
        // 医疗脱敏规则（示例：姓名→患者X，身份证→110********）
        return text
                .replaceAll("([张王李赵])([A-Za-z]+)", "患者$1") // 脱敏姓名
                .replaceAll("(\\d{6})\\d{10}(\\d{4})", "$1********$2"); // 脱敏身份证
    }

    public static boolean isLengthValid(String text, int minChars) {
        return text.length() >= minChars;
    }
}


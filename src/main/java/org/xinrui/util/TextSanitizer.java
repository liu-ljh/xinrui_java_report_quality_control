package org.xinrui.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 文本安全处理（医疗数据脱敏+长度校验）
 */
@Slf4j
public class TextSanitizer {

    /**
     * 医疗数据脱敏处理
     * @param text 原始文本
     * @return 脱敏后的文本，如果输入为null则返回空字符串
     */
    public static String sanitize(String text) {


        if (text == null) {
            return "";
        }

        // 医疗脱敏规则（示例：姓名→患者X，身份证→110********）
        return text
                .replaceAll("([张王李赵])([A-Za-z]+)", "患者$1") // 脱敏姓名
                .replaceAll("(\\d{6})\\d{10}(\\d{4})", "$1********$2"); // 脱敏身份证
    }

    /**
     * 检查文本长度是否符合要求
     * @param text 待检查的文本
     * @param minChars 最小字符数
     * @return 如果文本不为null且长度大于等于minChars返回true，否则返回false
     */
    public static boolean isLengthValid(String text, int minChars) {


        // 添加空值检查
        if (text == null) {
            return false;
        }

        // 确保minChars为非负数
        if (minChars < 0) {
            throw new IllegalArgumentException("最小长度不能为负数");
        }

        return text.length() >= minChars;
    }
}

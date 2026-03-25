package org.xinrui.service.model;

import lombok.Data;

/**
 * 结构化输出（仅保留模型原始输出）
 */
@Data
public class QualityResult {
    private String text; // 模型原始输出内容
}
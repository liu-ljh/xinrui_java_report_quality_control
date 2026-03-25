package org.xinrui.service.model;

import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 输入校验模型（医疗文本安全规范）
 */
@Data
public class ReportText {
    @Size(min = 10, message = "报告内容必须≥10字符")
    private String text;
}

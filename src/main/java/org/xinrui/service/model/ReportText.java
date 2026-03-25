package org.xinrui.service.model;

import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 输入校验模型（医疗文本安全规范）
 */
@Data
public class ReportText {

    private String text;
}

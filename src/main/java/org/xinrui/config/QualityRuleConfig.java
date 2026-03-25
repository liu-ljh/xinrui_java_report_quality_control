package org.xinrui.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 质控规则配置（未来可外部化为YAML）
 */
@Component
@ConfigurationProperties(prefix = "quality")
public class QualityRuleConfig {
    private int minReportLength = 10; // 默认最小长度
    private int defaultScore = 85;     // 未发现问题默认分
    private int timeoutMs = 100000;      // API超时(毫秒)

    public int getMinReportLength() { return minReportLength; }
    public void setMinReportLength(int minReportLength) { this.minReportLength = minReportLength; }
    public int getDefaultScore() { return defaultScore; }
    public void setDefaultScore(int defaultScore) { this.defaultScore = defaultScore; }
    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
}

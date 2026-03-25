package org.xinrui.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 从环境变量读取DeepSeek配置（绝不存储密钥）
 */
@Component
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {
    private String apiEndpoint;
    private String apiKey;

    public String getApiEndpoint() { return apiEndpoint; }
    public void setApiEndpoint(String apiEndpoint) { this.apiEndpoint = apiEndpoint; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
}
package org.xinrui.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xinrui.client.DeepSeekClient;
import org.xinrui.config.QualityRuleConfig;
import org.xinrui.dto.ApiResponse;
import org.xinrui.dto.DeepSeekRequestDTO;
import org.xinrui.dto.DeepSeekRequestDTO.Message;
import org.xinrui.dto.DeepSeekRequestDTO.Message.ContentItem;
import org.xinrui.excepction.GlobalExceptionHandler;
import org.xinrui.service.model.QualityResult;
import org.xinrui.util.TextSanitizer;
import org.xinrui.excepction.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class QualityService {
    private final DeepSeekClient deepSeekClient;
    private final QualityRuleConfig ruleConfig;
    private final ObjectMapper objectMapper;

    @Autowired
    public QualityService(DeepSeekClient deepSeekClient, QualityRuleConfig ruleConfig) {
        this.deepSeekClient = deepSeekClient;
        this.ruleConfig = ruleConfig;
        this.objectMapper = new ObjectMapper();
    }

    public QualityResult analyzeReport(DeepSeekRequestDTO request) {

        // 1. 校验消息内容是否为空
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new GlobalExceptionHandler.QualityException(ApiResponse.PARAM_ERROR, "消息内容为空");
        }

        // 2. 提取报告文本
        String reportText = extractReportText(request);
        if (!TextSanitizer.isLengthValid(reportText, ruleConfig.getMinReportLength())) {
            throw new GlobalExceptionHandler.QualityException(
                    ApiResponse.PARAM_ERROR,
                    "报告内容长度不足（需≥" + ruleConfig.getMinReportLength() + "字符）"
            );
        }

        // 3. 文本脱敏
        String sanitizedText = TextSanitizer.sanitize(reportText);

        // 4. 构建 Prompt
        // 逻辑：如果 request.getSystem() 不为空，则使用它作为前缀，否则使用默认的前缀
        // 格式：[System字段内容] + "报告内容为：" + [脱敏后的报告文本]
        String systemPrompt = request.getSystem();
        if (systemPrompt == null || systemPrompt.trim().isEmpty()) {
            // 如果前端未传 system，这里可以设置一个默认值，或者直接设为空字符串
            systemPrompt = "你是一个专业的医疗质控专家，请对下面医疗报告进行质控，并给出修改建议";
        }

        String prompt = systemPrompt + "报告内容为：" + sanitizedText;

        try {
            // 5. 调用DeepSeek API
            // 注意：这里假设 deepSeekClient.callModel 接受完整的 prompt 字符串
            String responseJson = deepSeekClient.callModel(prompt, request.getModel(), request.getTemperature());

            // 6. 提取模型原始输出
            String modelOutput = extractModelOutput(responseJson);
            log.debug("Model raw output: {}", modelOutput);

            // 7. 创建结果对象
            QualityResult result = new QualityResult();
            result.setText(modelOutput);
            return result;
        } catch (IOException e) {
            log.error("DeepSeek API error", e);
            throw new RuntimeException("模型服务不可用", e);
        }
    }

    private String extractReportText(DeepSeekRequestDTO request) {
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new IllegalArgumentException("缺少消息内容");
        }

        Message userMessage = request.getMessages().get(0);
        if (userMessage == null || userMessage.getContent() == null || userMessage.getContent().isEmpty()) {
            throw new IllegalArgumentException("缺少用户消息内容");
        }

        ContentItem contentItem = userMessage.getContent().get(0);
        if (contentItem == null || "text".equals(contentItem.getType()) == false) {
            throw new IllegalArgumentException("消息内容类型不正确");
        }

        // 添加对 getText() 的非空检查
        String text = contentItem.getText();
        if (text == null) {
            throw new IllegalArgumentException("消息文本内容为空");
        }

        return text;
    }

    private String extractModelOutput(String responseJson) throws IOException {
        JsonNode rootNode = objectMapper.readTree(responseJson);
        if (!rootNode.has("choices") || rootNode.get("choices").isEmpty()) {
            throw new IOException("Invalid API response: missing 'choices' array");
        }

        JsonNode choice = rootNode.get("choices").get(0);
        if (!choice.has("message")) {
            throw new IOException("Invalid API response: missing 'message' object");
        }

        JsonNode message = choice.get("message");
        if (!message.has("content")) {
            throw new IOException("Invalid API response: missing 'content' field");
        }

        return message.get("content").asText();
    }
}

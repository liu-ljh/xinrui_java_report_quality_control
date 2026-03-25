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

        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new GlobalExceptionHandler.QualityException(ApiResponse.PARAM_ERROR, "消息内容为空");
        }


        String reportText = extractReportText(request);
        if (!TextSanitizer.isLengthValid(reportText, ruleConfig.getMinReportLength())) {
            throw new GlobalExceptionHandler.QualityException(
                    ApiResponse.PARAM_ERROR,
                    "报告内容长度不足（需≥" + ruleConfig.getMinReportLength() + "字符）"
            );
        }




        // 3. 文本脱敏
        String sanitizedText = TextSanitizer.sanitize(reportText);

        // 4. 构建医疗质控专用Prompt（关键！）
        String prompt = String.format(
                "你是一名资深医疗质控专家。请分析以下报告内容，严格按JSON格式输出，禁止额外解释：\n" +
                        "{\n" +
                        "  \"score\": \"0-100分，基于完整性、逻辑性、合规性评分\",\n" +
                        "  \"summary\": \"问题类型（过诊/少字/报告不完整/其他）\",\n" +
                        "  \"issues\": [{\"type\": \"问题类型\", \"detail\": \"具体描述\"}],\n" +
                        "  \"suggestions\": [\"具体修改建议\"]\n" +
                        "}\n" +
                        "报告内容：%s",
                sanitizedText
        );

        try {
            // 5. 调用DeepSeek API（使用前端传入的model参数）
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

        return contentItem.getText();
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